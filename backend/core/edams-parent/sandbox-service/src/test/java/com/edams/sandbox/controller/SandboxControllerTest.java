package com.edams.sandbox.controller;

import com.edams.common.model.ApiResponse;
import com.edams.common.model.PageResult;
import com.edams.sandbox.entity.Sandbox;
import com.edams.sandbox.entity.SandboxExecution;
import com.edams.sandbox.service.SandboxService;
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
@DisplayName("SandboxController 单元测试")
class SandboxControllerTest {

    @Mock
    private SandboxService sandboxService;

    @InjectMocks
    private SandboxController sandboxController;

    private Sandbox testSandbox;
    private SandboxExecution testExecution;

    @BeforeEach
    void setUp() {
        testSandbox = new Sandbox();
        testSandbox.setId(1L);
        testSandbox.setName("Test Sandbox");
        testSandbox.setOwnerId(100L);
        testSandbox.setStatus("STOPPED");
        testSandbox.setSandboxType("POSTGRESQL");
        testSandbox.setCreatedTime(LocalDateTime.now());
        testSandbox.setExpiredTime(LocalDateTime.now().plusDays(7));

        testExecution = new SandboxExecution();
        testExecution.setId(1L);
        testExecution.setSandboxId(1L);
        testExecution.setStatus("SUCCESS");
        testExecution.setExecutionTime(500L);
    }

    @Test
    @DisplayName("创建沙箱 - 成功")
    void createSandbox_Success() {
        when(sandboxService.createSandbox(any(Sandbox.class))).thenReturn(testSandbox);

        ResponseEntity<ApiResponse<Sandbox>> response = sandboxController.createSandbox(testSandbox);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test Sandbox", response.getBody().getData().getName());
        verify(sandboxService, times(1)).createSandbox(any(Sandbox.class));
    }

    @Test
    @DisplayName("更新沙箱 - 成功")
    void updateSandbox_Success() {
        when(sandboxService.updateSandbox(eq(1L), any(Sandbox.class))).thenReturn(testSandbox);

        ResponseEntity<ApiResponse<Sandbox>> response = sandboxController.updateSandbox(1L, testSandbox);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test Sandbox", response.getBody().getData().getName());
        verify(sandboxService, times(1)).updateSandbox(eq(1L), any(Sandbox.class));
    }

    @Test
    @DisplayName("删除沙箱 - 成功")
    void deleteSandbox_Success() {
        doNothing().when(sandboxService).deleteSandbox(1L);

        ResponseEntity<ApiResponse<Void>> response = sandboxController.deleteSandbox(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        verify(sandboxService, times(1)).deleteSandbox(1L);
    }

    @Test
    @DisplayName("获取沙箱详情 - 成功")
    void getSandbox_Success() {
        when(sandboxService.getSandboxById(1L)).thenReturn(testSandbox);

        ResponseEntity<ApiResponse<Sandbox>> response = sandboxController.getSandbox(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test Sandbox", response.getBody().getData().getName());
        verify(sandboxService, times(1)).getSandboxById(1L);
    }

    @Test
    @DisplayName("分页查询沙箱列表 - 成功")
    void listSandboxes_Success() {
        PageResult<Sandbox> pageResult = new PageResult<>();
        pageResult.setTotal(1L);
        pageResult.setData(java.util.Collections.singletonList(testSandbox));

        when(sandboxService.listSandboxes(anyMap())).thenReturn(pageResult);

        ResponseEntity<ApiResponse<PageResult<Sandbox>>> response = sandboxController.listSandboxes(
                null, null, null, null, 1, 10, new String[]{"createdTime,desc"});

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getData().getTotal());
        verify(sandboxService, times(1)).listSandboxes(anyMap());
    }

    @Test
    @DisplayName("启动沙箱 - 成功")
    void startSandbox_Success() {
        doNothing().when(sandboxService).startSandbox(1L);

        ResponseEntity<ApiResponse<Void>> response = sandboxController.startSandbox(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        verify(sandboxService, times(1)).startSandbox(1L);
    }

    @Test
    @DisplayName("停止沙箱 - 成功")
    void stopSandbox_Success() {
        doNothing().when(sandboxService).stopSandbox(1L);

        ResponseEntity<ApiResponse<Void>> response = sandboxController.stopSandbox(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        verify(sandboxService, times(1)).stopSandbox(1L);
    }

    @Test
    @DisplayName("执行SQL - 成功")
    void executeSql_Success() {
        when(sandboxService.executeSql(eq(1L), anyString(), eq(100L))).thenReturn(testExecution);

        ResponseEntity<ApiResponse<SandboxExecution>> response =
                sandboxController.executeSql(1L, "SELECT * FROM users", 100L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("SUCCESS", response.getBody().getData().getStatus());
        verify(sandboxService, times(1)).executeSql(eq(1L), anyString(), eq(100L));
    }

    @Test
    @DisplayName("测试API - 成功")
    void testApi_Success() {
        when(sandboxService.testApi(eq(1L), anyString(), anyString(), any(), eq(100L)))
                .thenReturn(testExecution);

        ResponseEntity<ApiResponse<SandboxExecution>> response =
                sandboxController.testApi(1L, "http://api.test.com", "GET", null, 100L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("SUCCESS", response.getBody().getData().getStatus());
        verify(sandboxService, times(1)).testApi(eq(1L), anyString(), anyString(), any(), eq(100L));
    }

    @Test
    @DisplayName("数据模拟 - 成功")
    void simulateData_Success() {
        Map<String, Object> config = new HashMap<>();
        config.put("rows", 100);

        when(sandboxService.simulateData(eq(1L), anyString(), any(), eq(100L)))
                .thenReturn(testExecution);

        ResponseEntity<ApiResponse<SandboxExecution>> response =
                sandboxController.simulateData(1L, "ANONYMOUS", config, 100L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(sandboxService, times(1)).simulateData(eq(1L), anyString(), any(), eq(100L));
    }

    @Test
    @DisplayName("获取沙箱统计 - 成功")
    void getSandboxStats_Success() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalExecutions", 10L);
        stats.put("avgExecutionTime", 500.0);

        when(sandboxService.getSandboxStats(1L)).thenReturn(stats);

        ResponseEntity<ApiResponse<Map<String, Object>>> response = sandboxController.getSandboxStats(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(10L, response.getBody().getData().get("totalExecutions"));
        verify(sandboxService, times(1)).getSandboxStats(1L);
    }

    @Test
    @DisplayName("过期检查 - 成功")
    void expireCheck_Success() {
        doNothing().when(sandboxService).expireSandboxes();

        ResponseEntity<ApiResponse<Void>> response = sandboxController.expireCheck();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        verify(sandboxService, times(1)).expireSandboxes();
    }

    @Test
    @DisplayName("查询SQL历史记录 - 成功")
    void listSqlHistory_Success() {
        PageResult<SandboxExecution> pageResult = new PageResult<>();
        pageResult.setTotal(1L);
        pageResult.setData(java.util.Collections.singletonList(testExecution));

        when(sandboxService.listSqlExecutions(eq(1L), anyMap())).thenReturn(pageResult);

        ResponseEntity<ApiResponse<PageResult<SandboxExecution>>> response =
                sandboxController.listSqlHistory(1L, null, 1, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getData().getTotal());
        verify(sandboxService, times(1)).listSqlExecutions(eq(1L), anyMap());
    }
}
