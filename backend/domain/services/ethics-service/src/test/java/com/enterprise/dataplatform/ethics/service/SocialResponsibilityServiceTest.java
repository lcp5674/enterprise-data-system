package com.enterprise.dataplatform.ethics.service;

import com.enterprise.dataplatform.ethics.domain.dto.request.SocialImpactRequest;
import com.enterprise.dataplatform.ethics.domain.dto.response.SocialImpactResponse;
import com.enterprise.dataplatform.ethics.domain.entity.SocialImpactReport;
import com.enterprise.dataplatform.ethics.domain.enums.RiskLevel;
import com.enterprise.dataplatform.ethics.repository.SocialImpactReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SocialResponsibilityServiceTest {

    @Mock
    private SocialImpactReportRepository reportRepository;

    @InjectMocks
    private SocialResponsibilityService socialService;

    private SocialImpactRequest request;
    private SocialImpactReport report;

    @BeforeEach
    void setUp() {
        request = SocialImpactRequest.builder()
                .assetId("ASSET-001")
                .assetName("用户数据平台")
                .reportPeriod("2024-Q1")
                .dataUsageImpactScore(BigDecimal.valueOf(3.8))
                .stakeholderScore(BigDecimal.valueOf(4.0))
                .socialValueScore(BigDecimal.valueOf(3.5))
                .communityBenefitScore(BigDecimal.valueOf(3.2))
                .affectedStakeholders(Arrays.asList("客户", "员工", "合作伙伴"))
                .notes("季度评估")
                .build();

        report = SocialImpactReport.builder()
                .id(1L)
                .reportCode("SIR-TEST001")
                .assetId("ASSET-001")
                .assetName("用户数据平台")
                .reportDate(LocalDateTime.now())
                .reportPeriod("2024-Q1")
                .overallRisk(RiskLevel.MEDIUM)
                .overallImpactScore(BigDecimal.valueOf(3.63))
                .dataUsageImpactScore(BigDecimal.valueOf(3.8))
                .stakeholderScore(BigDecimal.valueOf(4.0))
                .socialValueScore(BigDecimal.valueOf(3.5))
                .communityBenefitScore(BigDecimal.valueOf(3.2))
                .positiveImpacts(Arrays.asList("数据使用透明合规"))
                .negativeImpacts(Arrays.asList())
                .affectedStakeholders(Arrays.asList("客户", "员工", "合作伙伴"))
                .analyst("admin")
                .status("DRAFT")
                .approvalStatus("PENDING")
                .build();
    }

    @Test
    void testGenerateSocialImpactReport_Success() {
        when(reportRepository.save(any(SocialImpactReport.class))).thenReturn(report);

        SocialImpactResponse response = socialService.generateSocialImpactReport(request, "admin");

        assertNotNull(response);
        assertEquals("ASSET-001", response.getAssetId());
        assertEquals("SIR-TEST001", response.getReportCode());
        assertNotNull(response.getOverallImpactScore());
        assertNotNull(response.getOverallRisk());
        assertFalse(response.getRecommendations().isEmpty());

        verify(reportRepository).save(any(SocialImpactReport.class));
    }

    @Test
    void testGenerateSocialImpactReport_WithoutProvidedScores() {
        SocialImpactRequest emptyRequest = SocialImpactRequest.builder()
                .assetId("ASSET-002")
                .assetName("测试数据")
                .build();

        when(reportRepository.save(any(SocialImpactReport.class))).thenReturn(report);

        SocialImpactResponse response = socialService.generateSocialImpactReport(emptyRequest, "admin");

        assertNotNull(response);
        assertEquals("ASSET-002", response.getAssetId());
    }

    @Test
    void testGetReport_Success() {
        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));

        SocialImpactResponse response = socialService.getReport(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("SIR-TEST001", response.getReportCode());
    }

    @Test
    void testGetReport_NotFound() {
        when(reportRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                socialService.getReport(999L));
    }

    @Test
    void testApproveReport_Success() {
        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));
        when(reportRepository.save(any(SocialImpactReport.class))).thenAnswer(invocation -> {
            SocialImpactReport saved = invocation.getArgument(0);
            saved.setApprovalStatus("APPROVED");
            saved.setApprovedBy("approver");
            return saved;
        });

        SocialImpactResponse response = socialService.approveReport(1L, "approver");

        assertNotNull(response);
        assertEquals("APPROVED", response.getApprovalStatus());
        assertEquals("APPROVED", response.getStatus());
        assertEquals("approver", response.getApprovedBy());
        assertNotNull(response.getApprovedAt());
    }

    @Test
    void testOverallImpactScoreCalculation() {
        when(reportRepository.save(any(SocialImpactReport.class))).thenReturn(report);

        SocialImpactResponse response = socialService.generateSocialImpactReport(request, "admin");

        BigDecimal overallScore = response.getOverallImpactScore();
        assertNotNull(overallScore);
        assertTrue(overallScore.compareTo(BigDecimal.ZERO) > 0);
        assertTrue(overallScore.compareTo(BigDecimal.valueOf(5.0)) <= 0);
    }

    @Test
    void testRiskLevelCalculation() {
        when(reportRepository.save(any(SocialImpactReport.class))).thenReturn(report);

        SocialImpactResponse response = socialService.generateSocialImpactReport(request, "admin");

        String riskLevel = response.getOverallRisk();
        assertNotNull(riskLevel);
        assertTrue(Arrays.asList("LOW", "MEDIUM", "HIGH", "CRITICAL").contains(riskLevel));
    }

    @Test
    void testPositiveImpactsAnalysis() {
        when(reportRepository.save(any(SocialImpactReport.class))).thenReturn(report);

        SocialImpactResponse response = socialService.generateSocialImpactReport(request, "admin");

        assertNotNull(response.getPositiveImpacts());
    }

    @Test
    void testNegativeImpactsAnalysis() {
        SocialImpactRequest negativeRequest = SocialImpactRequest.builder()
                .assetId("ASSET-003")
                .assetName("高风险数据")
                .dataUsageImpactScore(BigDecimal.valueOf(2.0))
                .stakeholderScore(BigDecimal.valueOf(2.2))
                .socialValueScore(BigDecimal.valueOf(1.8))
                .communityBenefitScore(BigDecimal.valueOf(2.1))
                .build();

        when(reportRepository.save(any(SocialImpactReport.class))).thenReturn(report);

        SocialImpactResponse response = socialService.generateSocialImpactReport(negativeRequest, "admin");

        assertNotNull(response);
    }
}
