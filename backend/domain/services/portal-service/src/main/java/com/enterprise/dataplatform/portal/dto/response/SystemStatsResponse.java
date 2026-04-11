package com.enterprise.dataplatform.portal.dto.response;

import lombok.Data;

/**
 * System Statistics Response DTO
 */
@Data
public class SystemStatsResponse {
    private Long totalAssets;
    private Long activeUsers;
    private Long todayOperations;
    private Long totalQualityRules;
    private Double avgQualityScore;
    private Long pendingIssues;
    private Long totalLineageNodes;
    private Long totalLineageEdges;
}
