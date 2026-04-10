package com.enterprise.dataplatform.quality.service;

import com.enterprise.dataplatform.quality.domain.entity.QualityCheckResult;
import com.enterprise.dataplatform.quality.domain.entity.QualityRule;
import com.enterprise.dataplatform.quality.dto.request.CheckExecutionRequest;
import com.enterprise.dataplatform.quality.repository.QualityCheckResultRepository;
import com.enterprise.dataplatform.quality.repository.QualityRuleRepository;
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
class QualityCheckServiceTest {

    @Mock
    private QualityRuleRepository ruleRepository;

    @Mock
    private QualityCheckResultRepository resultRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private QualityCheckService checkService;

    private QualityRule testRule;
    private QualityCheckResult testResult;

    @BeforeEach
    void setUp() {
        testRule = QualityRule.builder()
                .id(1L)
                .ruleName("完整性检测")
                .ruleCode("RULE_001")
                .ruleType("COMPLETENESS")
                .checkSql("SELECT COUNT(*) FROM ${table}")
                .threshold(0.95)
                .operator("GTE")
                .enabled(true)
                .build();

        testResult = QualityCheckResult.builder()
                .id(1L)
                .ruleId(1L)
                .assetId("TABLE_001")
                .assetType("TABLE")
                .checkStatus("PASSED")
                .checkScore(98.5)
                .actualValue(100.0)
                .thresholdValue(95.0)
                .executingAt(LocalDateTime.now())
                .build();
    }

    @Test
    void executeCheck_shouldExecuteSuccessfully() {
        CheckExecutionRequest request = CheckExecutionRequest.builder()
                .ruleId(1L)
                .assetId("TABLE_001")
                .assetType("TABLE")
                .build();

        when(ruleRepository.findById(1L)).thenReturn(Optional.of(testRule));
        when(resultRepository.save(any(QualityCheckResult.class))).thenReturn(testResult);

        QualityCheckResult result = checkService.executeCheck(request);

        assertNotNull(result);
        verify(resultRepository, times(1)).save(any(QualityCheckResult.class));
    }

    @Test
    void executeCheck_shouldReturnFailedResult() {
        testResult.setCheckStatus("FAILED");
        testResult.setCheckScore(60.0);

        CheckExecutionRequest request = CheckExecutionRequest.builder()
                .ruleId(1L)
                .assetId("TABLE_001")
                .assetType("TABLE")
                .build();

        when(ruleRepository.findById(1L)).thenReturn(Optional.of(testRule));
        when(resultRepository.save(any(QualityCheckResult.class))).thenReturn(testResult);

        QualityCheckResult result = checkService.executeCheck(request);

        assertNotNull(result);
        assertEquals("FAILED", result.getCheckStatus());
    }

    @Test
    void batchExecuteChecks_shouldExecuteMultipleChecks() {
        CheckExecutionRequest request = CheckExecutionRequest.builder()
                .ruleId(1L)
                .assetIds(List.of("TABLE_001", "TABLE_002", "TABLE_003"))
                .assetType("TABLE")
                .build();

        when(ruleRepository.findById(1L)).thenReturn(Optional.of(testRule));
        when(resultRepository.save(any(QualityCheckResult.class))).thenReturn(testResult);

        List<QualityCheckResult> results = checkService.batchExecuteChecks(request);

        assertNotNull(results);
        assertEquals(3, results.size());
        verify(resultRepository, times(3)).save(any(QualityCheckResult.class));
    }

    @Test
    void getCheckHistory_shouldReturnHistory() {
        when(resultRepository.findByRuleIdOrderByExecutingAtDesc(1L))
                .thenReturn(List.of(testResult));

        List<QualityCheckResult> results = checkService.getCheckHistory(1L);

        assertNotNull(results);
        assertEquals(1, results.size());
    }

    @Test
    void getCheckHistoryByAsset_shouldReturnAssetHistory() {
        when(resultRepository.findByAssetIdOrderByExecutingAtDesc("TABLE_001"))
                .thenReturn(List.of(testResult));

        List<QualityCheckResult> results = checkService.getCheckHistoryByAsset("TABLE_001");

        assertNotNull(results);
        assertEquals(1, results.size());
    }

    @Test
    void getQualityScore_shouldCalculateScore() {
        when(resultRepository.findByAssetIdAndExecutingAtAfter(
                eq("TABLE_001"), any(LocalDateTime.class)))
                .thenReturn(List.of(testResult));

        Map<String, Object> score = checkService.getQualityScore("TABLE_001");

        assertNotNull(score);
        assertTrue(score.containsKey("overallScore"));
        assertTrue(score.containsKey("ruleResults"));
    }

    @Test
    void getQualityTrend_shouldReturnTrend() {
        when(resultRepository.findByAssetIdAndRuleIdAndExecutingAtAfter(
                eq("TABLE_001"), eq(1L), any(LocalDateTime.class)))
                .thenReturn(List.of(testResult));

        Map<String, Object> trend = checkService.getQualityTrend("TABLE_001", 1L);

        assertNotNull(trend);
        assertTrue(trend.containsKey("dates"));
        assertTrue(trend.containsKey("scores"));
    }

    @Test
    void getRuleStatistics_shouldReturnStatistics() {
        when(resultRepository.countByRuleIdAndCheckStatus(1L, "PASSED")).thenReturn(80L);
        when(resultRepository.countByRuleId(1L)).thenReturn(100L);

        Map<String, Object> stats = checkService.getRuleStatistics(1L);

        assertNotNull(stats);
        assertEquals(80, stats.get("passedCount"));
        assertEquals(100, stats.get("totalCount"));
        assertEquals(80.0, stats.get("passRate"));
    }

    @Test
    void publishQualityEvent_shouldSendKafkaMessage() {
        when(resultRepository.findById(1L)).thenReturn(Optional.of(testResult));
        doNothing().when(kafkaTemplate).send(anyString(), anyString(), any());

        assertDoesNotThrow(() -> checkService.publishQualityEvent(1L));

        verify(kafkaTemplate, times(1)).send(anyString(), anyString(), any());
    }
}
