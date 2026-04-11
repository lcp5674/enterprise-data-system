package com.enterprise.edams.asset.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.enterprise.edams.common.entity.BaseEntity;
import com.enterprise.edams.common.enums.AssetStatus;
import com.enterprise.edams.common.enums.AssetType;
import com.enterprise.edams.common.enums.DataSensitivity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * 数据资产实体
 *
 * @author Architecture Team
 * @version 1.0.0
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@TableName("data_asset")
public class DataAsset extends BaseEntity {

    /**
     * 资产名称
     */
    @TableField("asset_name")
    private String assetName;

    /**
     * 资产类型
     */
    @TableField("asset_type")
    private AssetType assetType;

    /**
     * 资产描述
     */
    @TableField("description")
    private String description;

    /**
     * 负责人ID
     */
    @TableField("owner_id")
    private Long ownerId;

    /**
     * 敏感级别
     */
    @TableField("sensitivity")
    private DataSensitivity sensitivity = DataSensitivity.INTERNAL;

    /**
     * 资产状态
     */
    @TableField("status")
    private AssetStatus status = AssetStatus.DRAFT;

    /**
     * 数据源ID
     */
    @TableField("datasource_id")
    private Long datasourceId;

    /**
     * 数据库名称
     */
    @TableField("database_name")
    private String databaseName;

    /**
     * Schema名称
     */
    @TableField("schema_name")
    private String schemaName;

    /**
     * 表名称
     */
    @TableField("table_name")
    private String tableName;

    /**
     * 字段数量
     */
    @TableField("column_count")
    private Integer columnCount = 0;

    /**
     * 行数
     */
    @TableField("row_count")
    private Long rowCount = 0L;

    /**
     * 数据大小(字节)
     */
    @TableField("data_size")
    private Long dataSize = 0L;

    /**
     * 质量评分
     */
    @TableField("quality_score")
    private BigDecimal qualityScore = BigDecimal.ZERO;

    /**
     * 标签(JSON数组)
     */
    @TableField("tags")
    private String tags;

    /**
     * 扩展属性(JSON对象)
     */
    @TableField("properties")
    private String properties;

    /**
     * 判断资产是否可修改
     */
    public boolean isImmutable() {
        return this.status == AssetStatus.PUBLISHED 
            || this.status == AssetStatus.ARCHIVED;
    }

    /**
     * 判断资产是否受保护
     */
    public boolean isProtected() {
        return this.sensitivity == DataSensitivity.SECRET;
    }
}
