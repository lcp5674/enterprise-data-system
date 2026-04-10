package com.enterprise.edams.version.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 版本记录实体
 * 
 * @author EDAMS Team
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("ver_version_record")
public class VersionRecord {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 业务ID
     */
    private String businessId;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 版本标签
     */
    private String versionTag;

    /**
     * 版本说明
     */
    private String versionComment;

    /**
     * 数据内容JSON
     */
    private String dataContent;

    /**
     * 数据摘要（MD5）
     */
    private String dataDigest;

    /**
     * 变更类型：1-创建，2-更新，3-删除
     */
    private Integer changeType;

    /**
     * 变更内容摘要
     */
    private String changeSummary;

    /**
     * 变更详情JSON（diff格式）
     */
    private String changeDetail;

    /**
     * 创建人ID
     */
    private String createdBy;

    /**
     * 创建人名称
     */
    private String createdByName;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Boolean deleted;
}
