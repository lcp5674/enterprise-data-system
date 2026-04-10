package com.enterprise.edams.lifecycle.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 归档记录DTO
 * 
 * @author EDAMS Team
 */
@Data
@Schema(description = "归档记录")
public class ArchiveRecordDTO {

    @Schema(description = "记录ID")
    private String id;

    @Schema(description = "归档策略ID")
    private String policyId;

    @Schema(description = "业务类型")
    private String businessType;

    @Schema(description = "业务ID")
    private String businessId;

    @Schema(description = "业务名称")
    private String businessName;

    @Schema(description = "数据类型")
    private String dataType;

    @Schema(description = "归档文件数量")
    private Integer fileCount;

    @Schema(description = "归档数据大小（字节）")
    private Long dataSize;

    @Schema(description = "归档文件名称")
    private String archiveFileName;

    @Schema(description = "归档文件URL")
    private String archiveUrl;

    @Schema(description = "校验和（MD5）")
    private String checksum;

    @Schema(description = "压缩格式")
    private String compressionFormat;

    @Schema(description = "归档状态：0-待归档，1-归档中，2-归档成功，3-归档失败，4-已恢复")
    private Integer status;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "归档开始时间")
    private LocalDateTime archiveStartTime;

    @Schema(description = "归档结束时间")
    private LocalDateTime archiveEndTime;

    @Schema(description = "归档耗时（毫秒）")
    private Long archiveDuration;

    @Schema(description = "恢复时间")
    private LocalDateTime restoreTime;

    @Schema(description = "恢复人")
    private String restoreBy;

    @Schema(description = "保留到期时间")
    private LocalDateTime retentionExpireTime;

    @Schema(description = "创建时间")
    private LocalDateTime createdTime;
}
