package com.enterprise.edams.asset.dto.lineage;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 影响分析DTO
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Data
public class ImpactAnalysisDTO {

    /**
     * 被分析资产ID
     */
    private String assetId;

    /**
     * 被分析资产名称
     */
    private String assetName;

    /**
     * 影响的资产总数
     */
    private Integer totalImpactedAssets;

    /**
     * 影响的表数量
     */
    private Integer impactedTables;

    /**
     * 影响的视图数量
     */
    private Integer impactedViews;

    /**
     * 影响的报表数量
     */
    private Integer impactedReports;

    /**
     * 影响的ETL任务数量
     */
    private Integer impactedTasks;

    /**
     * 下游节点列表
     */
    private List<LineageGraphDTO.LineageNode> downstreamNodes;

    /**
     * 高影响路径
     */
    private List<ImpactPath> highImpactPaths;

    /**
     * 关键下游资产
     */
    private List<ImpactedAsset> criticalDownstreamAssets;

    /**
     * 分析深度
     */
    private Integer depth;

    /**
     * 分析时间
     */
    private LocalDateTime analysisTime;

    /**
     * 影响路径
     */
    @Data
    public static class ImpactPath {
        private String pathId;
        private List<String> assetIds;
        private String pathDescription;
        private Integer assetCount;
        private String riskLevel;
    }

    /**
     * 受影响资产
     */
    @Data
    public static class ImpactedAsset {
        private String assetId;
        private String assetName;
        private String assetType;
        private String owner;
        private String businessCriticality;
        private Integer downstreamLevel;
    }
}
