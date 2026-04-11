package com.enterprise.edams.lifecycle.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.enterprise.edams.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 归档记录实体
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("archive_record")
public class ArchiveRecord extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 数据生命周期ID */
    private Long lifecycleId;

    /** 数据资产ID */
    private Long dataAssetId;

    /** 数据资产名称 */
    private String dataAssetName;

    /** 归档类型：0-自动归档，1-手动归档 */
    private Integer archiveType;

    /** 归档状态：0-归档中，1-已归档，2-归档失败 */
    private Integer archiveStatus;

    /** 归档时间 */
    private LocalDateTime archiveTime;

    /** 归档操作人 */
    private String archiveOperator;

    /** 归档存储路径 */
    private String archiveStoragePath;

    /** 归档存储类型：0-本地存储，1-对象存储，2-云存储 */
    private Integer archiveStorageType;

    /** 归档文件大小（字节） */
    private Long archiveFileSize;

    /** 归档备注 */
    private String archiveRemark;

    /** 还原状态：0-未还原，1-已还原 */
    private Integer restoreStatus;

    /** 还原时间 */
    private LocalDateTime restoreTime;

    /** 还原操作人 */
    private String restoreOperator;

    /** 还原备注 */
    private String restoreRemark;

    /** 租户ID */
    private Long tenantId;
}