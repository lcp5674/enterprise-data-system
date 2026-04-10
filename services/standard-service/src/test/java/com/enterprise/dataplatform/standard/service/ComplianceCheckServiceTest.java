package com.enterprise.dataplatform.standard.service;

import com.enterprise.dataplatform.standard.domain.entity.ComplianceCheck;
import com.enterprise.dataplatform.standard.domain.entity.StandardMapping;
import com.enterprise.dataplatform.standard.dto.request.ComplianceCheckRequest;
import com.enterprise.dataplatform.standard.repository.ComplianceCheckRepository;
import com.enterprise.dataplatform.standard.repository.StandardMappingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComplianceCheckServiceTest {

    @Mock
    private ComplianceCheckRepository checkRepository;

    @Mock
    private StandardMappingRepository mappingRepository;

    @InjectMocks
    private ComplianceCheckService complianceCheckService;

    private ComplianceCheck testCheck;
    private StandardMapping testMapping;

    @BeforeEach
    void setUp() {
        testCheck = ComplianceCheck.builder()
                .id(1L)
                .assetId("TABLE_001")
                .assetType("TABLE")
                .standardId(1L)
                .mappingId(1L)
                .checkType("FORMAT")
                .checkStatus("PASSED")
                .checkScore(100)
                .violations("[]")
                .checkedAt(LocalDateTime.now())
                .build();

        testMapping = StandardMapping.builder()
                .id(1L)
                .assetId("COL_001")
                .assetType("COLUMN")
                .standardId(1L)
                .mappingStatus("MAPPED")
                .build();
    }

    @Test
    void checkCompliance_shouldReturnPassResult() {
        ComplianceCheckRequest request = ComplianceCheckRequest.builder()
                .assetId("TABLE_001")
                .assetType("TABLE")
                .standardId(1L)
                .checkType("FORMAT")
                .build();

        when(mappingRepository.findByAssetIdAndStandardId("TABLE_001", 1L))
                .thenReturn(Optional.of(testMapping));
        when(checkRepository.save(any(ComplianceCheck.class))).thenReturn(testCheck);

        ComplianceCheck result = complianceCheckService.checkCompliance(request);

        assertNotNull(result);
        verify(checkRepository, times(1)).save(any(ComplianceCheck.class));
    }

    @Test
    void checkCompliance_shouldReturnFailResult() {
        testCheck.setCheckStatus("FAILED");
        testCheck.setCheckScore(60);
        testCheck.setViolations("[{\"field\":\"customer_id\",\"issue\":\"格式不符\"}]");

        ComplianceCheckRequest request = ComplianceCheckRequest.builder()
                .assetId("TABLE_001")
                .assetType("TABLE")
                .standardId(1L)
                .checkType("FORMAT")
                .build();

        when(mappingRepository.findByAssetIdAndStandardId("TABLE_001", 1L))
                .thenReturn(Optional.of(testMapping));
        when(checkRepository.save(any(ComplianceCheck.class))).thenReturn(testCheck);

        ComplianceCheck result = complianceCheckService.checkCompliance(request);

        assertNotNull(result);
        assertEquals("FAILED", result.getCheckStatus());
    }

    @Test
    void batchCheckCompliance_shouldCheckMultipleAssets() {
        ComplianceCheckRequest request = ComplianceCheckRequest.builder()
                .assetIds(List.of("TABLE_001", "TABLE_002"))
                .assetType("TABLE")
                .standardId(1L)
                .checkType("FORMAT")
                .build();

        when(checkRepository.save(any(ComplianceCheck.class))).thenReturn(testCheck);

        List<ComplianceCheck> results = complianceCheckService.batchCheckCompliance(request);

        assertNotNull(results);
        verify(checkRepository, times(2)).save(any(ComplianceCheck.class));
    }

    @Test
    void getComplianceHistory_shouldReturnHistory() {
        when(checkRepository.findByAssetIdOrderByCheckedAtDesc("TABLE_001"))
                .thenReturn(List.of(testCheck));

        List<ComplianceCheck> results = complianceCheckService.getComplianceHistory("TABLE_001");

        assertNotNull(results);
        assertEquals(1, results.size());
    }

    @Test
    void getComplianceRate_shouldCalculateCorrectly() {
        when(checkRepository.countByAssetIdAndCheckStatus("TABLE_001", "PASSED")).thenReturn(8L);
        when(checkRepository.countByAssetId("TABLE_001")).thenReturn(10L);

        Map<String, Object> rate = complianceCheckService.getComplianceRate("TABLE_001");

        assertNotNull(rate);
        assertEquals(80.0, rate.get("complianceRate"));
    }

    @Test
    void getViolations_shouldReturnViolationList() {
        testCheck.setViolations("[{\"field\":\"customer_id\",\"issue\":\"格式不符\"}]");
        when(checkRepository.findById(1L)).thenReturn(Optional.of(testCheck));

        List<Map<String, Object>> violations = complianceCheckService.getViolations(1L);

        assertNotNull(violations);
        assertEquals(1, violations.size());
    }

    @Test
    void getStandardsByAsset_shouldReturnMappedStandards() {
        when(mappingRepository.findByAssetId("COL_001")).thenReturn(List.of(testMapping));

        List<StandardMapping> results = complianceCheckService.getStandardsByAsset("COL_001");

        assertNotNull(results);
        assertEquals(1, results.size());
    }
}
