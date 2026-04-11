package com.enterprise.dataplatform.governance.service;

import com.enterprise.dataplatform.governance.domain.entity.GovernanceTask;
import com.enterprise.dataplatform.governance.domain.entity.TaskExecution;
import com.enterprise.dataplatform.governance.dto.request.TaskExecutionRequest;
import com.enterprise.dataplatform.governance.dto.response.TaskExecutionResponse;
import com.enterprise.dataplatform.governance.repository.GovernanceTaskRepository;
import com.enterprise.dataplatform.governance.repository.TaskExecutionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * GovernanceOrchestrationService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("治理任务编排服务测试")
class GovernanceOrchestrationServiceTest {

    @Mock
    private GovernanceTaskRepository taskRepository;

    @Mock
    private TaskExecutionRepository executionRepository;

    @InjectMocks
    private GovernanceOrchestrationService orchestrationService;

    private GovernanceTask testTask;
    private TaskExecution testExecution;

    @BeforeEach
    void setUp() {
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
        // Given
        TaskExecutionRequest request = TaskExecutionRequest.builder()
                .taskId(1L)
                .executionParams("{}")
                .build();
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(executionRepository.save(any(TaskExecution.class))).thenAnswer(invocation -> {
            TaskExecution saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });
        when(taskRepository.save(any(GovernanceTask.class))).thenReturn(testTask);

        // When
        TaskExecutionResponse response = orchestrationService.executeTask(request, "test-executor");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTaskCode()).isEqualTo("TASK-001");
        assertThat(response.getExecutionStatus()).isEqualTo("RUNNING");
        verify(taskRepository, times(2)).save(any(GovernanceTask.class));
        verify(executionRepository, times(1)).save(any(TaskExecution.class));
    }

    @Test
    @DisplayName("执行任务 - 任务不存在抛出异常")
    void testExecuteTask_TaskNotFound() {
        // Given
        TaskExecutionRequest request = TaskExecutionRequest.builder()
                .taskId(999L)
                .executionParams("{}")
                .build();
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> orchestrationService.executeTask(request, "test-executor"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("任务不存在");
    }

    @Test
    @DisplayName("异步执行任务 - 执行成功场景")
    void testExecuteTaskAsync_Success() {
        // Given
        when(executionRepository.findById(1L)).thenReturn(Optional.of(testExecution));
        when(executionRepository.save(any(TaskExecution.class))).thenReturn(testExecution);
        when(taskRepository.save(any(GovernanceTask.class))).thenReturn(testTask);

        // When
        orchestrationService.executeTaskAsync(1L);

        // Then
        verify(executionRepository, times(1)).save(any(TaskExecution.class));
        verify(taskRepository, times(1)).save(any(GovernanceTask.class));
    }

    @Test
    @DisplayName("异步执行任务 - 执行记录不存在")
    void testExecuteTaskAsync_ExecutionNotFound() {
        // Given
        when(executionRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        orchestrationService.executeTaskAsync(999L);

        // Then
        verify(executionRepository, never()).save(any(TaskExecution.class));
    }

    @Test
    @DisplayName("异步执行任务 - 执行失败场景")
    void testExecuteTaskAsync_Failure() {
        // Given
        testTask.setTaskType("UNKNOWN_TYPE");
        when(executionRepository.findById(1L)).thenReturn(Optional.of(testExecution));
        when(executionRepository.save(any(TaskExecution.class))).thenReturn(testExecution);
        when(taskRepository.save(any(GovernanceTask.class))).thenReturn(testTask);

        // When
        orchestrationService.executeTaskAsync(1L);

        // Then
        verify(executionRepository, times(1)).save(any(TaskExecution.class));
    }

    @Test
    @DisplayName("更新任务为失败状态")
    void testUpdateTaskToFailed() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(GovernanceTask.class))).thenReturn(testTask);

        // When
        orchestrationService.updateTaskToFailed(1L, "Test error message");

        // Then
        verify(taskRepository, times(1)).save(any(GovernanceTask.class));
    }

    @Test
    @DisplayName("查询执行记录 - 正常查询")
    void testGetExecution_Success() {
        // Given
        when(executionRepository.findById(1L)).thenReturn(Optional.of(testExecution));

        // When
        TaskExecutionResponse response = orchestrationService.getExecution(1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getBatchNo()).isEqualTo("EXEC-12345678");
    }

    @Test
    @DisplayName("查询执行记录 - 记录不存在抛出异常")
    void testGetExecution_NotFound() {
        // Given
        when(executionRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> orchestrationService.getExecution(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("执行记录不存在");
    }

    @Test
    @DisplayName("批量执行任务 - 多任务执行")
    void testBatchExecuteTasks() {
        // Given
        GovernanceTask task2 = GovernanceTask.builder()
                .id(2L)
                .taskCode("TASK-002")
                .taskName("测试任务2")
                .taskType("NOTIFICATION")
                .taskStatus("PENDING")
                .upstreamTasks(new ArrayList<>())
                .downstreamTasks(new ArrayList<>())
                .build();

        List<Long> taskIds = List.of(1L, 2L);
        when(taskRepository.findAllById(taskIds)).thenReturn(List.of(testTask, task2));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.findById(2L)).thenReturn(Optional.of(task2));
        when(executionRepository.save(any(TaskExecution.class))).thenAnswer(invocation -> {
            TaskExecution saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });
        when(taskRepository.save(any(GovernanceTask.class))).thenReturn(testTask);

        // When
        List<TaskExecutionResponse> responses = orchestrationService.batchExecuteTasks(taskIds, "batch-executor");

        // Then
        assertThat(responses).isNotNull();
        verify(taskRepository, atLeast(2)).findById(any());
    }

    @Test
    @DisplayName("执行编排类型任务")
    void testExecuteOrchestrationTask() {
        // Given
        testTask.setTaskType("ORCHESTRATION");

        // When
        orchestrationService.executeTaskAsync(1L);

        // Then - 验证任务执行完成
        verify(executionRepository, times(1)).save(any(TaskExecution.class));
    }

    @Test
    @DisplayName("执行通知类型任务")
    void testExecuteNotificationTask() {
        // Given
        testTask.setTaskType("NOTIFICATION");
        when(executionRepository.findById(1L)).thenReturn(Optional.of(testExecution));
        when(executionRepository.save(any(TaskExecution.class))).thenReturn(testExecution);
        when(taskRepository.save(any(GovernanceTask.class))).thenReturn(testTask);

        // When
        orchestrationService.executeTaskAsync(1L);

        // Then
        verify(executionRepository, times(1)).save(any(TaskExecution.class));
    }

    @Test
    @DisplayName("执行报告类型任务")
    void testExecuteReportingTask() {
        // Given
        testTask.setTaskType("REPORTING");
        when(executionRepository.findById(1L)).thenReturn(Optional.of(testExecution));
        when(executionRepository.save(any(TaskExecution.class))).thenReturn(testExecution);
        when(taskRepository.save(any(GovernanceTask.class))).thenReturn(testTask);

        // When
        orchestrationService.executeTaskAsync(1L);

        // Then
        verify(executionRepository, times(1)).save(any(TaskExecution.class));
    }

    @Test
    @DisplayName("执行自动修复类型任务")
    void testExecuteAutoRemediationTask() {
        // Given
        testTask.setTaskType("AUTO_REMEDIATION");
        when(executionRepository.findById(1L)).thenReturn(Optional.of(testExecution));
        when(executionRepository.save(any(TaskExecution.class))).thenReturn(testExecution);
        when(taskRepository.save(any(GovernanceTask.class))).thenReturn(testTask);

        // When
        orchestrationService.executeTaskAsync(1L);

        // Then
        verify(executionRepository, times(1)).save(any(TaskExecution.class));
    }
}
