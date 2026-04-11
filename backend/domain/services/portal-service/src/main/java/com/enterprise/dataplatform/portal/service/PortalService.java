package com.enterprise.dataplatform.portal.service;

import com.enterprise.dataplatform.portal.dto.request.AnnouncementRequest;
import com.enterprise.dataplatform.portal.dto.response.*;
import com.enterprise.dataplatform.portal.entity.Announcement;
import com.enterprise.dataplatform.portal.entity.DashboardStats;
import com.enterprise.dataplatform.portal.repository.AnnouncementRepository;
import com.enterprise.dataplatform.portal.repository.DashboardStatsRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Portal Service
 * 门户服务 - 工作台统计、公告、用户活动
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PortalService {

    private final DashboardStatsRepository dashboardStatsRepository;
    private final AnnouncementRepository announcementRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String SYSTEM_STATS_KEY = "portal:system_stats";
    private static final String USER_ACTIVITY_KEY = "portal:user_activity:";
    private static final String POPULAR_ASSETS_KEY = "portal:popular_assets";
    private static final long STATS_CACHE_TTL = 5; // minutes

    /**
     * Get dashboard stats for a specific user
     */
    public DashboardStatsResponse getDashboardStats(String userId) {
        DashboardStatsResponse response = new DashboardStatsResponse();

        // Try to get from DB first
        DashboardStats stats = dashboardStatsRepository
                .findTopByUserIdOrderByStatsDateDesc(userId)
                .orElse(null);

        if (stats != null) {
            response.setTotalAssets(stats.getTotalAssets());
            response.setActiveAssets(stats.getActiveAssets());
            response.setQualityScore(stats.getQualityScore());
            response.setPendingTasks(stats.getPendingTasks());
            response.setStatsDate(stats.getStatsDate());

            // Parse recent views JSON
            if (stats.getRecentViews() != null) {
                try {
                    List<DashboardStatsResponse.RecentViewItem> recentViews = objectMapper.readValue(
                            stats.getRecentViews(),
                            new TypeReference<List<DashboardStatsResponse.RecentViewItem>>() {}
                    );
                    response.setRecentViews(recentViews);
                } catch (JsonProcessingException e) {
                    log.warn("Failed to parse recent views JSON: {}", e.getMessage());
                    response.setRecentViews(new ArrayList<>());
                }
            }
        } else {
            // Return default values
            response.setTotalAssets(0L);
            response.setActiveAssets(0L);
            response.setQualityScore(0.0);
            response.setPendingTasks(0);
            response.setRecentViews(new ArrayList<>());
        }

        return response;
    }

    /**
     * Get system-level statistics
     */
    @SuppressWarnings("unchecked")
    public SystemStatsResponse getSystemStats() {
        SystemStatsResponse response = new SystemStatsResponse();

        // Try to get from Redis cache first
        Object cachedStats = redisTemplate.opsForValue().get(SYSTEM_STATS_KEY);
        if (cachedStats != null && cachedStats instanceof Map) {
            Map<String, Object> statsMap = (Map<String, Object>) cachedStats;
            response.setTotalAssets(((Number) statsMap.getOrDefault("totalAssets", 0)).longValue());
            response.setActiveUsers(((Number) statsMap.getOrDefault("activeUsers", 0)).longValue());
            response.setTodayOperations(((Number) statsMap.getOrDefault("todayOperations", 0)).longValue());
            response.setTotalQualityRules(((Number) statsMap.getOrDefault("totalQualityRules", 0)).longValue());
            response.setAvgQualityScore(((Number) statsMap.getOrDefault("avgQualityScore", 0)).doubleValue());
            response.setPendingIssues(((Number) statsMap.getOrDefault("pendingIssues", 0)).longValue());
            response.setTotalLineageNodes(((Number) statsMap.getOrDefault("totalLineageNodes", 0)).longValue());
            response.setTotalLineageEdges(((Number) statsMap.getOrDefault("totalLineageEdges", 0)).longValue());
            return response;
        }

        // Return default values if no cache
        response.setTotalAssets(0L);
        response.setActiveUsers(0L);
        response.setTodayOperations(0L);
        response.setTotalQualityRules(0L);
        response.setAvgQualityScore(0.0);
        response.setPendingIssues(0L);
        response.setTotalLineageNodes(0L);
        response.setTotalLineageEdges(0L);

        return response;
    }

    /**
     * Get active announcements
     */
    public List<AnnouncementResponse> getActiveAnnouncements() {
        List<Announcement> announcements = announcementRepository
                .findByStatusAndExpiredAtAfterOrderByPriorityDesc(
                        Announcement.AnnouncementStatus.PUBLISHED,
                        LocalDateTime.now()
                );

        return announcements.stream()
                .map(this::toAnnouncementResponse)
                .collect(Collectors.toList());
    }

    /**
     * Publish an announcement
     */
    @Transactional
    public AnnouncementResponse publishAnnouncement(AnnouncementRequest request, String publishedBy) {
        Announcement announcement = new Announcement();
        announcement.setTitle(request.getTitle());
        announcement.setContent(request.getContent());
        announcement.setType(Announcement.AnnouncementType.valueOf(
                request.getType() != null ? request.getType() : "INFO"));
        announcement.setPriority(request.getPriority() != null ? request.getPriority() : 0);
        announcement.setTargetRoles(request.getTargetRoles());
        announcement.setPublishedBy(publishedBy);
        announcement.setPublishedAt(LocalDateTime.now());
        announcement.setStatus(Announcement.AnnouncementStatus.PUBLISHED);

        if (request.getExpiredAt() != null) {
            announcement.setExpiredAt(LocalDateTime.parse(request.getExpiredAt()));
        } else {
            announcement.setExpiredAt(LocalDateTime.now().plusDays(30)); // Default 30 days
        }

        Announcement saved = announcementRepository.save(announcement);
        return toAnnouncementResponse(saved);
    }

    /**
     * Get user recent activities
     */
    @SuppressWarnings("unchecked")
    public List<ActivityResponse> getUserRecentActivities(String userId) {
        String key = USER_ACTIVITY_KEY + userId;
        Object activities = redisTemplate.opsForValue().get(key);

        if (activities != null && activities instanceof List) {
            List<Map<String, Object>> activityList = (List<Map<String, Object>>) activities;
            return activityList.stream().map(map -> {
                ActivityResponse response = new ActivityResponse();
                response.setActivityId((String) map.get("activityId"));
                response.setUserId(userId);
                response.setAction((String) map.get("action"));
                response.setResourceId((String) map.get("resourceId"));
                response.setResourceName((String) map.get("resourceName"));
                response.setDescription((String) map.get("description"));
                if (map.get("timestamp") != null) {
                    response.setTimestamp(LocalDateTime.parse((String) map.get("timestamp")));
                }
                return response;
            }).collect(Collectors.toList());
        }

        return new ArrayList<>();
    }

    /**
     * Record user activity
     */
    public void recordActivity(String userId, String action, String resourceId) {
        String key = USER_ACTIVITY_KEY + userId;

        ActivityResponse activity = new ActivityResponse();
        activity.setActivityId(java.util.UUID.randomUUID().toString());
        activity.setUserId(userId);
        activity.setAction(action);
        activity.setResourceId(resourceId);
        activity.setTimestamp(LocalDateTime.now());

        try {
            // Get existing activities
            Object existing = redisTemplate.opsForValue().get(key);
            List<Map<String, Object>> activities = new ArrayList<>();

            if (existing != null && existing instanceof List) {
                activities.addAll((List<Map<String, Object>>) existing);
            }

            // Add new activity at the beginning
            Map<String, Object> activityMap = objectMapper.convertValue(activity, Map.class);
            activities.add(0, activityMap);

            // Keep only last 50 activities
            if (activities.size() > 50) {
                activities = activities.subList(0, 50);
            }

            // Save back to Redis
            redisTemplate.opsForValue().set(key, activities, 30, TimeUnit.DAYS);
        } catch (Exception e) {
            log.error("Failed to record activity: {}", e.getMessage());
        }
    }

    /**
     * Get popular assets
     */
    @SuppressWarnings("unchecked")
    public List<PopularAssetResponse> getPopularAssets() {
        Object cached = redisTemplate.opsForValue().get(POPULAR_ASSETS_KEY);

        if (cached != null && cached instanceof List) {
            List<Map<String, Object>> assetList = (List<Map<String, Object>>) cached;
            return assetList.stream().map(map -> {
                PopularAssetResponse response = new PopularAssetResponse();
                response.setAssetId((String) map.get("assetId"));
                response.setAssetName((String) map.get("assetName"));
                response.setAssetType((String) map.get("assetType"));
                response.setOwner((String) map.get("owner"));
                response.setViewCount(((Number) map.getOrDefault("viewCount", 0)).longValue());
                response.setUsageCount(((Number) map.getOrDefault("usageCount", 0)).longValue());
                response.setQualityScore(((Number) map.getOrDefault("qualityScore", 0)).doubleValue());
                return response;
            }).collect(Collectors.toList());
        }

        return new ArrayList<>();
    }

    private AnnouncementResponse toAnnouncementResponse(Announcement announcement) {
        AnnouncementResponse response = new AnnouncementResponse();
        response.setId(announcement.getId());
        response.setTitle(announcement.getTitle());
        response.setContent(announcement.getContent());
        response.setType(announcement.getType().name());
        response.setPriority(announcement.getPriority());
        response.setPublishedBy(announcement.getPublishedBy());
        response.setPublishedAt(announcement.getPublishedAt());
        response.setExpiredAt(announcement.getExpiredAt());
        response.setStatus(announcement.getStatus().name());

        // Parse target roles JSON
        if (announcement.getTargetRoles() != null) {
            try {
                List<String> roles = objectMapper.readValue(
                        announcement.getTargetRoles(),
                        new TypeReference<List<String>>() {}
                );
                response.setTargetRoles(roles);
            } catch (JsonProcessingException e) {
                response.setTargetRoles(new ArrayList<>());
            }
        } else {
            response.setTargetRoles(new ArrayList<>());
        }

        return response;
    }
}
