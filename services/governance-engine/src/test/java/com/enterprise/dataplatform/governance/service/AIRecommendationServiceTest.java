package com.enterprise.dataplatform.governance.service;

import com.enterprise.dataplatform.governance.domain.entity.AIRecommendation;
import com.enterprise.dataplatform.governance.domain.entity.GovernancePolicy;
import com.enterprise.dataplatform.governance.dto.request.AIRecommendationRequest;
import com.enterprise.dataplatform.governance.repository.AIRecommendationRepository;
import io.milvus.client.MilvusClient;
import io.milvus.param.dml.QueryParam;
import io.milvus.response.QueryResultsWrapper;
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
class AIRecommendationServiceTest {

    @Mock
    private AIRecommendationRepository recommendationRepository;

    @Mock
    private MilvusClient milvusClient;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private AIRecommendationService recommendationService;

    private AIRecommendation testRecommendation;
    private GovernancePolicy testPolicy;

    @BeforeEach
    void setUp() {
        testRecommendation = AIRecommendation.builder()
                .id(1L)
                .recommendationType("POLICY_SUGGESTION")
                .recommendationCode("REC001")
                .title("建议应用质量检测策略")
                .description("根据数据分析，建议对表T_SALES添加质量检测策略")
                .confidenceScore(0.95)
                .priority("HIGH")
                .targetAssetId("TABLE_001")
                .targetAssetType("TABLE")
                .recommendedAction(Map.of("action", "apply_quality_rule", "ruleId", 123))
                .status("GENERATED")
                .accepted(false)
                .applied(false)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        testPolicy = GovernancePolicy.builder()
                .id(1L)
                .name("质量检测策略")
                .policyType("QUALITY_CHECK")
                .assetType("TABLE")
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getRecommendations_shouldReturnRecommendations() {
        AIRecommendationRequest request = AIRecommendationRequest.builder()
                .recommendationType("POLICY_SUGGESTION")
                .domain("SALES")
                .limitResults(5)
                .build();

        when(recommendationRepository.findByTypeAndStatus(
                eq("POLICY_SUGGESTION"), eq("GENERATED"), any()))
                .thenReturn(List.of(testRecommendation));

        List<AIRecommendation> results = recommendationService.getRecommendations(request);

        assertNotNull(results);
        assertEquals(1, results.size());
    }

    @Test
    void generatePolicyRecommendation_shouldGenerateSuccessfully() {
        when(recommendationRepository.save(any(AIRecommendation.class)))
                .thenReturn(testRecommendation);

        AIRecommendation result = recommendationService.generatePolicyRecommendation(
                "TABLE", "SALES", "TABLE_001");

        assertNotNull(result);
        verify(recommendationRepository, times(1)).save(any(AIRecommendation.class));
    }

    @Test
    void generateQualityImprovementRecommendation_shouldGenerateSuccessfully() {
        when(recommendationRepository.save(any(AIRecommendation.class)))
                .thenReturn(testRecommendation);

        AIRecommendation result = recommendationService.generateQualityImprovementRecommendation(
                "TABLE_001", "COMPLETENESS", Map.of("score", 0.85));

        assertNotNull(result);
        verify(recommendationRepository, times(1)).save(any(AIRecommendation.class));
    }

    @Test
    void generateStandardMappingRecommendation_shouldGenerateSuccessfully() {
        when(recommendationRepository.save(any(AIRecommendation.class)))
                .thenReturn(testRecommendation);

        AIRecommendation result = recommendationService.generateStandardMappingRecommendation(
                "COL_001", "NAMING_STANDARD");

        assertNotNull(result);
        verify(recommendationRepository, times(1)).save(any(AIRecommendation.class));
    }

    @Test
    void generateLineageImpactRecommendation_shouldGenerateSuccessfully() {
        when(recommendationRepository.save(any(AIRecommendation.class)))
                .thenReturn(testRecommendation);

        AIRecommendation result = recommendationService.generateLineageImpactRecommendation(
                "TABLE_001", "SCHEMA_CHANGE");

        assertNotNull(result);
        verify(recommendationRepository, times(1)).save(any(AIRecommendation.class));
    }

    @Test
    void searchRecommendationsInVectorDb_shouldQuerySuccessfully() {
        QueryResultsWrapper wrapper = mock(QueryResultsWrapper.class);
        when(milvusClient.query(any(QueryParam.class))).thenReturn(wrapper);
        when(wrapper.getResultFields()).thenReturn(Collections.emptyList());

        List<List<Float>> result = recommendationService.searchRecommendationsInVectorDb(
                "测试上下文", 5);

        assertNotNull(result);
    }

    @Test
    void acceptRecommendation_shouldAcceptSuccessfully() {
        when(recommendationRepository.findById(1L)).thenReturn(Optional.of(testRecommendation));
        when(recommendationRepository.save(any(AIRecommendation.class)))
                .thenReturn(testRecommendation);

        Optional<AIRecommendation> result = recommendationService.acceptRecommendation(
                1L, "user123");

        assertTrue(result.isPresent());
        verify(recommendationRepository, times(1)).save(any(AIRecommendation.class));
    }

    @Test
    void acceptRecommendation_shouldReturnEmptyWhenNotFound() {
        when(recommendationRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<AIRecommendation> result = recommendationService.acceptRecommendation(
                99L, "user123");

        assertFalse(result.isPresent());
    }

    @Test
    void rejectRecommendation_shouldRejectSuccessfully() {
        when(recommendationRepository.findById(1L)).thenReturn(Optional.of(testRecommendation));
        when(recommendationRepository.save(any(AIRecommendation.class)))
                .thenReturn(testRecommendation);

        assertDoesNotThrow(() -> recommendationService.rejectRecommendation(
                1L, "不适用当前场景"));

        verify(recommendationRepository, times(1)).save(any(AIRecommendation.class));
    }

    @Test
    void applyRecommendation_shouldApplySuccessfully() {
        testRecommendation.setAccepted(true);
        when(recommendationRepository.findById(1L)).thenReturn(Optional.of(testRecommendation));
        when(recommendationRepository.save(any(AIRecommendation.class)))
                .thenReturn(testRecommendation);

        Map<String, Object> result = recommendationService.applyRecommendation(
                1L, "user123");

        assertNotNull(result);
        assertTrue(result.containsKey("success"));
    }

    @Test
    void recommendPoliciesForAsset_shouldReturnPolicies() {
        when(recommendationRepository.findByTypeAndStatus(
                eq("POLICY_SUGGESTION"), any(), any()))
                .thenReturn(List.of(testRecommendation));

        List<GovernancePolicy> results = recommendationService.recommendPoliciesForAsset(
                "TABLE", "SALES", "TABLE_001");

        assertNotNull(results);
    }

    @Test
    void getRecommendationHistory_shouldReturnHistory() {
        when(recommendationRepository.findByTypeOrderByCreatedAtDesc(
                any(), any(), any()))
                .thenReturn(List.of(testRecommendation));

        List<AIRecommendation> results = recommendationService.getRecommendationHistory(
                "POLICY_SUGGESTION", "GENERATED", 50);

        assertNotNull(results);
    }

    @Test
    void generateGovernanceSummary_shouldGenerateSummary() {
        Map<String, Object> summary = recommendationService.generateGovernanceSummary(
                "SALES", "weekly");

        assertNotNull(summary);
        assertTrue(summary.containsKey("totalRecommendations"));
        assertTrue(summary.containsKey("acceptedRecommendations"));
    }

    @Test
    void cleanupExpiredRecommendations_shouldDeleteExpired() {
        when(recommendationRepository.deleteByExpiresAtBefore(any(LocalDateTime.class)))
                .thenReturn(5);

        int deleted = recommendationService.cleanupExpiredRecommendations();

        assertEquals(5, deleted);
    }
}
