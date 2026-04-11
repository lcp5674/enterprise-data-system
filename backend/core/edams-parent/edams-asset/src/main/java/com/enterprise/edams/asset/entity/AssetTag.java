package com.enterprise.edams.asset.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.enterprise.edams.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 资产标签实体
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("edams_asset_tag")
public class AssetTag extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 标签名称 */
    @TableField("tag_name")
    private String tagName;

    /** 标签编码 */
    @TableField("tag_code")
    private String tagCode;

    /** 标签颜色 */
    @TableField("color")
    private String color;

    /** 标签描述 */
    @TableField("description")
    private String description;

    /** 所属分类 */
    @TableField("category")
    private String category;

    /** 状态: 0-禁用, 1-启用 */
    @TableField("status")
    private Integer status;

    /** 使用次数统计 */
    @TableField("usage_count")
    private Integer usageCount;

    /** 创建人ID */
    @TableField("creator_id")
    private Long creatorId;

    /** 最后使用时间 */
    @TableField("last_used_time")
    private LocalDateTime lastUsedTime;
}
