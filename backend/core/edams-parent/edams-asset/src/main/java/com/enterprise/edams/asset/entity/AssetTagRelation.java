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
 * 资产标签关联实体
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("edams_asset_tag_relation")
public class AssetTagRelation extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 资产ID */
    @TableField("asset_id")
    private Long assetId;

    /** 标签ID */
    @TableField("tag_id")
    private Long tagId;

    /** 打标签人ID */
    @TableField("tagger_id")
    private Long taggerId;

    /** 打标签时间 */
    @TableField("tag_time")
    private LocalDateTime tagTime;
}
