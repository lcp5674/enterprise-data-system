package com.enterprise.dataplatform.portal.dto.response;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Dashboard Stats Response DTO
 */
@Data
public class DashboardStatsResponse {
    private Long totalAssets;
    private Long activeAssets;
    private Double qualityScore;
    private Integer pendingTasks;
    private List<RecentViewItem> recentViews;
    private LocalDate statsDate;

    @Data
    public static class RecentViewItem {
        private String assetId;
        private String assetName;
        private String assetType;
        private LocalDateTime viewedAt;
    }
}
