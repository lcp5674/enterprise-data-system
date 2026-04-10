package com.enterprise.dataplatform.lineage.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for impact analysis
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImpactAnalysisResponse {

    private String assetId;

    private String assetName;

    private ImpactAnalysis impactAnalysis;

    private List<AffectedAsset> affectedAssets;

    private List<AffectedTask> affectedTasks;

    private List<String> mitigationSuggestions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImpactAnalysis {
        private Integer directDownstreamCount;
        private Integer totalDownstreamCount;
        private Integer criticalAssetsCount;
        private Integer reportsAffectedCount;
        private String estimatedImpactTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AffectedAsset {
        private String id;
        private String name;
        private String type;
        private String dependencyType;
        private String criticalLevel;
        private OwnerInfo owner;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AffectedTask {
        private String id;
        private String name;
        private String type;
        private String status;
        private OwnerInfo owner;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OwnerInfo {
        private String id;
        private String name;
        private String email;
    }
}
