package com.enterprise.dataplatform.governance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.dataplatform.governance.domain.entity.GovernancePolicy;
import com.enterprise.dataplatform.governance.domain.entity.GovernanceTask;
import com.enterprise.dataplatform.governance.domain.entity.TaskExecution;
import com.enterprise.dataplatform.governance.dto.request.GovernancePolicyRequest;
import com.enterprise.dataplatform.governance.dto.request.GovernanceTaskRequest;
import com.enterprise.dataplatform.governance.repository.GovernancePolicyRepository;
import com.enterprise.dataplatform.governance.repository.GovernanceTaskRepository;
import com.enterprise.dataplatform.governance.repository.TaskExecutionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GovernanceOrchestrationServiceTest {

    @Mock
    private GovernancePolicyRepository policyRepository;

    @Mock
    private GovernanceTaskRepository taskRepository;

    @Mock
    private TaskExecutionRepository executionRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private GovernanceOrchestrationService orchestrationService;

    private GovernancePolicy testPolicy;
    private GovernanceTask testTask;

    @BeforeEach
    void setUp() {
        testPolicy = GovernancePolicy.builder()
                .id(1L)
                .name("测试策略")
                .policyType("QUALITY_CHECK")
                .assetType("TABLE")
                .priority(1)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();

        testTask = GovernanceTask.builder()
                .id(1L)
                .taskName("测试任务")
                .taskType("QUALITY_CHECK")
                .policyId(1L)
                .priority(1)
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createPolicy_shouldCreatePolicySuccessfully() {
        GovernancePolicyRequest request = GovernancePolicyRequest.builder()
                .name("新策略")
                .policyType("QUALITY_CHECK")
                .assetType("TABLE")
                .priority(2)
                .build();

        when(policyRepository.save(any(GovernancePolicy.class))).thenReturn(testPolicy);

        GovernancePolicy result = orchestrationService.createPolicy(request);

        assertNotNull(result);
        assertEquals("测试策略", result.getName());
        verify(policyRepository, times(1)).save(any(GovernancePolicy.class));
    }

    @Test
    void updatePolicy_shouldUpdatePolicySuccessfully() {
        GovernancePolicyRequest request = GovernancePolicyRequest.builder()
                .name("更新后的策略")
                .policyType("QUALITY_CHECK")
                .assetType("TABLE")
                .priority(1)
                .build();

        when(policyRepository.findById(1L)).thenReturn(Optional.of(testPolicy));
        when(policyRepository.save(any(GovernancePolicy.class))).thenReturn(testPolicy);

        GovernancePolicy result = orchestrationService.updatePolicy(1L, request);

        assertNotNull(result);
        verify(policyRepository, times(1)).save(any(GovernancePolicy.class));
    }

    @Test
    void deletePolicy_shouldDeletePolicySuccessfully() {
        when(policyRepository.findById(1L)).thenReturn(Optional.of(testPolicy));
        doNothing().when(policyRepository).delete(any(GovernancePolicy.class));

        assertDoesNotThrow(() -> orchestrationService.deletePolicy(1L));

        verify(policyRepository, times(1)).delete(any(GovernancePolicy.class));
    }

    @Test
    void getPolicyById_shouldReturnPolicyWhenExists() {
        when(policyRepository.findById(1L)).thenReturn(Optional.of(testPolicy));

        Optional<GovernancePolicy> result = orchestrationService.getPolicyById(1L);

        assertTrue(result.isPresent());
        assertEquals(testPolicy.getName(), result.get().getName());
    }

    @Test
    void getPolicyById_shouldReturnEmptyWhenNotExists() {
        when(policyRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<GovernancePolicy> result = orchestrationService.getPolicyById(99L);

        assertFalse(result.isPresent());
    }

    @Test
    void listPolicies_shouldReturnPagedResults() {
        Page<GovernancePolicy> page = new Page<>(1, 10);
        page.setRecords(List.of(testPolicy));
        page.setTotal(1);

        when(policyRepository.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        Page<GovernancePolicy> result = orchestrationService.listPolicies(
                1, 10, null, null, null, null);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
    }

    @Test
    void createTask_shouldCreateTaskSuccessfully() {
        GovernanceTaskRequest request = GovernanceTaskRequest.builder()
                .taskName("新任务")
                .taskType("QUALITY_CHECK")
                .policyId(1L)
                .priority(2)
                .build();

        when(taskRepository.save(any(GovernanceTask.class))).thenReturn(testTask);

        GovernanceTask result = orchestrationService.createTask(request);

        assertNotNull(result);
        verify(taskRepository, times(1)).save(any(GovernanceTask.class));
    }

    @Test
    void executeTask_shouldExecuteTaskSuccessfully() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(executionRepository.save(any(TaskExecution.class)))
                .thenAnswer(invocation -> {
                    TaskExecution exec = invocation.getArgument(0);
                    exec.setId(1L);
                    return exec;
                });
        when(UUID.randomUUID()).thenReturn(new UUID(0, 1));

        String executionId = orchestrationService.executeTask(1L);

        assertNotNull(executionId);
        verify(kafkaTemplate, times(1)).send(anyString(), anyString(), any());
    }

    @Test
    void cancelTask_shouldCancelTaskSuccessfully() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(GovernanceTask.class))).thenReturn(testTask);

        Optional<GovernanceTask> result = orchestrationService.cancelTask(1L);

        assertTrue(result.isPresent());
        verify(taskRepository, times(1)).save(any(GovernanceTask.class));
    }

    @Test
    void getPendingTasks_shouldReturnPendingTasks() {
        when(taskRepository.findByStatus("PENDING")).thenReturn(List.of(testTask));

        List<GovernanceTask> result = orchestrationService.getPendingTasks(null, 50);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void updatePolicyStatus_shouldUpdateStatusSuccessfully() {
        when(policyRepository.findById(1L)).thenReturn(Optional.of(testPolicy));
        when(policyRepository.save(any(GovernancePolicy.class))).thenReturn(testPolicy);

        GovernancePolicy result = orchestrationService.updatePolicyStatus(1L, false);

        assertNotNull(result);
        verify(policyRepository, times(1)).save(any(GovernancePolicy.class));
    }

    @Test
    void getAvailablePolicyTypes_shouldReturnTypeList() {
        List<Map<String, Object>> types = orchestrationService.getAvailablePolicyTypes();

        assertNotNull(types);
        assertFalse(types.isEmpty());
    }
}
