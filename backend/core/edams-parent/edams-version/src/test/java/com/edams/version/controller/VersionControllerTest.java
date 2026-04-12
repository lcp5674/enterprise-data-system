package com.edams.version.controller;

import com.edams.common.model.ApiResponse;
import com.edams.common.model.PageResult;
import com.edams.version.dto.VersionDTO;
import com.edams.version.service.VersionService;
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
@DisplayName("VersionController 单元测试")
class VersionControllerTest {

    @Mock
    private VersionService versionService;

    @InjectMocks
    private VersionController versionController;

    private VersionDTO testVersion;

    @BeforeEach
    void setUp() {
        testVersion = new VersionDTO();
        testVersion.setId(1L);
        testVersion.setAssetId(100L);
        testVersion.setVersionNumber("v1.0.0");
        testVersion.setChangeType("CREATE");
        testVersion.setChangeDescription("Initial version");
        testVersion.setCreatedBy("admin");
        testVersion.setCreatedTime(LocalDateTime.now());
        testVersion.setStatus("ACTIVE");
    }

    @Test
    @DisplayName("获取版本详情 - 成功")
    void getById_Success() {
        when(versionService.getById(1L)).thenReturn(testVersion);

        ResponseEntity<ApiResponse<VersionDTO>> response = versionController.getById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("v1.0.0", response.getBody().getData().getVersionNumber());
        verify(versionService, times(1)).getById(1L);
    }

    @Test
    @DisplayName("获取资产版本列表 - 成功")
    void listByAsset_Success() {
        PageResult<VersionDTO> pageResult = new PageResult<>();
        pageResult.setTotal(3L);
        pageResult.setData(java.util.Collections.singletonList(testVersion));

        when(versionService.listByAsset(eq(100L), any())).thenReturn(pageResult);

        ResponseEntity<ApiResponse<PageResult<VersionDTO>>> response =
                versionController.listByAsset(100L, new HashMap<>());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3L, response.getBody().getData().getTotal());
        verify(versionService, times(1)).listByAsset(eq(100L), any());
    }

    @Test
    @DisplayName("比较版本差异 - 成功")
    void compareVersions_Success() {
        Map<String, Object> diff = new HashMap<>();
        diff.put("fromVersion", "v1.0.0");
        diff.put("toVersion", "v1.1.0");
        diff.put("additions", 10);
        diff.put("deletions", 3);
        diff.put("modifications", 5);

        when(versionService.compareVersions(eq(1L), eq(2L))).thenReturn(diff);

        ResponseEntity<ApiResponse<Map<String, Object>>> response =
                versionController.compareVersions(1L, 2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("v1.0.0", response.getBody().getData().get("fromVersion"));
        assertEquals("v1.1.0", response.getBody().getData().get("toVersion"));
        verify(versionService, times(1)).compareVersions(eq(1L), eq(2L));
    }

    @Test
    @DisplayName("回滚版本 - 成功")
    void rollback_Success() {
        Map<String, Object> rollbackResult = new HashMap<>();
        rollbackResult.put("success", true);
        rollbackResult.put("newVersionId", 3L);

        when(versionService.rollback(eq(100L), eq("v1.0.0"), anyString()))
                .thenReturn(rollbackResult);

        ResponseEntity<ApiResponse<Map<String, Object>>> response =
                versionController.rollback(100L, "v1.0.0", "admin");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(true, response.getBody().getData().get("success"));
        verify(versionService, times(1)).rollback(eq(100L), eq("v1.0.0"), anyString());
    }

    @Test
    @DisplayName("获取版本历史 - 成功")
    void getVersionHistory_Success() {
        java.util.List<VersionDTO> history = java.util.Collections.singletonList(testVersion);
        when(versionService.getVersionHistory(eq(100L), anyInt())).thenReturn(history);

        ResponseEntity<ApiResponse<java.util.List<VersionDTO>>> response =
                versionController.getVersionHistory(100L, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getData().size());
        verify(versionService, times(1)).getVersionHistory(eq(100L), anyInt());
    }

    @Test
    @DisplayName("获取版本变更记录 - 成功")
    void getChangeLog_Success() {
        PageResult<VersionDTO> pageResult = new PageResult<>();
        pageResult.setTotal(5L);
        pageResult.setData(java.util.Collections.singletonList(testVersion));

        when(versionService.getChangeLog(any())).thenReturn(pageResult);

        ResponseEntity<ApiResponse<PageResult<VersionDTO>>> response =
                versionController.getChangeLog(new HashMap<>());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(5L, response.getBody().getData().getTotal());
        verify(versionService, times(1)).getChangeLog(any());
    }

    @Test
    @DisplayName("验证版本 - 成功")
    void validateVersion_Success() {
        Map<String, Object> validation = new HashMap<>();
        validation.put("valid", true);
        validation.put("issues", java.util.Collections.emptyList());

        when(versionService.validateVersion(eq(100L), eq("v1.0.0"))).thenReturn(validation);

        ResponseEntity<ApiResponse<Map<String, Object>>> response =
                versionController.validateVersion(100L, "v1.0.0");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(true, response.getBody().getData().get("valid"));
        verify(versionService, times(1)).validateVersion(eq(100L), eq("v1.0.0"));
    }

    @Test
    @DisplayName("获取版本统计 - 成功")
    void getVersionStats_Success() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalVersions", 50L);
        stats.put("totalAssets", 30L);
        stats.put("avgVersionsPerAsset", 1.67);

        when(versionService.getVersionStats()).thenReturn(stats);

        ResponseEntity<ApiResponse<Map<String, Object>>> response = versionController.getVersionStats();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(50L, response.getBody().getData().get("totalVersions"));
        verify(versionService, times(1)).getVersionStats();
    }

    @Test
    @DisplayName("批量获取版本 - 成功")
    void batchGetVersions_Success() {
        java.util.List<VersionDTO> versions = java.util.Collections.singletonList(testVersion);
        when(versionService.batchGetVersions(anyList())).thenReturn(versions);

        ResponseEntity<ApiResponse<java.util.List<VersionDTO>>> response =
                versionController.batchGetVersions(java.util.Arrays.asList(1L, 2L, 3L));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getData().size());
        verify(versionService, times(1)).batchGetVersions(anyList());
    }

    @Test
    @DisplayName("创建版本快照 - 成功")
    void createSnapshot_Success() {
        Map<String, Object> snapshotResult = new HashMap<>();
        snapshotResult.put("snapshotId", "snap_001");
        snapshotResult.put("versionId", 1L);
        snapshotResult.put("size", 1024000L);

        when(versionService.createSnapshot(eq(100L), eq("v1.0.0"))).thenReturn(snapshotResult);

        ResponseEntity<ApiResponse<Map<String, Object>>> response =
                versionController.createSnapshot(100L, "v1.0.0");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("snap_001", response.getBody().getData().get("snapshotId"));
        verify(versionService, times(1)).createSnapshot(eq(100L), eq("v1.0.0"));
    }

    @Test
    @DisplayName("从快照恢复 - 成功")
    void restoreFromSnapshot_Success() {
        Map<String, Object> restoreResult = new HashMap<>();
        restoreResult.put("success", true);
        restoreResult.put("newVersionId", 4L);

        when(versionService.restoreFromSnapshot(eq("snap_001"))).thenReturn(restoreResult);

        ResponseEntity<ApiResponse<Map<String, Object>>> response =
                versionController.restoreFromSnapshot("snap_001");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(true, response.getBody().getData().get("success"));
        verify(versionService, times(1)).restoreFromSnapshot(eq("snap_001"));
    }
}
