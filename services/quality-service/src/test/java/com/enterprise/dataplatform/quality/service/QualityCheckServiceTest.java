package com.enterprise.dataplatform.quality.service;

import com.enterprise.dataplatform.quality.domain.entity.QualityCheckResult;
import com.enterprise.dataplatform.quality.domain.entity.QualityCheckTask;
import com.enterprise.dataplatform.quality.domain.entity.QualityRule;
import com.enterprise.dataplatform.quality.dto.request.CheckExecutionRequest;
import com.enterprise.dataplatform.quality.dto.response.QualityCheckResultResponse;
import com.enterprise.dataplatform.quality.repository.QualityCheckResultRepository;
import com.enterprise.dataplatform.quality.repository.QualityCheckTaskRepository;
import com.enterprise.dataplatform.quality.repository.QualityRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * QualityCheckService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("质量检查服务测试")
class QualityCheckServiceTest {

    @Mock
    private QualityCheckTaskRepository taskRepository;

    @Mock
    private QualityCheckResultRepository resultRepository;

    @Mock
    private QualityRuleRepository ruleRepository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private QualityCheckService qualityCheckService;

    private QualityCheckTask testTask;
    private QualityCheckResult testResult;
    private QualityRule testRule;
    private CheckExecutionRequest testRequest;

    @BeforeEach
    void setUp() {
        testRule = QualityRule.builder()
                .id(1L)
                .ruleCode("RULE-001")
                .ruleName("测试规则")
                .ruleType("COMPLETENESS")
                .alertThreshold(5.0)
                .errorThreshold(10.0)
                .build();

        testTask = QualityCheckTask.builder()
                .id(1L)
                .taskCode("TASK-001")
                .taskName("测试检查任务")
                .rule(testRule)
                .assetId("ASSET-001")
                .assetName("测试资产")
                .taskStatus("PENDING")
                .executionParams("{}")
                .build();

        testResult = QualityCheckResult.builder()
                .id(1L)
                .batchNo("CHECK-12345678")
                .task(testTask)
                .rule(testRule)
                .ruleCode(testRule.getRuleCode())
                .ruleName(testRule.getRuleName())
                .ruleType(testRule.getRuleType())
                .assetId(testTask.getAssetId())
                .assetName(testTask.getAssetName())
                .checkStatus("RUNNING")
                .checkResult("RUNNING")
                .checkTime(LocalDateTime.now())
                .executor("test-executor")
                .build();

        testRequest = CheckExecutionRequest.builder()
                .taskId(1L)
                .executionParams("{}")
                .build();
    }

    @Test
    @DisplayName("执行质量检查 - 成功")
    void testExecuteCheck_Success() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(resultRepository.save(any(QualityCheckResult.class))).thenReturn(testResult);
        when(taskRepository.save(any(QualityCheckTask.class))).thenReturn(testTask);

        // When
        QualityCheckResultResponse response = qualityCheckService.executeCheck(testRequest, "test-executor");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getBatchNo()).isEqualTo("CHECK-12345678");
        verify(taskRepository, times(1)).save(any(QualityCheckTask.class));
        verify(resultRepository, times(1)).save(any(QualityCheckResult.class));
    }

    @Test
    @DisplayName("执行质量检查 - 任务不存在")
    void testExecuteCheck_TaskNotFound() {
        // Given
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> qualityCheckService.executeCheck(testRequest, "test-executor"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("任务不存在");
    }

    @Test
    @DisplayName("异步执行质量检查 - 检查成功")
    void testExecuteCheckAsync_Success() {
        // Given
        when(resultRepository.findById(1L)).thenReturn(Optional.of(testResult));
        when(resultRepository.save(any(QualityCheckResult.class))).thenReturn(testResult);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(QualityCheckTask.class))).thenReturn(testTask);
        doNothing().when(kafkaTemplate).send(anyString(), anyString(), anyString());

        // When
        qualityCheckService.executeCheckAsync(1L, testRequest);

        // Then
        verify(resultRepository, atLeastOnce()).save(any(QualityCheckResult.class));
    }

    @Test
    @DisplayName("异步执行质量检查 - 结果不存在")
    void testExecuteCheckAsync_ResultNotFound() {
        // Given
        when(resultRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        qualityCheckService.executeCheckAsync(999L, testRequest);

        // Then
        verify(resultRepository, never()).save(any(QualityCheckResult.class));
    }

    @Test
    @DisplayName("更新检查结果为失败")
    void testUpdateResultToFailed() {
        // Given
        testResult.setTask(testTask);
        when(resultRepository.findById(1L)).thenReturn(Optional.of(testResult));
        when(resultRepository.save(any(QualityCheckResult.class))).thenReturn(testResult);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(QualityCheckTask.class))).thenReturn(testTask);

        // When
        qualityCheckService.updateResultToFailed(1L, "Test error");

        // Then
        verify(resultRepository, times(1)).save(any(QualityCheckResult.class));
    }

    @Test
    @DisplayName("查询检查结果 - 成功")
    void testGetCheckResult_Success() {
        // Given
        when(resultRepository.findById(1L)).thenReturn(Optional.of(testResult));

        // When
        QualityCheckResultResponse response = qualityCheckService.getCheckResult(1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getBatchNo()).isEqualTo("CHECK-12345678");
    }

    @Test
    @DisplayName("查询检查结果 - 结果不存在")
    void testGetCheckResult_NotFound() {
        // Given
        when(resultRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> qualityCheckService.getCheckResult(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("结果不存在");
    }

    @Test
    @DisplayName("批量执行质量检查")
    void testBatchExecuteCheck() {
        // Given
        QualityCheckTask task2 = QualityCheckTask.builder()
                .id(2L)
                .taskCode("TASK-002")
                .taskName("测试任务2")
                .rule(testRule)
                .assetId("ASSET-002")
                .assetName("测试资产2")
                .taskStatus("PENDING")
                .build();

        List<Long> taskIds = List.of(1L, 2L);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.findById(2L)).thenReturn(Optional.of(task2));
        when(resultRepository.save(any(QualityCheckResult.class))).thenAnswer(invocation -> {
            QualityCheckResult result = invocation.getArgument(0);
            result.setId(1L);
            return result;
        });
        when(taskRepository.save(any(QualityCheckTask.class))).thenReturn(testTask);

        // When
        List<QualityCheckResultResponse> responses = qualityCheckService.batchExecuteCheck(taskIds, "batch-executor");

        // Then
        assertThat(responses).isNotNull();
        verify(taskRepository, atLeast(2)).findById(any());
    }
}
