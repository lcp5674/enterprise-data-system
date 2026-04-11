package com.enterprise.dataplatform.index.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 资产事件 - Kafka消息模型
 *
 * @author Team-D
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetEvent {

    /**
     * 事件类型：CREATED, UPDATED, DELETED
     */
    private String eventType;

    /**
     * 资产ID
     */
    private String assetId;

    /**
     * 资产名称
     */
    private String name;

    /**
     * 资产描述
     */
    private String description;

    /**
     * 领域编码
     */
    private String domainCode;

    /**
     * 领域名称
     */
    private String domainName;

    /**
     * 对象类型
     */
    private String objectType;

    /**
     * 标签列表
     */
    private String[] tags;

    /**
     * 负责人
     */
    private String owner;

    /**
     * 负责人名称
     */
    private String ownerName;

    /**
     * 状态
     */
    private String status;

    /**
     * 质量评分
     */
    private Double qualityScore;

    /**
     * 敏感度等级
     */
    private String sensitivity;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 创建者
     */
    private String createdBy;

    /**
     * 业务分类
     */
    private String businessCategory;

    /**
     * 技术类型
     */
    private String techType;

    /**
     * 数据源类型
     */
    private String dataSourceType;

    /**
     * 数据表名
     */
    private String tableName;

    /**
     * 存储路径
     */
    private String storagePath;

    /**
     * API路径
     */
    private String apiPath;

    /**
     * 版本号
     */
    private String version;

    /**
     * 扩展属性
     */
    private Map<String, Object> attributes;

    /**
     * 事件发生时间
     */
    private LocalDateTime eventTime;
}
