package com.edams.watermark.controller;

import com.edams.common.model.ApiResponse;
import com.edams.common.model.PageResult;
import com.edams.watermark.dto.WatermarkDTO;
import com.edams.watermark.dto.WatermarkQueryRequest;
import com.edams.watermark.service.WatermarkService;
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
@DisplayName("WatermarkController 单元测试")
class WatermarkControllerTest {

    @Mock
    private WatermarkService watermarkService;

    @InjectMocks
    private WatermarkController watermarkController;

    private WatermarkDTO testWatermark;

    @BeforeEach
    void setUp() {
        testWatermark = new WatermarkDTO();
        testWatermark.setId(1L);
        testWatermark.setAssetId(100L);
        testWatermark.setAssetType("DATABASE_TABLE");
        testWatermark.setWatermarkType("VISIBLE");
        testWatermark.setWatermarkContent("CONFIDENTIAL");
        testWatermark.setStatus("ACTIVE");
        testWatermark.setCreatedBy("admin");
        testWatermark.setCreatedTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("创建数字水印 - 成功")
    void createWatermark_Success() {
        when(watermarkService.createWatermark(any())).thenReturn(testWatermark);

        ResponseEntity<ApiResponse<WatermarkDTO>> response =
                watermarkController.createWatermark(new HashMap<>());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ACTIVE", response.getBody().getData().getStatus());
        verify(watermarkService, times(1)).createWatermark(any());
    }

    @Test
    @DisplayName("获取水印详情 - 成功")
    void getById_Success() {
        when(watermarkService.getById(1L)).thenReturn(testWatermark);

        ResponseEntity<ApiResponse<WatermarkDTO>> response = watermarkController.getById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("CONFIDENTIAL", response.getBody().getData().getWatermarkContent());
        verify(watermarkService, times(1)).getById(1L);
    }

    @Test
    @DisplayName("分页查询水印列表 - 成功")
    void list_Success() {
        PageResult<WatermarkDTO> pageResult = new PageResult<>();
        pageResult.setTotal(1L);
        pageResult.setData(java.util.Collections.singletonList(testWatermark));

        when(watermarkService.list(any())).thenReturn(pageResult);

        ResponseEntity<ApiResponse<PageResult<WatermarkDTO>>> response =
                watermarkController.list(new WatermarkQueryRequest());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getData().getTotal());
        verify(watermarkService, times(1)).list(any());
    }

    @Test
    @DisplayName("更新水印 - 成功")
    void update_Success() {
        when(watermarkService.update(eq(1L), any())).thenReturn(testWatermark);

        ResponseEntity<ApiResponse<WatermarkDTO>> response =
                watermarkController.update(1L, new HashMap<>());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        verify(watermarkService, times(1)).update(eq(1L), any());
    }

    @Test
    @DisplayName("删除水印 - 成功")
    void delete_Success() {
        doNothing().when(watermarkService).delete(1L);

        ResponseEntity<ApiResponse<Void>> response = watermarkController.delete(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        verify(watermarkService, times(1)).delete(1L);
    }

    @Test
    @DisplayName("资产溯源 - 成功")
    void traceAsset_Success() {
        Map<String, Object> traceResult = new HashMap<>();
        traceResult.put("assetId", 100L);
        traceResult.put("watermarkFound", true);
        traceResult.put("watermarkType", "VISIBLE");

        when(watermarkService.traceAsset(eq(100L), anyString())).thenReturn(traceResult);

        ResponseEntity<ApiResponse<Map<String, Object>>> response =
                watermarkController.traceAsset(100L, "admin");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(true, response.getBody().getData().get("watermarkFound"));
        verify(watermarkService, times(1)).traceAsset(eq(100L), anyString());
    }

    @Test
    @DisplayName("提取水印 - 成功")
    void extractWatermark_Success() {
        Map<String, Object> extractResult = new HashMap<>();
        extractResult.put("extractedContent", "CONFIDENTIAL");
        extractResult.put("confidence", 0.95);

        when(watermarkService.extractWatermark(anyString())).thenReturn(extractResult);

        ResponseEntity<ApiResponse<Map<String, Object>>> response =
                watermarkController.extractWatermark("test-content");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("CONFIDENTIAL", response.getBody().getData().get("extractedContent"));
        verify(watermarkService, times(1)).extractWatermark(anyString());
    }

    @Test
    @DisplayName("获取水印统计 - 成功")
    void getStatistics_Success() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalWatermarks", 50L);
        stats.put("activeWatermarks", 45L);

        when(watermarkService.getStatistics()).thenReturn(stats);

        ResponseEntity<ApiResponse<Map<String, Object>>> response = watermarkController.getStatistics();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(50L, response.getBody().getData().get("totalWatermarks"));
        verify(watermarkService, times(1)).getStatistics();
    }

    @Test
    @DisplayName("批量生成水印 - 成功")
    void batchCreate_Success() {
        java.util.List<WatermarkDTO> watermarks = java.util.Collections.singletonList(testWatermark);
        when(watermarkService.batchCreate(any())).thenReturn(watermarks);

        ResponseEntity<ApiResponse<java.util.List<WatermarkDTO>>> response =
                watermarkController.batchCreate(new HashMap<>());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getData().size());
        verify(watermarkService, times(1)).batchCreate(any());
    }

    @Test
    @DisplayName("验证水印 - 成功")
    void verifyWatermark_Success() {
        Map<String, Object> verifyResult = new HashMap<>();
        verifyResult.put("valid", true);
        verifyResult.put("watermarkContent", "CONFIDENTIAL");

        when(watermarkService.verifyWatermark(anyString())).thenReturn(verifyResult);

        ResponseEntity<ApiResponse<Map<String, Object>>> response =
                watermarkController.verifyWatermark("test-content");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(true, response.getBody().getData().get("valid"));
        verify(watermarkService, times(1)).verifyWatermark(anyString());
    }
}
