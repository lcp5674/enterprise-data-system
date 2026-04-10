package com.enterprise.edams.asset.dto.lineage;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 血缘图谱DTO
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Data
public class LineageGraphDTO {

    /**
     * 根资产ID
     */
    private String rootAssetId;

    /**
     * 根资产名称
     */
    private String rootAssetName;

    /**
     * 节点列表
     */
    private List<LineageNode> nodes;

    /**
     * 边列表
     */
    private List<LineageEdge> edges;

    /**
     * 血缘深度
     */
    private Integer depth;

    /**
     * 节点数量
     */
    private Integer nodeCount;

    /**
     * 边数量
     */
    private Integer edgeCount;

    /**
     * 查询时间
     */
    private LocalDateTime queryTime;

    /**
     * 血缘节点
     */
    @Data
    public static class LineageNode {
        /**
         * 资产ID
         */
        private String assetId;

        /**
         * 资产名称
         */
        private String assetName;

        /**
         * 资产类型
         */
        private String assetType;

        /**
         * 数据库
         */
        private String database;

        /**
         * schema
         */
        private String schema;

        /**
         * 物理名称
         */
        private String physicalName;

        /**
         * 描述
         */
        private String description;

        /**
         * 所有者
         */
        private String owner;

        /**
         * 安全级别
         */
        private String securityLevel;

        /**
         * 是否为根节点
         */
        private Boolean isRoot;

        /**
         * 距离根节点的深度
         */
        private Integer level;

        /**
         * 附加属性
         */
        private Map<String, Object> attributes;
    }

    /**
     * 血缘边
     */
    @Data
    public static class LineageEdge {
        /**
         * 源节点ID
         */
        private String sourceId;

        /**
         * 目标节点ID
         */
        private String targetId;

        /**
         * 血缘类型
         */
        private String lineageType;

        /**
         * 转换描述
         */
        private String transformation;

        /**
         * 关联的任务ID
         */
        private String taskId;

        /**
         * 任务名称
         */
        private String taskName;

        /**
         * 是否已验证
         */
        private Boolean verified;

        /**
         * 验证方法
         */
        private String verifyMethod;
    }
}
