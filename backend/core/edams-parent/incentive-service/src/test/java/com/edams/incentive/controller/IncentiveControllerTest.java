package com.edams.incentive.controller;

import com.edams.common.model.ApiResponse;
import com.edams.common.model.PageResult;
import com.edams.incentive.service.IncentiveService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("IncentiveController 单元测试")
class IncentiveControllerTest {

    @Mock
    private IncentiveService incentiveService;

    @InjectMocks
    private IncentiveController incentiveController;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("获取用户积分 - 成功")
    void getUserPoints_Success() {
        Map<String, Object> pointsInfo = new HashMap<>();
        pointsInfo.put("userId", 100L);
        pointsInfo.put("totalPoints", 1500L);
        pointsInfo.put("availablePoints", 1200L);
        pointsInfo.put("frozenPoints", 300L);

        when(incentiveService.getUserPoints(100L)).thenReturn(pointsInfo);

        ResponseEntity<ApiResponse<Map<String, Object>>> response = incentiveController.getUserPoints(100L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1500L, response.getBody().getData().get("totalPoints"));
        verify(incentiveService, times(1)).getUserPoints(100L);
    }

    @Test
    @DisplayName("获取用户积分明细 - 成功")
    void getPointsDetail_Success() {
        PageResult<Map<String, Object>> pageResult = new PageResult<>();
        pageResult.setTotal(10L);

        when(incentiveService.getPointsDetail(eq(100L), any())).thenReturn(pageResult);

        ResponseEntity<ApiResponse<PageResult<Map<String, Object>>>> response =
                incentiveController.getPointsDetail(100L, new HashMap<>());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(10L, response.getBody().getData().getTotal());
        verify(incentiveService, times(1)).getPointsDetail(eq(100L), any());
    }

    @Test
    @DisplayName("获取积分规则列表 - 成功")
    void getRules_Success() {
        PageResult<Map<String, Object>> pageResult = new PageResult<>();
        pageResult.setTotal(5L);

        when(incentiveService.getRules(any())).thenReturn(pageResult);

        ResponseEntity<ApiResponse<PageResult<Map<String, Object>>>> response =
                incentiveController.getRules(new HashMap<>());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(5L, response.getBody().getData().getTotal());
        verify(incentiveService, times(1)).getRules(any());
    }

    @Test
    @DisplayName("获取排行榜 - 成功")
    void getLeaderboard_Success() {
        java.util.List<Map<String, Object>> leaders = new java.util.ArrayList<>();
        Map<String, Object> leader = new HashMap<>();
        leader.put("rank", 1);
        leader.put("userId", 100L);
        leader.put("userName", "Alice");
        leader.put("points", 5000L);
        leaders.add(leader);

        when(incentiveService.getLeaderboard(anyString(), anyInt(), anyInt()))
                .thenReturn(leaders);

        ResponseEntity<ApiResponse<java.util.List<Map<String, Object>>>> response =
                incentiveController.getLeaderboard("MONTHLY", 1, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getData().size());
        assertEquals("Alice", response.getBody().getData().get(0).get("userName"));
        verify(incentiveService, times(1)).getLeaderboard(anyString(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("创建积分规则 - 成功")
    void createRule_Success() {
        Map<String, Object> rule = new HashMap<>();
        rule.put("id", 1L);
        rule.put("name", "Data Contribution");
        rule.put("points", 100);

        when(incentiveService.createRule(anyMap())).thenReturn(rule);

        ResponseEntity<ApiResponse<Map<String, Object>>> response =
                incentiveController.createRule(new HashMap<>());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(100, response.getBody().getData().get("points"));
        verify(incentiveService, times(1)).createRule(anyMap());
    }

    @Test
    @DisplayName("更新积分规则 - 成功")
    void updateRule_Success() {
        Map<String, Object> rule = new HashMap<>();
        rule.put("id", 1L);
        rule.put("name", "Updated Rule");
        rule.put("points", 150);

        when(incentiveService.updateRule(eq(1L), anyMap())).thenReturn(rule);

        ResponseEntity<ApiResponse<Map<String, Object>>> response =
                incentiveController.updateRule(1L, new HashMap<>());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(150, response.getBody().getData().get("points"));
        verify(incentiveService, times(1)).updateRule(eq(1L), anyMap());
    }

    @Test
    @DisplayName("删除积分规则 - 成功")
    void deleteRule_Success() {
        doNothing().when(incentiveService).deleteRule(1L);

        ResponseEntity<ApiResponse<Void>> response = incentiveController.deleteRule(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        verify(incentiveService, times(1)).deleteRule(1L);
    }

    @Test
    @DisplayName("获取积分统计 - 成功")
    void getStatistics_Success() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", 500L);
        stats.put("totalPointsIssued", 1000000L);
        stats.put("totalPointsRedeemed", 800000L);

        when(incentiveService.getStatistics()).thenReturn(stats);

        ResponseEntity<ApiResponse<Map<String, Object>>> response = incentiveController.getStatistics();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500L, response.getBody().getData().get("totalUsers"));
        verify(incentiveService, times(1)).getStatistics();
    }

    @Test
    @DisplayName("获取可兑换奖励列表 - 成功")
    void getRewards_Success() {
        PageResult<Map<String, Object>> pageResult = new PageResult<>();
        pageResult.setTotal(3L);

        when(incentiveService.getRewards(any())).thenReturn(pageResult);

        ResponseEntity<ApiResponse<PageResult<Map<String, Object>>>> response =
                incentiveController.getRewards(new HashMap<>());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3L, response.getBody().getData().getTotal());
        verify(incentiveService, times(1)).getRewards(any());
    }

    @Test
    @DisplayName("兑换奖励 - 成功")
    void redeemReward_Success() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("remainingPoints", 800L);

        when(incentiveService.redeemReward(eq(100L), eq(1L))).thenReturn(result);

        ResponseEntity<ApiResponse<Map<String, Object>>> response =
                incentiveController.redeemReward(100L, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(true, response.getBody().getData().get("success"));
        verify(incentiveService, times(1)).redeemReward(eq(100L), eq(1L));
    }

    @Test
    @DisplayName("处理积分事件 - 成功")
    void processPointsEvent_Success() {
        Map<String, Object> result = new HashMap<>();
        result.put("eventId", "evt_001");
        result.put("pointsAwarded", 50L);
        result.put("newBalance", 1250L);

        when(incentiveService.processPointsEvent(anyMap())).thenReturn(result);

        ResponseEntity<ApiResponse<Map<String, Object>>> response =
                incentiveController.processPointsEvent(new HashMap<>());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(50L, response.getBody().getData().get("pointsAwarded"));
        verify(incentiveService, times(1)).processPointsEvent(anyMap());
    }
}
