package com.edams.value.controller;

import com.edams.common.model.ApiResponse;
import com.edams.common.model.PageResult;
import com.edams.value.entity.DataValue;
import com.edams.value.entity.ValueMetric;
import com.edams.value.service.ValueService;
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
@DisplayName("ValueController 单元测试")
class ValueControllerTest {

    @Mock
    private ValueService valueService;

    @InjectMocks
    private ValueController valueController;

    private DataValue testValue;
    private ValueMetric testMetric;

    @BeforeEach
    void setUp() {
        testValue = new DataValue();
        testValue.setId(1L);
        testValue.setAssetId(100L);
        testValue.setAssetType("DATABASE_TABLE");
        testValue.setValueCategory("BUSINESS");
        testValue.setAssessorId(200L);
        testValue.setOverallScore(85.5);
        testValue.setStatus("COMPLETED");
        testValue.setAssessmentDate(LocalDateTime.now());

        testMetric = new ValueMetric();
        testMetric.setId(1L);
        testMetric.setMetricName("Usage Frequency");
        testMetric.setMetricType("USAGE");
        testMetric.setWeight(0.3);
        testMetric.setStatus("ACTIVE");
    }

    @Test
    @DisplayName("评估数据资产价值 - 成功")
    void assessDataValue_Success() {
        when(valueService.assessDataValue(eq(100L), eq(200L))).thenReturn(testValue);

        ResponseEntity<ApiResponse<DataValue>> response =
                valueController.assessDataValue(100L, 200L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(85.5, response.getBody().getData().getOverallScore());
        verify(valueService, times(1)).assessDataValue(eq(100L), eq(200L));
    }

    @Test
    @DisplayName("获取价值评估详情 - 成功")
    void getValue_Success() {
        when(valueService.getValueById(1L)).thenReturn(testValue);

        ResponseEntity<ApiResponse<DataValue>> response = valueController.getValue(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(85.5, response.getBody().getData().getOverallScore());
        verify(valueService, times(1)).getValueById(1L);
    }

    @Test
    @DisplayName("分页查询价值评估列表 - 成功")
    void listValues_Success() {
        PageResult<DataValue> pageResult = new PageResult<>();
        pageResult.setTotal(1L);
        pageResult.setData(java.util.Collections.singletonList(testValue));

        when(valueService.listValues(anyMap())).thenReturn(pageResult);

        ResponseEntity<ApiResponse<PageResult<DataValue>>> response = valueController.listValues(
                null, null, null, null, null, null, null, 1, 10, new String[]{"assessmentDate,desc"});

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getData().getTotal());
        verify(valueService, times(1)).listValues(anyMap());
    }

    @Test
    @DisplayName("更新价值评估 - 成功")
    void updateValue_Success() {
        when(valueService.updateValue(eq(1L), any(DataValue.class))).thenReturn(testValue);

        ResponseEntity<ApiResponse<DataValue>> response = valueController.updateValue(1L, testValue);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(85.5, response.getBody().getData().getOverallScore());
        verify(valueService, times(1)).updateValue(eq(1L), any(DataValue.class));
    }

    @Test
    @DisplayName("删除价值评估 - 成功")
    void deleteValue_Success() {
        doNothing().when(valueService).deleteValue(1L);

        ResponseEntity<ApiResponse<Void>> response = valueController.deleteValue(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        verify(valueService, times(1)).deleteValue(1L);
    }

    @Test
    @DisplayName("创建价值度量标准 - 成功")
    void createMetric_Success() {
        when(valueService.createMetric(any(ValueMetric.class))).thenReturn(testMetric);

        ResponseEntity<ApiResponse<ValueMetric>> response = valueController.createMetric(testMetric);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Usage Frequency", response.getBody().getData().getMetricName());
        verify(valueService, times(1)).createMetric(any(ValueMetric.class));
    }

    @Test
    @DisplayName("更新价值度量标准 - 成功")
    void updateMetric_Success() {
        when(valueService.updateMetric(eq(1L), any(ValueMetric.class))).thenReturn(testMetric);

        ResponseEntity<ApiResponse<ValueMetric>> response = valueController.updateMetric(1L, testMetric);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Usage Frequency", response.getBody().getData().getMetricName());
        verify(valueService, times(1)).updateMetric(eq(1L), any(ValueMetric.class));
    }

    @Test
    @DisplayName("删除价值度量标准 - 成功")
    void deleteMetric_Success() {
        doNothing().when(valueService).deleteMetric(1L);

        ResponseEntity<ApiResponse<Void>> response = valueController.deleteMetric(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        verify(valueService, times(1)).deleteMetric(1L);
    }

    @Test
    @DisplayName("获取价值度量标准详情 - 成功")
    void getMetric_Success() {
        when(valueService.getMetricById(1L)).thenReturn(testMetric);

        ResponseEntity<ApiResponse<ValueMetric>> response = valueController.getMetric(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Usage Frequency", response.getBody().getData().getMetricName());
        verify(valueService, times(1)).getMetricById(1L);
    }

    @Test
    @DisplayName("分析资产价值 - 成功")
    void analyzeAssetValue_Success() {
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("overallScore", 85.5);
        analysis.put("dimensions", Map.of("business", 90.0, "technical", 80.0));

        when(valueService.analyzeAssetValue(100L)).thenReturn(analysis);

        ResponseEntity<ApiResponse<Map<String, Object>>> response = valueController.analyzeAssetValue(100L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(85.5, response.getBody().getData().get("overallScore"));
        verify(valueService, times(1)).analyzeAssetValue(100L);
    }

    @Test
    @DisplayName("分析价值趋势 - 成功")
    void analyzeTrend_Success() {
        Map<String, Object> trend = new HashMap<>();
        trend.put("currentScore", 85.5);
        trend.put("previousScore", 82.0);
        trend.put("changeRate", 4.27);

        when(valueService.analyzeTrend(100L)).thenReturn(trend);

        ResponseEntity<ApiResponse<Map<String, Object>>> response = valueController.analyzeTrend(100L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(85.5, response.getBody().getData().get("currentScore"));
        verify(valueService, times(1)).analyzeTrend(100L);
    }

    @Test
    @DisplayName("比较资产价值 - 成功")
    void compareValues_Success() {
        Map<String, Object> comparison = new HashMap<>();
        comparison.put("asset1Score", 85.5);
        comparison.put("asset2Score", 78.0);
        comparison.put("difference", 7.5);

        when(valueService.compareValues(eq(100L), eq(200L))).thenReturn(comparison);

        ResponseEntity<ApiResponse<Map<String, Object>>> response =
                valueController.compareValues(100L, 200L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(7.5, response.getBody().getData().get("difference"));
        verify(valueService, times(1)).compareValues(eq(100L), eq(200L));
    }

    @Test
    @DisplayName("预测未来价值 - 成功")
    void predictFutureValue_Success() {
        Map<String, Object> prediction = new HashMap<>();
        prediction.put("currentScore", 85.5);
        prediction.put("predictedScore3Months", 88.0);
        prediction.put("confidence", 0.85);

        when(valueService.predictFutureValue(100L)).thenReturn(prediction);

        ResponseEntity<ApiResponse<Map<String, Object>>> response = valueController.predictFutureValue(100L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(88.0, response.getBody().getData().get("predictedScore3Months"));
        verify(valueService, times(1)).predictFutureValue(100L);
    }

    @Test
    @DisplayName("计算价值趋势 - 成功")
    void calculateTrends_Success() {
        doNothing().when(valueService).calculateValueTrends();

        ResponseEntity<ApiResponse<Void>> response = valueController.calculateTrends();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        verify(valueService, times(1)).calculateValueTrends();
    }

    @Test
    @DisplayName("生成价值报告 - 成功")
    void generateValueReport_Success() {
        Map<String, Object> report = new HashMap<>();
        report.put("assetId", 100L);
        report.put("overallScore", 85.5);
        report.put("recommendations", java.util.Collections.emptyList());

        when(valueService.generateValueReport(100L)).thenReturn(report);

        ResponseEntity<ApiResponse<Map<String, Object>>> response = valueController.generateValueReport(100L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(85.5, response.getBody().getData().get("overallScore"));
        verify(valueService, times(1)).generateValueReport(100L);
    }
}
