package com.enterprise.dataplatform.ethics.service;

import com.enterprise.dataplatform.ethics.domain.dto.request.FairnessEvaluationRequest;
import com.enterprise.dataplatform.ethics.domain.dto.response.FairnessEvaluationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FairnessEvaluationServiceTest {

    @InjectMocks
    private FairnessEvaluationService fairnessService;

    private FairnessEvaluationRequest request;

    @BeforeEach
    void setUp() {
        Map<String, BigDecimal> groupMetrics = new HashMap<>();
        groupMetrics.put("group_A", BigDecimal.valueOf(0.85));
        groupMetrics.put("group_B", BigDecimal.valueOf(0.78));
        groupMetrics.put("group_C", BigDecimal.valueOf(0.82));
        groupMetrics.put("group_D", BigDecimal.valueOf(0.80));

        request = FairnessEvaluationRequest.builder()
                .assetId("ASSET-001")
                .assetName("招聘数据")
                .evaluationType("STANDARD")
                .dataGroups(createDataGroups())
                .groupMetrics(groupMetrics)
                .protectedAttributes(Arrays.asList("性别", "年龄", "地区"))
                .significanceLevel(0.05)
                .methodology("Statistical Significance Testing")
                .build();
    }

    private Map<String, java.util.List<String>> createDataGroups() {
        Map<String, java.util.List<String>> groups = new HashMap<>();
        groups.put("group_A", Arrays.asList("record1", "record2", "record3"));
        groups.put("group_B", Arrays.asList("record4", "record5"));
        groups.put("group_C", Arrays.asList("record6", "record7", "record8"));
        groups.put("group_D", Arrays.asList("record9", "record10"));
        return groups;
    }

    @Test
    void testEvaluateFairness_WithProvidedMetrics() {
        FairnessEvaluationResponse response = fairnessService.evaluateFairness(request);

        assertNotNull(response);
        assertEquals("ASSET-001", response.getAssetId());
        assertEquals("STANDARD", response.getEvaluationType());
        assertNotNull(response.getEvaluationId());
        assertNotNull(response.getOverallFairnessScore());
        assertNotNull(response.getRiskLevel());
        assertNotNull(response.getDetectedBiases());
        assertNotNull(response.getGroupDisparityScores());
        assertFalse(response.getRecommendations().isEmpty());
    }

    @Test
    void testEvaluateFairness_WithoutMetrics() {
        FairnessEvaluationRequest emptyRequest = FairnessEvaluationRequest.builder()
                .assetId("ASSET-002")
                .assetName("测试数据")
                .build();

        FairnessEvaluationResponse response = fairnessService.evaluateFairness(emptyRequest);

        assertNotNull(response);
        assertEquals("ASSET-002", response.getAssetId());
        assertNotNull(response.getOverallFairnessScore());
    }

    @Test
    void testDisparateImpactCalculation() {
        FairnessEvaluationResponse response = fairnessService.evaluateFairness(request);

        BigDecimal disparateImpact = response.getDisparateImpactRatio();
        assertNotNull(disparateImpact);
        assertTrue(disparateImpact.compareTo(BigDecimal.ZERO) > 0);
        assertTrue(disparateImpact.compareTo(BigDecimal.ONE) <= 0);
    }

    @Test
    void testStatisticalParityDifference() {
        FairnessEvaluationResponse response = fairnessService.evaluateFairness(request);

        BigDecimal spd = response.getStatisticalParityDifference();
        assertNotNull(spd);
    }

    @Test
    void testBiasDetection_SignificantDisparity() {
        Map<String, BigDecimal> biasedMetrics = new HashMap<>();
        biasedMetrics.put("group_A", BigDecimal.valueOf(0.95));
        biasedMetrics.put("group_B", BigDecimal.valueOf(0.50));
        biasedMetrics.put("group_C", BigDecimal.valueOf(0.90));

        FairnessEvaluationRequest biasedRequest = FairnessEvaluationRequest.builder()
                .assetId("ASSET-BIASED")
                .assetName("有偏见的数据")
                .groupMetrics(biasedMetrics)
                .significanceLevel(0.05)
                .build();

        FairnessEvaluationResponse response = fairnessService.evaluateFairness(biasedRequest);

        assertNotNull(response);
        assertFalse(response.getDetectedBiases().isEmpty());
    }

    @Test
    void testFairnessRiskLevel_Calculation() {
        FairnessEvaluationResponse response = fairnessService.evaluateFairness(request);

        String riskLevel = response.getRiskLevel();
        assertNotNull(riskLevel);
        assertTrue(Arrays.asList("LOW", "MEDIUM", "HIGH", "CRITICAL").contains(riskLevel));
    }

    @Test
    void testRecommendationsGeneration() {
        FairnessEvaluationResponse response = fairnessService.evaluateFairness(request);

        assertNotNull(response.getRecommendations());
        assertFalse(response.getRecommendations().isEmpty());
    }

    @Test
    void testPositiveIndicators() {
        FairnessEvaluationResponse response = fairnessService.evaluateFairness(request);

        assertNotNull(response.getPositiveIndicators());
    }

    @Test
    void testDetailedMetrics() {
        FairnessEvaluationResponse response = fairnessService.evaluateFairness(request);

        assertNotNull(response.getDetailedMetrics());
        assertTrue(response.getDetailedMetrics().containsKey("groupMetrics"));
        assertTrue(response.getDetailedMetrics().containsKey("groupCount"));
    }
}
