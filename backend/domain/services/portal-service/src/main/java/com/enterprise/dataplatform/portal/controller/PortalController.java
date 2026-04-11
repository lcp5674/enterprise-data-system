package com.enterprise.dataplatform.portal.controller;

import com.enterprise.dataplatform.portal.dto.request.AnnouncementRequest;
import com.enterprise.dataplatform.portal.dto.response.*;
import com.enterprise.dataplatform.portal.service.PortalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Portal Controller
 * 门户服务REST接口
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/portal")
@RequiredArgsConstructor
@Tag(name = "Portal API", description = "门户服务 - 工作台、公告、统计接口")
public class PortalController {

    private final PortalService portalService;

    @GetMapping("/dashboard")
    @Operation(summary = "获取用户工作台数据")
    public ResponseEntity<Map<String, Object>> getDashboard(
            @RequestParam(required = false, defaultValue = "current") String userId) {
        try {
            DashboardStatsResponse stats = portalService.getDashboardStats(userId);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", stats);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to get dashboard stats: {}", e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    @GetMapping("/stats")
    @Operation(summary = "获取系统级统计数据")
    public ResponseEntity<Map<String, Object>> getSystemStats() {
        try {
            SystemStatsResponse stats = portalService.getSystemStats();
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", stats);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to get system stats: {}", e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    @GetMapping("/announcements")
    @Operation(summary = "获取当前有效公告")
    public ResponseEntity<Map<String, Object>> getActiveAnnouncements() {
        try {
            List<AnnouncementResponse> announcements = portalService.getActiveAnnouncements();
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", announcements);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to get announcements: {}", e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    @PostMapping("/announcements")
    @Operation(summary = "发布新公告")
    public ResponseEntity<Map<String, Object>> publishAnnouncement(
            @RequestBody AnnouncementRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            AnnouncementResponse announcement = portalService.publishAnnouncement(request, userId);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", announcement);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to publish announcement: {}", e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    @GetMapping("/activities")
    @Operation(summary = "获取用户最近活动记录")
    public ResponseEntity<Map<String, Object>> getUserActivities(
            @RequestParam(required = false, defaultValue = "current") String userId) {
        try {
            List<ActivityResponse> activities = portalService.getUserRecentActivities(userId);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", activities);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to get user activities: {}", e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    @GetMapping("/popular-assets")
    @Operation(summary = "获取热门资产排行")
    public ResponseEntity<Map<String, Object>> getPopularAssets() {
        try {
            List<PopularAssetResponse> assets = portalService.getPopularAssets();
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", assets);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to get popular assets: {}", e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    @PostMapping("/activities/record")
    @Operation(summary = "记录用户活动")
    public ResponseEntity<Map<String, Object>> recordActivity(
            @RequestParam String userId,
            @RequestParam String action,
            @RequestParam(required = false) String resourceId) {
        try {
            portalService.recordActivity(userId, action, resourceId);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to record activity: {}", e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }
}
