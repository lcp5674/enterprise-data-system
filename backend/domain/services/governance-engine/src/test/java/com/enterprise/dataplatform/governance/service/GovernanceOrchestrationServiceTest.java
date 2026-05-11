package com.enterprise.dataplatform.governance.service;

import com.enterprise.dataplatform.governance.domain.entity.GovernanceTask;
import com.enterprise.dataplatform.governance.domain.entity.TaskExecution;
import com.enterprise.dataplatform.governance.dto.request.TaskExecutionRequest;
import com.enterprise.dataplatform.governance.dto.response.TaskExecutionResponse;
import com.enterprise.dataplatform.governance.repository.GovernancePolicyRepository;
import com.enterprise.dataplatform.governance.repository.GovernanceTaskRepository;
import com.enterprise.dataplatform.governance.repository.TaskExecutionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("治理任务编排服务测试")
class GovernanceOrchestrationServiceTest {

    @Mock
    private GovernanceTaskRepository taskRepository;

    @Mock
    private TaskExecutionRepository executionRepository;

    @Mock
    private GovernancePolicyRepository policyRepository;

    @InjectMocks
    private GovernanceOrchestrationService orchestrationService;

    private GovernanceTask testTask;
    private TaskExecution testExecution;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(orchestrationService, "defaultTimeout", 3600);
        ReflectionTestUtils.setField(orchestrationService, "maxRetry", 3);

        testTask = GovernanceTask.builder()
                .id(1L)
                .taskCode("TASK-001")
                .taskName("测试任务")
                .taskType("ORCHESTRATION")
                .taskStatus("PENDING")
                .taskParams("{}")
                .upstreamTasks(new ArrayList<>())
                .downstreamTasks(new ArrayList<>())
                .build();

