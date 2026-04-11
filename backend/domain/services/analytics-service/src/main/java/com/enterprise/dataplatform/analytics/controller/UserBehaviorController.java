package com.enterprise.dataplatform.analytics.controller;

import com.enterprise.dataplatform.analytics.dto.UserBehaviorResponse;
import com.enterprise.dataplatform.analytics.entity.UserBehavior;
import com.enterprise.dataplatform.analytics.service.UserBehaviorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for User Behavior analysis
 */
@RestController
@RequestMapping("/api/v1/analytics/user")
@RequiredArgsConstructor
@Tag(name = "用户行为分析", description = "用户行为数据分析和可视化")
public class UserBehaviorController {

    private final UserBehaviorService userBehaviorService;

    /**
     * Get user behavior analysis
     * GET /api/v1/analytics/user/behavior
     */
    @GetMapping("/behavior")
    @Operation(summary = "获取用户行为分析", description = "获取指定用户的行为分析数据")
    public ResponseEntity<Map<String, Object>> getUserBehavior(
            @Parameter(description = "用户ID")
            @RequestParam String userId,
            @Parameter(description = "天数")
            @RequestParam(defaultValue = "7") int days) {
        
        UserBehaviorResponse response = userBehaviorService.getUserBehavior(userId, days);
        return ResponseEntity.ok(wrapResponse(response));
    }

    /**
     * Get all users behavior summary
     * GET /api/v1/analytics/user/behavior/all
     */
    @GetMapping("/behavior/all")
    @Operation(summary = "获取所有用户行为汇总", description = "获取所有用户的行为分析汇总")
    public ResponseEntity<Map<String, Object>> getAllUsersBehavior(
            @Parameter(description = "天数")
            @RequestParam(defaultValue = "7") int days,
            @Parameter(description = "返回数量")
            @RequestParam(defaultValue = "100") int limit) {
        
        List<UserBehaviorResponse> responses = userBehaviorService.getAllUsersBehavior(days, limit);
        return ResponseEntity.ok(wrapResponse(responses));
    }

    /**
     * Get user behavior by session
     * GET /api/v1/analytics/user/behavior/session/{sessionId}
     */
    @GetMapping("/behavior/session/{sessionId}")
    @Operation(summary = "获取会话行为", description = "获取指定会话的所有用户行为")
    public ResponseEntity<Map<String, Object>> getSessionBehaviors(
            @PathVariable String sessionId) {
        
        List<UserBehavior> behaviors = userBehaviorService.getSessionBehaviors(sessionId);
        return ResponseEntity.ok(wrapResponse(behaviors));
    }

    /**
     * Get behavior summary statistics
     * GET /api/v1/analytics/user/behavior/summary
     */
    @GetMapping("/behavior/summary")
    @Operation(summary = "获取行为汇总统计", description = "获取指定时间范围内的行为汇总统计数据")
    public ResponseEntity<Map<String, Object>> getBehaviorSummary(
            @Parameter(description = "开始时间")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "结束时间")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        Map<String, Object> summary = userBehaviorService.getBehaviorSummary(startTime, endTime);
        return ResponseEntity.ok(wrapResponse(summary));
    }

    /**
     * Record user behavior
     * POST /api/v1/analytics/user/behavior
     */
    @PostMapping("/behavior")
    @Operation(summary = "记录用户行为", description = "记录单个用户的行为数据")
    public ResponseEntity<Map<String, Object>> recordUserBehavior(
            @RequestBody UserBehavior behavior,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String executor) {
        
        // Override userId with header value if provided
        if (!"system".equals(executor)) {
            behavior.setUserId(executor);
        }
        userBehaviorService.saveUserBehavior(behavior);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(wrapResponse(Map.of("message", "User behavior recorded successfully")));
    }

    /**
     * Batch record user behaviors
     * POST /api/v1/analytics/user/behavior/batch
     */
    @PostMapping("/behavior/batch")
    @Operation(summary = "批量记录用户行为", description = "批量记录多个用户的行为数据")
    public ResponseEntity<Map<String, Object>> batchRecordUserBehaviors(
            @RequestBody List<UserBehavior> behaviors,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String executor) {
        
        userBehaviorService.batchSaveUserBehaviors(behaviors);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(wrapResponse(Map.of("message", "Batch recording accepted", 
                        "count", behaviors.size())));
    }

    /**
     * Get top active users
     * GET /api/v1/analytics/user/top-active
     */
    @GetMapping("/top-active")
    @Operation(summary = "获取最活跃用户", description = "获取行为最活跃的用户排行")
    public ResponseEntity<Map<String, Object>> getTopActiveUsers(
            @Parameter(description = "天数")
            @RequestParam(defaultValue = "7") int days,
            @Parameter(description = "返回数量")
            @RequestParam(defaultValue = "50") int limit) {
        
        List<UserBehaviorResponse> topUsers = userBehaviorService.getAllUsersBehavior(days, limit);
        return ResponseEntity.ok(wrapResponse(topUsers));
    }

    /**
     * Get user activity timeline
     * GET /api/v1/analytics/user/timeline/{userId}
     */
    @GetMapping("/timeline/{userId}")
    @Operation(summary = "获取用户活动时间线", description = "获取指定用户的行为活动时间线")
    public ResponseEntity<Map<String, Object>> getUserTimeline(
            @PathVariable String userId,
            @Parameter(description = "天数")
            @RequestParam(defaultValue = "7") int days) {
        
        UserBehaviorResponse behavior = userBehaviorService.getUserBehavior(userId, days);
        
        Map<String, Object> timeline = new HashMap<>();
        timeline.put("userId", userId);
        timeline.put("startTime", behavior.getStartTime());
        timeline.put("endTime", behavior.getEndTime());
        timeline.put("totalActions", behavior.getTotalActions());
        timeline.put("topActions", behavior.getTopActions());
        timeline.put("topAssets", behavior.getTopAssets());
        
        return ResponseEntity.ok(wrapResponse(timeline));
    }

    /**
     * Get department behavior analysis
     * GET /api/v1/analytics/user/department/{department}
     */
    @GetMapping("/department/{department}")
    @Operation(summary = "获取部门行为分析", description = "获取指定部门用户的行为分析汇总")
    public ResponseEntity<Map<String, Object>> getDepartmentBehavior(
            @PathVariable String department,
            @Parameter(description = "天数")
            @RequestParam(defaultValue = "7") int days) {
        
        List<UserBehaviorResponse> allUsers = userBehaviorService.getAllUsersBehavior(days, 1000);
        List<UserBehaviorResponse> deptUsers = allUsers.stream()
                .filter(u -> department.equals(u.getDepartment()))
                .toList();
        
        // Aggregate department stats
        Map<String, Object> deptSummary = new HashMap<>();
        deptSummary.put("department", department);
        deptSummary.put("userCount", deptUsers.size());
        deptSummary.put("totalActions", deptUsers.stream()
                .mapToLong(UserBehaviorResponse::getTotalActions).sum());
        deptSummary.put("avgActionsPerUser", deptUsers.stream()
                .mapToLong(UserBehaviorResponse::getTotalActions)
                .average().orElse(0));
        deptSummary.put("users", deptUsers);
        
        return ResponseEntity.ok(wrapResponse(deptSummary));
    }

    /**
     * Health check endpoint
     * GET /api/v1/analytics/user/health
     */
    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查用户行为服务健康状态")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "user-behavior-service");
        health.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(wrapResponse(health));
    }

    private Map<String, Object> wrapResponse(Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "success");
        response.put("data", data);
        response.put("timestamp", LocalDateTime.now());
        return response;
    }
}
