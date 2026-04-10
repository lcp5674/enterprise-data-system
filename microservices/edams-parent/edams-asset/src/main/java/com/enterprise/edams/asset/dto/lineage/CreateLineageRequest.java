package com.enterprise.edams.asset.dto.lineage;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 创建血缘关系请求DTO
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Data
public class CreateLineageRequest {

    /**
     * 源资产ID
     */
    @NotBlank(message = "源资产ID不能为空")
    private String sourceAssetId;

    /**
     * 源资产名称
     */
    private String sourceAssetName;

    /**
     * 源资产类型
     */
    private String sourceAssetType;

    /**
     * 目标资产ID
     */
    @NotBlank(message = "目标资产ID不能为空")
    private String targetAssetId;

    /**
     * 目标资产名称
     */
    private String targetAssetName;

    /**
     * 目标资产类型
     */
    private String targetAssetType;

    /**
     * 血缘类型: TABLE, FIELD, PROCESS
     */
    @NotBlank(message = "血缘类型不能为空")
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
     * 字段级血缘映射
     */
    private List<FieldLineageMapping> fieldMappings;

    /**
     * 附加属性
     */
    private Map<String, String> attributes;

    /**
     * 字段级血缘映射
     */
    @Data
    public static class FieldLineageMapping {
        private String sourceField;
        private String targetField;
        private String transformation;
    }
}