        testExecution = TaskExecution.builder()
                .id(1L)
                .batchNo("EXEC-12345678")
                .task(testTask)
                .taskCode(testTask.getTaskCode())
                .taskName(testTask.getTaskName())
                .taskType(testTask.getTaskType())
                .executionStatus("RUNNING")
                .startTime(LocalDateTime.now())
                .executor("test-user")
                .build();
    }

    @Test
    @DisplayName("执行任务 - 正常执行场景")
    void testExecuteTask_Success() {
        TaskExecutionRequest request = TaskExecutionRequest.builder()
                .taskId(1L)
                .executionParams(Map.of())
                .build();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(executionRepository.save(any(TaskExecution.class))).thenAnswer(invocation -> {
            TaskExecution saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });
        when(taskRepository.save(any(GovernanceTask.class))).thenReturn(testTask);

        TaskExecutionResponse response = orchestrationService.executeTask(request, "test-executor");

        assertThat(response).isNotNull();
        assertThat(response.getTaskCode()).isEqualTo("TASK-001");
        assertThat(response.getExecutionStatus()).isEqualTo("RUNNING");
        verify(taskRepository, atLeast(1)).save(any(GovernanceTask.class));
        verify(executionRepository, times(1)).save(any(TaskExecution.class));
    }

    @Test
    @DisplayName("执行任务 - 任务不存在抛出异常")
    void testExecuteTask_TaskNotFound() {
        TaskExecutionRequest request = TaskExecutionRequest.builder()
                .taskId(999L)
                .executionParams(Map.of())
                .build();

        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orchestrationService.executeTask(request, "test-executor"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("任务不存在");
    }

    @Test
    @DisplayName("异步执行任务 - 执行成功场景")
    void testExecuteTaskAsync_Success() {
        when(executionRepository.findById(1L)).thenReturn(Optional.of(testExecution));
        when(executionRepository.save(any(TaskExecution.class))).thenReturn(testExecution);
        when(taskRepository.save(any(GovernanceTask.class))).thenReturn(testTask);

        orchestrationService.executeTaskAsync(1L);

        verify(executionRepository, times(1)).save(any(TaskExecution.class));
        verify(taskRepository, times(1)).save(any(GovernanceTask.class));
    }

    @Test
    @DisplayName("异步执行任务 - 执行记录不存在")
    void testExecuteTaskAsync_ExecutionNotFound() {
        when(executionRepository.findById(999L)).thenReturn(Optional.empty());

        orchestrationService.executeTaskAsync(999L);

        verify(executionRepository, never()).save(any(TaskExecution.class));
    }

    @Test
    @DisplayName("更新任务为失败状态")
    void testUpdateTaskToFailed() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(GovernanceTask.class))).thenReturn(testTask);

        orchestrationService.updateTaskToFailed(1L, "Test error message");

        verify(taskRepository, times(1)).save(any(GovernanceTask.class));
    }

    @Test
    @DisplayName("查询执行记录 - 正常查询")
    void testGetExecution_Success() {
        when(executionRepository.findById(1L)).thenReturn(Optional.of(testExecution));

        TaskExecutionResponse response = orchestrationService.getExecution(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getBatchNo()).isEqualTo("EXEC-12345678");
    }

    @Test
    @DisplayName("查询执行记录 - 记录不存在抛出异常")
    void testGetExecution_NotFound() {
        when(executionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orchestrationService.getExecution(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("执行记录不存在");
    }

    @Test
    @DisplayName("执行编排类型任务 - 无子任务配置")
    void testExecuteOrchestrationTask_NoSubtasks() {
        testTask.setTaskType("ORCHESTRATION");
        testTask.setTaskParams("{}");

        when(executionRepository.findById(1L)).thenReturn(Optional.of(testExecution));
        when(executionRepository.save(any(TaskExecution.class))).thenReturn(testExecution);
        when(taskRepository.save(any(GovernanceTask.class))).thenReturn(testTask);

        orchestrationService.executeTaskAsync(1L);

        verify(executionRepository, times(1)).save(any(TaskExecution.class));
        verify(taskRepository, times(1)).save(any(GovernanceTask.class));
    }

    @Test
    @DisplayName("执行通知类型任务")
    void testExecuteNotificationTask() {
        testTask.setTaskType("NOTIFICATION");
        testTask.setTaskParams("{\"recipient\":\"test@example.com\",\"subject\":\"Test\",\"message\":\"Hello\"}");

        when(executionRepository.findById(1L)).thenReturn(Optional.of(testExecution));
        when(executionRepository.save(any(TaskExecution.class))).thenReturn(testExecution);
        when(taskRepository.save(any(GovernanceTask.class))).thenReturn(testTask);

        orchestrationService.executeTaskAsync(1L);

        verify(executionRepository, times(1)).save(any(TaskExecution.class));
    }

    @Test
    @DisplayName("执行报告类型任务")
    void testExecuteReportingTask() {
        testTask.setTaskType("REPORTING");
        testTask.setTaskParams("{\"reportType\":\"GOVERNANCE\",\"format\":\"PDF\"}");

        when(executionRepository.findById(1L)).thenReturn(Optional.of(testExecution));
        when(executionRepository.save(any(TaskExecution.class))).thenReturn(testExecution);
        when(taskRepository.save(any(GovernanceTask.class))).thenReturn(testTask);

        orchestrationService.executeTaskAsync(1L);

        verify(executionRepository, times(1)).save(any(TaskExecution.class));
    }

    @Test
    @DisplayName("执行自动修复类型任务")
    void testExecuteAutoRemediationTask() {
        testTask.setTaskType("AUTO_REMEDIATION");
        testTask.setTaskParams("{\"assetId\":\"asset-001\",\"issueType\":\"DATA_QUALITY\",\"severity\":\"HIGH\"}");

        when(executionRepository.findById(1L)).thenReturn(Optional.of(testExecution));
        when(executionRepository.save(any(TaskExecution.class))).thenReturn(testExecution);
        when(taskRepository.save(any(GovernanceTask.class))).thenReturn(testTask);

        orchestrationService.executeTaskAsync(1L);

        verify(executionRepository, times(1)).save(any(TaskExecution.class));
    }

    @Test
    @DisplayName("执行默认类型任务")
    void testExecuteDefaultTask() {
        testTask.setTaskType("UNKNOWN_TYPE");
        testTask.setTaskParams("{}");

        when(executionRepository.findById(1L)).thenReturn(Optional.of(testExecution));
        when(executionRepository.save(any(TaskExecution.class))).thenReturn(testExecution);
        when(taskRepository.save(any(GovernanceTask.class))).thenReturn(testTask);

        orchestrationService.executeTaskAsync(1L);

        verify(executionRepository, times(1)).save(any(TaskExecution.class));
    }

    @Test
    @DisplayName("诊断结果类应正确保存数据")
    void testDiagnosisResultClass() {
        GovernanceOrchestrationService.DiagnosisResult result = new GovernanceOrchestrationService.DiagnosisResult();
        result.setAssetId("asset-001");
        result.setIssueType("DATA_QUALITY");
        result.setSeverity("HIGH");
        result.setDiagnosedAt(LocalDateTime.now());
        result.setSymptoms(List.of("symptom1", "symptom2"));
        result.setRootCause("root cause");

        assertThat(result.getAssetId()).isEqualTo("asset-001");
        assertThat(result.getIssueType()).isEqualTo("DATA_QUALITY");
        assertThat(result.getSymptoms()).hasSize(2);
    }

    @Test
    @DisplayName("修复方案类应正确保存数据")
    void testRemediationPlanClass() {
        GovernanceOrchestrationService.RemediationPlan plan = new GovernanceOrchestrationService.RemediationPlan();
        plan.setDiagnosisId("diag-001");
        plan.setStatus("SUCCESS");
        plan.setEstimatedDuration(300);
        plan.setCreatedAt(LocalDateTime.now());

        GovernanceOrchestrationService.DiagnosisResult diagnosis = new GovernanceOrchestrationService.DiagnosisResult();
        plan.setDiagnosisResult(diagnosis);

        GovernanceOrchestrationService.RemediationStep step = new GovernanceOrchestrationService.RemediationStep();
        step.setStepType("CLEANUP");
        step.setDescription("Clean invalid data");
        step.setOrder(1);
        step.setStatus("COMPLETED");
        plan.setSteps(List.of(step));

        assertThat(plan.getDiagnosisId()).isEqualTo("diag-001");
        assertThat(plan.getSteps()).hasSize(1);
        assertThat(plan.getSteps().get(0).getStatus()).isEqualTo("COMPLETED");
    }

    @Test
    @DisplayName("修复步骤类应正确保存数据")
    void testRemediationStepClass() {
        GovernanceOrchestrationService.RemediationStep step = new GovernanceOrchestrationService.RemediationStep();
        step.setStepType("NOTIFICATION");
        step.setDescription("Send notification");
        step.setOrder(2);
        step.setStatus("PENDING");
        step.setErrorMessage(null);
        step.setCompletedAt(LocalDateTime.now());

        assertThat(step.getStepType()).isEqualTo("NOTIFICATION");
        assertThat(step.getOrder()).isEqualTo(2);
        assertThat(step.getStatus()).isEqualTo("PENDING");
    }
}
