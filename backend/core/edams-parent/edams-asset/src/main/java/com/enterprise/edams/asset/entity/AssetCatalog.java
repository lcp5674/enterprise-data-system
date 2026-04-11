package com.enterprise.edams.asset.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.enterprise.edams.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 资产目录实体
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("edams_asset_catalog")
public class AssetCatalog extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 目录名称 */
    @TableField("name")
    private String name;

    /** 目录编码 */
    @TableField("code")
    private String code;

    /** 父目录ID(0表示根目录) */
    @TableField("parent_id")
    private Long parentId;

    /** 目录层级 */
    @TableField("level")
    private Integer level;

    /** 目录路径 */
    @TableField("path")
    private String path;

    /** 排序号 */
    @TableField("sort_order")
    private Integer sortOrder;

    /** 目录描述 */
    @TableField("description")
    private String description;

    /** 图标 */
    @TableField("icon")
    private String icon;

    /** 状态: 0-禁用, 1-启用 */
    @TableField("status")
    private Integer status;

    /** 资产数量统计 */
    @TableField(exist = false)
    private Integer assetCount;

    /** 是否叶子节点 */
    @TableField(exist = false)
    private Boolean isLeaf;
}
