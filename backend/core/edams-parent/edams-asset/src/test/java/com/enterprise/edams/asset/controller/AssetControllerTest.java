package com.enterprise.edams.asset.controller;

import com.enterprise.edams.asset.dto.AssetCreateRequest;
import com.enterprise.edams.asset.dto.AssetDTO;
import com.enterprise.edams.asset.dto.AssetQueryRequest;
import com.enterprise.edams.asset.dto.AssetUpdateRequest;
import com.enterprise.edams.asset.service.AssetService;
import com.enterprise.edams.common.result.PageResult;
import com.enterprise.edams.common.result.Result;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AssetController 单元测试")
class AssetControllerTest {

    @Mock
    private AssetService assetService;

    @InjectMocks
    private AssetController assetController;

    private AssetDTO testAsset;
    private AssetCreateRequest createRequest;
    private AssetUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        testAsset = new AssetDTO();
        testAsset.setId(1L);
        testAsset.setName("test_table");
        testAsset.setCode("ODS.TEST_TABLE");
        testAsset.setAssetType("DATABASE_TABLE");
        testAsset.setStatus("ACTIVE");
        testAsset.setOwnerId(100L);
        testAsset.setCreateTime(LocalDateTime.now());
        testAsset.setUpdateTime(LocalDateTime.now());

        createRequest = new AssetCreateRequest();
        createRequest.setName("new_table");
        createRequest.setCode("ODS.NEW_TABLE");
        createRequest.setAssetType("DATABASE_TABLE");
        createRequest.setOwnerId(100L);

        updateRequest = new AssetUpdateRequest();
        updateRequest.setName("updated_table");
        updateRequest.setDescription("Updated description");
    }

    @Test
    @DisplayName("创建资产 - 成功")
    void createAsset_Success() {
        when(assetService.createAsset(any(AssetCreateRequest.class), anyString())).thenReturn(testAsset);

        ResponseEntity<Result<AssetDTO>> response = assetController.createAsset(createRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("test_table", response.getBody().getData().getName());
        verify(assetService, times(1)).createAsset(any(AssetCreateRequest.class), anyString());
    }

    @Test
    @DisplayName("更新资产 - 成功")
    void updateAsset_Success() {
        when(assetService.updateAsset(eq(1L), any(AssetUpdateRequest.class), anyString()))
                .thenReturn(testAsset);

        ResponseEntity<Result<AssetDTO>> response = assetController.updateAsset(1L, updateRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(assetService, times(1)).updateAsset(eq(1L), any(AssetUpdateRequest.class), anyString());
    }

    @Test
    @DisplayName("删除资产 - 成功")
    void deleteAsset_Success() {
        doNothing().when(assetService).deleteAsset(eq(1L), anyString());

        ResponseEntity<Result<Void>> response = assetController.deleteAsset(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        verify(assetService, times(1)).deleteAsset(eq(1L), anyString());
    }

    @Test
    @DisplayName("获取资产详情 - 成功")
    void getAssetById_Success() {
        when(assetService.getAssetById(1L)).thenReturn(testAsset);

        ResponseEntity<Result<AssetDTO>> response = assetController.getAssetById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("test_table", response.getBody().getData().getName());
        verify(assetService, times(1)).getAssetById(1L);
    }

    @Test
    @DisplayName("分页查询资产列表 - 成功")
    void listAssets_Success() {
        PageResult<AssetDTO> pageResult = new PageResult<>();
        pageResult.setTotal(1L);
        pageResult.setData(List.of(testAsset));

        when(assetService.listAssets(any(AssetQueryRequest.class))).thenReturn(pageResult);

        ResponseEntity<Result<PageResult<AssetDTO>>> response =
                assetController.listAssets(new AssetQueryRequest());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getData().getTotal());
        verify(assetService, times(1)).listAssets(any(AssetQueryRequest.class));
    }

    @Test
    @DisplayName("获取资产标签列表 - 成功")
    void getAssetTags_Success() {
        List<String> tags = Arrays.asList("PII", "CONFIDENTIAL");
        when(assetService.getAssetTags(1L)).thenReturn(tags);

        ResponseEntity<Result<List<String>>> response = assetController.getAssetTags(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getData().size());
        verify(assetService, times(1)).getAssetTags(1L);
    }

    @Test
    @DisplayName("更新资产标签 - 成功")
    void updateAssetTags_Success() {
        doNothing().when(assetService).updateAssetTags(eq(1L), anyList(), anyString());

        ResponseEntity<Result<Void>> response =
                assetController.updateAssetTags(1L, Arrays.asList("PII", "PUBLIC"), "admin");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        verify(assetService, times(1)).updateAssetTags(eq(1L), anyList(), anyString());
    }

    @Test
    @DisplayName("获取资产统计 - 成功")
    void getAssetStats_Success() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", 1000L);
        stats.put("active", 950L);

        when(assetService.getAssetStatistics()).thenReturn(stats);

        ResponseEntity<Result<Map<String, Object>>> response = assetController.getAssetStatistics();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1000L, response.getBody().getData().get("total"));
        verify(assetService, times(1)).getAssetStatistics();
    }

    @Test
    @DisplayName("获取资产字段详情 - 成功")
    void getAssetFields_Success() {
        ResponseEntity<Result<Map<String, Object>>> response = assetController.getAssetFields(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(assetService, times(1)).getAssetFields(1L);
    }

    @Test
    @DisplayName("获取资产血缘关系 - 成功")
    void getAssetLineage_Success() {
        Map<String, Object> lineage = new HashMap<>();
        lineage.put("upstream", java.util.Collections.emptyList());
        lineage.put("downstream", java.util.Collections.emptyList());

        when(assetService.getAssetLineage(1L)).thenReturn(lineage);

        ResponseEntity<Result<Map<String, Object>>> response = assetController.getAssetLineage(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(assetService, times(1)).getAssetLineage(1L);
    }

    @Test
    @DisplayName("获取资产样本数据 - 成功")
    void getAssetSampleData_Success() {
        ResponseEntity<Result<Map<String, Object>>> response = assetController.getAssetSampleData(1L, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(assetService, times(1)).getAssetSampleData(eq(1L), eq(10));
    }
}
