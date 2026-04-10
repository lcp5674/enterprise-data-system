package com.enterprise.edams.knowledge.dto;

import lombok.Data;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 数据导入请求DTO
 *
 * @author Knowledge Team
 * @version 1.0.0
 */
@Data
@Builder
public class DataImportDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 图谱ID
     */
    @NotBlank(message = "图谱ID不能为空")
    private String graphId;

    /**
     * 导入类型: EXCEL, CSV, JSON, EXTERNAL_API
     */
    @NotBlank(message = "导入类型不能为空")
    private String importType;

    /**
     * 数据文件URL (外部导入时使用)
     */
    private String fileUrl;

    /**
     * 数据内容 (直接导入时使用)
     */
    private String dataContent;

    /**
     * 节点配置
     */
    private NodeImportConfig nodeConfig;

    /**
     * 边配置
     */
    private EdgeImportConfig edgeConfig;

    /**
     * 导入选项
     */
    private ImportOptions options;

    /**
     * 节点导入配置
     */
    @Data
    @Builder
    public static class NodeImportConfig implements Serializable {
        /**
         * 节点类型
         */
        @NotBlank(message = "节点类型不能为空")
        private String nodeType;

        /**
         * 节点名称字段
         */
        @NotBlank(message = "节点名称字段不能为空")
        private String nameField;

        /**
         * 字段映射
         */
        private Map<String, String> fieldMapping;

        /**
         * 额外属性
         */
        private Map<String, Object> extraProperties;
    }

    /**
     * 边导入配置
     */
    @Data
    @Builder
    public static class EdgeImportConfig implements Serializable {
        /**
         * 关系类型
         */
        @NotBlank(message = "关系类型不能为空")
        private String relationType;

        /**
         * 源节点字段
         */
        @NotBlank(message = "源节点字段不能为空")
        private String sourceField;

        /**
         * 目标节点字段
         */
        @NotBlank(message = "目标节点字段不能为空")
        private String targetField;

        /**
         * 字段映射
         */
        private Map<String, String> fieldMapping;

        /**
         * 额外属性
         */
        private Map<String, Object> extraProperties;
    }

    /**
     * 导入选项
     */
    @Data
    @Builder
    public static class ImportOptions implements Serializable {
        /**
         * 是否去重
         */
        @Builder.Default
        private Boolean deduplicate = true;

        /**
         * 是否更新已存在记录
         */
        @Builder.Default
        private Boolean updateExisting = true;

        /**
         * 批量大小
         */
        @Builder.Default
        private Integer batchSize = 500;

        /**
         * 导入模式: MERGE-合并, REPLACE-替换
         */
        @Builder.Default
        private String mode = "MERGE";
    }
}
