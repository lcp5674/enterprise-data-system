package com.enterprise.dataplatform.index.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 资产索引文档 - Elasticsearch文档模型
 *
 * @author Team-D
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "asset_index")
@Setting(settingPath = "elasticsearch/asset-index-settings.json")
public class AssetIndexDocument {

    /**
     * 文档ID（对应ES自动生成的ID）
     */
    @Id
    private String id;

    /**
     * 资产ID
     */
    @Field(type = FieldType.Keyword)
    private String assetId;

    /**
     * 资产名称
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String name;

    /**
     * 资产描述
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String description;

    /**
     * 领域编码
     */
    @Field(type = FieldType.Keyword)
    private String domainCode;

    /**
     * 领域名称
     */
    @Field(type = FieldType.Keyword)
    private String domainName;

    /**
     * 对象类型：TABLE, FILE, API, MODEL, etc.
     */
    @Field(type = FieldType.Keyword)
    private String objectType;

    /**
     * 标签列表
     */
    @Field(type = FieldType.Keyword)
    private List<String> tags;

    /**
     * 负责人
     */
    @Field(type = FieldType.Keyword)
    private String owner;

    /**
     * 负责人名称
     */
    @Field(type = FieldType.Text)
    private String ownerName;

    /**
     * 状态：DRAFT, PUBLISHED, DEPRECATED, ARCHIVED
     */
    @Field(type = FieldType.Keyword)
    private String status;

    /**
     * 质量评分（0-100）
     */
    @Field(type = FieldType.Double)
    private Double qualityScore;

    /**
     * 敏感度等级：PUBLIC, INTERNAL, CONFIDENTIAL, HIGHLY_CONFIDENTIAL
     */
    @Field(type = FieldType.Keyword)
    private String sensitivity;

    /**
     * 创建时间
     */
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    private LocalDateTime updatedAt;

    /**
     * 创建者
     */
    @Field(type = FieldType.Keyword)
    private String createdBy;

    /**
     * 更新者
     */
    @Field(type = FieldType.Keyword)
    private String updatedBy;

    /**
     * 业务分类
     */
    @Field(type = FieldType.Keyword)
    private String businessCategory;

    /**
     * 技术类型
     */
    @Field(type = FieldType.Keyword)
    private String techType;

    /**
     * 数据源类型
     */
    @Field(type = FieldType.Keyword)
    private String dataSourceType;

    /**
     * 数据表名（针对数据库表）
     */
    @Field(type = FieldType.Keyword)
    private String tableName;

    /**
     * 存储路径（针对文件）
     */
    @Field(type = FieldType.Keyword)
    private String storagePath;

    /**
     * API路径（针对API服务）
     */
    @Field(type = FieldType.Keyword)
    private String apiPath;

    /**
     * 版本号
     */
    @Field(type = FieldType.Keyword)
    private String version;

    /**
     * 访问次数
     */
    @Field(type = FieldType.Long)
    private Long accessCount;

    /**
     * 收藏次数
     */
    @Field(type = FieldType.Long)
    private Long favoriteCount;

    /**
     * 扩展属性（用于存储额外的元数据）
     */
    @Field(type = FieldType.Object)
    private Map<String, Object> attributes;

    /**
     * 组合搜索字段（用于全文搜索）
     * 将 name, description, tags, ownerName 等字段组合
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String fullText;

    /**
     * 生成fullText字段
     */
    public void generateFullText() {
        StringBuilder sb = new StringBuilder();
        if (name != null) sb.append(name).append(" ");
        if (description != null) sb.append(description).append(" ");
        if (tags != null && !tags.isEmpty()) sb.append(String.join(" ", tags)).append(" ");
        if (ownerName != null) sb.append(ownerName).append(" ");
        if (domainName != null) sb.append(domainName).append(" ");
        if (businessCategory != null) sb.append(businessCategory).append(" ");
        this.fullText = sb.toString().trim();
    }
}
