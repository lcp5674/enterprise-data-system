package com.enterprise.dataplatform.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * User Behavior Analysis Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBehaviorResponse {

    private String userId;
    private String userName;
    private String department;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long totalActions;
    private Long uniqueAssetsAccessed;
    private Long activeMinutes;
    private Map<String, Long> actionTypeCounts;
    private List<ActionSummary> topActions;
    private List<AssetAccess> topAssets;
    private Map<String, Object> behaviorInsights;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActionSummary {
        private String actionType;
        private Long count;
        private Long totalDuration;
        private Long avgDuration;
        private Double successRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssetAccess {
        private String assetId;
        private String assetName;
        private String assetType;
        private Long accessCount;
        private Long totalDuration;
    }
}
