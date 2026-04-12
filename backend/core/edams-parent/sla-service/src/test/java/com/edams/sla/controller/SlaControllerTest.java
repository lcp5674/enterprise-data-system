package com.edams.sla.controller;

import com.edams.common.model.ApiResponse;
import com.edams.common.model.PageResult;
import com.edams.sla.entity.SlaAgreement;
import com.edams.sla.entity.SlaReport;
import com.edams.sla.service.SlaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SlaController 单元测试")
class SlaControllerTest {

    @Mock
    private SlaService slaService;

    @InjectMocks
    private SlaController slaController;

    private SlaAgreement testAgreement;
    private SlaReport testReport;

    @BeforeEach
    void setUp() {
        testAgreement = new SlaAgreement();
        testAgreement.setId(1L);
        testAgreement.setName("API SLA Agreement");
        testAgreement.setServiceName("user-service");
        testAgreement.setServiceType("REST_API");
        testAgreement.setOwnerId(100L);
        testAgreement.setStatus("ACTIVE");
        testAgreement.setMetricType("AVAILABILITY");
        testAgreement.setTargetValue(99.9);
        testAgreement.setCreatedTime(LocalDateTime.now());

        testReport = new SlaReport();
        testReport.setId(1L);
        testReport.setAgreementId(1L);
        testReport.setReportPeriod("DAILY");
        testReport.setActualValue(99.95);
        testReport.setAnalysisResult("COMPLIANT");
        testReport.setGeneratedTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("创建SLA协议 - 成功")
    void createAgreement_Success() {
        when(slaService.createAgreement(any(SlaAgreement.class))).thenReturn(testAgreement);

        ResponseEntity<ApiResponse<SlaAgreement>> response = slaController.createAgreement(testAgreement);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("API SLA Agreement", response.getBody().getData().getName());
        assertEquals("ACTIVE", response.getBody().getData().getStatus());
        verify(slaService, times(1)).createAgreement(any(SlaAgreement.class));
    }

    @Test
    @DisplayName("更新SLA协议 - 成功")
    void updateAgreement_Success() {
        when(slaService.updateAgreement(eq(1L), any(SlaAgreement.class))).thenReturn(testAgreement);

        ResponseEntity<ApiResponse<SlaAgreement>> response = slaController.updateAgreement(1L, testAgreement);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("API SLA Agreement", response.getBody().getData().getName());
        verify(slaService, times(1)).updateAgreement(eq(1L), any(SlaAgreement.class));
    }

    @Test
    @DisplayName("删除SLA协议 - 成功")
    void deleteAgreement_Success() {
        doNothing().when(slaService).deleteAgreement(1L);

        ResponseEntity<ApiResponse<Void>> response = slaController.deleteAgreement(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        verify(slaService, times(1)).deleteAgreement(1L);
    }

    @Test
    @DisplayName("获取SLA协议详情 - 成功")
    void getAgreement_Success() {
        when(slaService.getAgreementById(1L)).thenReturn(testAgreement);

        ResponseEntity<ApiResponse<SlaAgreement>> response = slaController.getAgreement(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("API SLA Agreement", response.getBody().getData().getName());
        verify(slaService, times(1)).getAgreementById(1L);
    }

    @Test
    @DisplayName("分页查询SLA协议列表 - 成功")
    void listAgreements_Success() {
        PageResult<SlaAgreement> pageResult = new PageResult<>();
        pageResult.setTotal(1L);
        pageResult.setData(java.util.Collections.singletonList(testAgreement));

        when(slaService.listAgreements(anyMap())).thenReturn(pageResult);

        ResponseEntity<ApiResponse<PageResult<SlaAgreement>>> response = slaController.listAgreements(
                null, null, null, null, null, null, 1, 10, new String[]{"createdTime,desc"});

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getData().getTotal());
        verify(slaService, times(1)).listAgreements(anyMap());
    }

    @Test
    @DisplayName("监控SLA协议 - 成功")
    void monitorAgreement_Success() {
        doNothing().when(slaService).monitorSla(1L);

        ResponseEntity<ApiResponse<Void>> response = slaController.monitorAgreement(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        verify(slaService, times(1)).monitorSla(1L);
    }

    @Test
    @DisplayName("检查SLA合规性 - 成功")
    void checkCompliance_Success() {
        Map<String, Object> complianceResult = new HashMap<>();
        complianceResult.put("isCompliant", true);
        complianceResult.put("actualValue", 99.95);
        complianceResult.put("targetValue", 99.9);

        when(slaService.checkSlaCompliance(1L)).thenReturn(complianceResult);

        ResponseEntity<ApiResponse<Map<String, Object>>> response = slaController.checkCompliance(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(true, response.getBody().getData().get("isCompliant"));
        verify(slaService, times(1)).checkSlaCompliance(1L);
    }

    @Test
    @DisplayName("生成SLA报告 - 成功")
    void generateReport_Success() {
        doNothing().when(slaService).generateReport(eq(1L), anyString(), eq(100L));

        ResponseEntity<ApiResponse<Void>> response = slaController.generateReport(1L, "DAILY", 100L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        verify(slaService, times(1)).generateReport(eq(1L), eq("DAILY"), eq(100L));
    }

    @Test
    @DisplayName("获取SLA报告详情 - 成功")
    void getReport_Success() {
        when(slaService.getReportById(1L)).thenReturn(testReport);

        ResponseEntity<ApiResponse<SlaReport>> response = slaController.getReport(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("COMPLIANT", response.getBody().getData().getAnalysisResult());
        verify(slaService, times(1)).getReportById(1L);
    }

    @Test
    @DisplayName("获取服务SLA统计 - 成功")
    void getServiceStats_Success() {
        Map<String, Object> serviceStats = new HashMap<>();
        serviceStats.put("serviceName", "user-service");
        serviceStats.put("avgAvailability", 99.95);
        serviceStats.put("totalViolations", 0L);

        when(slaService.getServiceSlaStats("user-service")).thenReturn(serviceStats);

        ResponseEntity<ApiResponse<Map<String, Object>>> response =
                slaController.getServiceStats("user-service");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(99.95, response.getBody().getData().get("avgAvailability"));
        verify(slaService, times(1)).getServiceSlaStats("user-service");
    }

    @Test
    @DisplayName("检查SLA违规 - 成功")
    void checkViolations_Success() {
        doNothing().when(slaService).checkViolations();

        ResponseEntity<ApiResponse<Void>> response = slaController.checkViolations();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        verify(slaService, times(1)).checkViolations();
    }

    @Test
    @DisplayName("发送SLA违规警报 - 成功")
    void sendViolationAlert_Success() {
        doNothing().when(slaService).sendViolationAlert(1L);

        ResponseEntity<ApiResponse<Void>> response = slaController.sendViolationAlert(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        verify(slaService, times(1)).sendViolationAlert(1L);
    }
}
