package com.enterprise.edams.asset.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.enterprise.edams.common.entity.BaseEntity;
import com.enterprise.edams.common.enums.AssetStatus;
import com.enterprise.edams.common.enums.AssetType;
import com.enterprise.edams.common.enums.DataSensitivity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 资产实体
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("edams_asset")
public class Asset extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 资产名称 */
    @TableField("asset_name")
    private String assetName;

    /** 资产编码 */
    @TableField("asset_code")
    private String assetCode;

    /** 资产类型: DATABASE/TABLE/VIEW/FILE/API */
    @TableField("asset_type")
    private AssetType assetType;

    /** 资产描述 */
    @TableField("description")
    private String description;

    /** 负责人ID */
    @TableField("owner_id")
    private Long ownerId;

    /** 负责人名称 */
    @TableField("owner_name")
    private String ownerName;

    /** 敏感级别: PUBLIC/INTERNAL/CONFIDENTIAL/SECRET */
    @TableField("sensitivity")
    private DataSensitivity sensitivity;

    /** 资产状态: DRAFT/REVIEWING/PUBLISHED/ARCHIVED/DEPRECATED */
    @TableField("status")
    private AssetStatus status;

    /** 数据源ID */
    @TableField("datasource_id")
    private Long datasourceId;

    /** 数据源名称 */
    @TableField("datasource_name")
    private String datasourceName;

    /** 数据库名称 */
    @TableField("database_name")
    private String databaseName;

    /** Schema名称 */
    @TableField("schema_name")
    private String schemaName;

    /** 表/视图名称 */
    @TableField("table_name")
    private String tableName;

    /** 字段数量 */
    @TableField("column_count")
    private Integer columnCount;

    /** 数据行数 */
    @TableField("row_count")
    private Long rowCount;

    /** 数据大小(字节) */
    @TableField("data_size")
    private Long dataSize;

    /** 质量评分(0-100) */
    @TableField("quality_score")
    private BigDecimal qualityScore;

    /** 所属目录ID */
    @TableField("catalog_id")
    private Long catalogId;

    /** 所属业务域ID */
    @TableField("domain_id")
    private Long domainId;

    /** 所属业务域名称 */
    @TableField("domain_name")
    private String domainName;

    /** 标签(JSON数组) */
    @TableField("tags")
    private String tags;

    /** 扩展属性(JSON对象) */
    @TableField("properties")
    private String properties;

    /** 最后同步时间 */
    @TableField("last_sync_time")
    private LocalDateTime lastSyncTime;

    /** 发布时间 */
    @TableField("publish_time")
    private LocalDateTime publishTime;

    /** 版本号 */
    @TableField("version")
    private Integer version;

    /** 是否可编辑 */
    public boolean isEditable() {
        return this.status == AssetStatus.DRAFT || this.status == AssetStatus.REVIEWING;
    }

    /** 是否已发布 */
    public boolean isPublished() {
        return this.status == AssetStatus.PUBLISHED;
    }

    /** 是否敏感 */
    public boolean isSensitive() {
        return this.sensitivity == DataSensitivity.SECRET 
            || this.sensitivity == DataSensitivity.CONFIDENTIAL;
    }
}
