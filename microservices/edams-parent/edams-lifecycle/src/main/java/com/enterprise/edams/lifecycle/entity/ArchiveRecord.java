package com.enterprise.edams.lifecycle.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 归档记录实体
 * 
 * @author EDAMS Team
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("lc_archive_record")
public class ArchiveRecord {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 归档策略ID
     */
    private String policyId;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 业务ID
     */
    private String businessId;

    /**
     * 业务名称
     */
    private String businessName;

    /**
     * 数据类型
     */
    private String dataType;

    /**
     * 归档文件数量
     */
    private Integer fileCount;

    /**
     * 归档数据大小（字节）
     */
    private Long dataSize;

    /**
     * 归档前存储路径
     */
    private String sourcePath;

    /**
     * 归档后存储路径
     */
    private String archivePath;

    /**
     * 归档文件名称
     */
    private String archiveFileName;

    /**
     * 归档文件URL
     */
    private String archiveUrl;

    /**
     * 校验和（MD5）
     */
    private String checksum;

    /**
     * 压缩格式
     */
    private String compressionFormat;

    /**
     * 加密算法
     */
    private String encryptionAlgorithm;

    /**
     * 归档状态：0-待归档，1-归档中，2-归档成功，3-归档失败，4-已恢复
     */
    private Integer status;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 归档开始时间
     */
    private LocalDateTime archiveStartTime;

    /**
     * 归档结束时间
     */
    private LocalDateTime archiveEndTime;

    /**
     * 归档耗时（毫秒）
     */
    private Long archiveDuration;

    /**
     * 恢复时间
     */
    private LocalDateTime restoreTime;

    /**
     * 恢复人
     */
    private String restoreBy;

    /**
     * 保留到期时间
     */
    private LocalDateTime retentionExpireTime;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Boolean deleted;
}
