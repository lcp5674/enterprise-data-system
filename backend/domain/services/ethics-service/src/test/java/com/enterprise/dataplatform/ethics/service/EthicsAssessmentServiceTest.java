package com.enterprise.dataplatform.ethics.service;

import com.enterprise.dataplatform.ethics.domain.dto.request.EthicsAssessmentRequest;
import com.enterprise.dataplatform.ethics.domain.dto.response.EthicsAssessmentResponse;
import com.enterprise.dataplatform.ethics.domain.entity.EthicsAssessment;
import com.enterprise.dataplatform.ethics.domain.entity.EthicsFramework;
import com.enterprise.dataplatform.ethics.domain.enums.EthicsScore;
import com.enterprise.dataplatform.ethics.domain.enums.RiskLevel;
import com.enterprise.dataplatform.ethics.repository.EthicsAssessmentRepository;
import com.enterprise.dataplatform.ethics.repository.EthicsFrameworkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EthicsAssessmentServiceTest {

    @Mock
    private EthicsAssessmentRepository assessmentRepository;

    @Mock
    private EthicsFrameworkRepository frameworkRepository;

    @InjectMocks
    private EthicsAssessmentService assessmentService;

    private EthicsAssessmentRequest request;
    private EthicsAssessment assessment;
    private EthicsFramework framework;

    @BeforeEach
    void setUp() {
        request = EthicsAssessmentRequest.builder()
                .assetId("ASSET-001")
                .assetName("用户数据表")
                .frameworkId(1L)
                .frameworkName("ESG数据伦理框架")
                .transparencyValue(BigDecimal.valueOf(4.2))
                .fairnessValue(BigDecimal.valueOf(3.8))
                .accountabilityValue(BigDecimal.valueOf(4.0))
                .privacyValue(BigDecimal.valueOf(3.5))
                .methodology("AUTOMATED")
                .notes("测试评估")
                .build();

        framework = EthicsFramework.builder()
                .id(1L)
                .frameworkCode("EF-001")
                .frameworkName("ESG数据伦理框架")
                .build();

        assessment = EthicsAssessment.builder()
                .id(1L)
                .assessmentCode("EA-TEST001")
                .assetId("ASSET-001")
                .assetName("用户数据表")
                .frameworkId(1L)
                .frameworkName("ESG数据伦理框架")
                .transparencyScore(EthicsScore.GOOD)
                .transparencyValue(BigDecimal.valueOf(4.2))
                .fairnessScore(EthicsScore.GOOD)
                .fairnessValue(BigDecimal.valueOf(3.8))
                .accountabilityScore(EthicsScore.GOOD)
                .accountabilityValue(BigDecimal.valueOf(4.0))
                .privacyScore(EthicsScore.FAIR)
                .privacyValue(BigDecimal.valueOf(3.5))
                .overallScore(BigDecimal.valueOf(3.88))
                .overallRisk(RiskLevel.MEDIUM)
                .methodology("AUTOMATED")
                .assessor("admin")
                .status("COMPLETED")
                .build();
    }

    @Test
    void testCreateAssessment_Success() {
        when(frameworkRepository.findById(1L)).thenReturn(Optional.of(framework));
        when(assessmentRepository.save(any(EthicsAssessment.class))).thenReturn(assessment);

        EthicsAssessmentResponse response = assessmentService.createAssessment(request, "admin");

        assertNotNull(response);
        assertEquals("ASSET-001", response.getAssetId());
        assertEquals("EA-TEST001", response.getAssessmentCode());
        assertNotNull(response.getOverallScore());
        assertNotNull(response.getOverallRisk());
        assertFalse(response.getRecommendations().isEmpty());

        verify(assessmentRepository).save(any(EthicsAssessment.class));
    }

    @Test
    void testCreateAssessment_WithoutFramework() {
        request.setFrameworkId(null);
        when(assessmentRepository.save(any(EthicsAssessment.class))).thenReturn(assessment);

        EthicsAssessmentResponse response = assessmentService.createAssessment(request, "admin");

        assertNotNull(response);
        assertEquals("ASSET-001", response.getAssetId());
        verify(frameworkRepository, never()).findById(any());
    }

    @Test
    void testGetAssessment_Success() {
        when(assessmentRepository.findById(1L)).thenReturn(Optional.of(assessment));

        EthicsAssessmentResponse response = assessmentService.getAssessment(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("EA-TEST001", response.getAssessmentCode());
    }

    @Test
    void testGetAssessment_NotFound() {
        when(assessmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                assessmentService.getAssessment(999L));
    }

    @Test
    void testGetAssessmentByCode_Success() {
        when(assessmentRepository.findByAssessmentCode("EA-TEST001")).thenReturn(Optional.of(assessment));

        EthicsAssessmentResponse response = assessmentService.getAssessmentByCode("EA-TEST001");

        assertNotNull(response);
        assertEquals("EA-TEST001", response.getAssessmentCode());
    }

    @Test
    void testCalculateOverallScore() {
        BigDecimal transparency = BigDecimal.valueOf(4.0);
        BigDecimal fairness = BigDecimal.valueOf(3.5);
        BigDecimal accountability = BigDecimal.valueOf(4.5);
        BigDecimal privacy = BigDecimal.valueOf(3.0);

        BigDecimal overallScore = assessmentService.createAssessment(request, "admin").getOverallScore();

        assertNotNull(overallScore);
        assertTrue(overallScore.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void testGenerateRecommendations_HighRisk() {
        EthicsAssessmentRequest highRiskRequest = EthicsAssessmentRequest.builder()
                .assetId("ASSET-002")
                .transparencyValue(BigDecimal.valueOf(1.5))
                .fairnessValue(BigDecimal.valueOf(1.8))
                .accountabilityValue(BigDecimal.valueOf(2.0))
                .privacyValue(BigDecimal.valueOf(1.2))
                .build();

        when(assessmentRepository.save(any(EthicsAssessment.class))).thenAnswer(invocation -> {
            EthicsAssessment saved = invocation.getArgument(0);
            return saved;
        });

        EthicsAssessmentResponse response = assessmentService.createAssessment(highRiskRequest, "admin");

        assertNotNull(response);
        assertTrue(response.getRecommendations().stream()
                .anyMatch(r -> r.contains("高风险") || r.contains("立即")));
    }
}
