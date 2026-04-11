package com.enterprise.dataplatform.standard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 标准版本响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StandardVersionResponse {

    /**
     * 版本ID
     */
    private Long id;

    /**
     * 数据标准ID
     */
    private Long standardId;

    /**
     * 版本号
     */
    private Integer versionNo;

    /**
     * 版本说明
     */
    private String versionDescription;

    /**
     * 变更类型
     */
    private String changeType;

    /**
     * 变更内容
     */
    private String changeContent;

    /**
     * 变更前内容
     */
    private String beforeContent;

    /**
     * 变更后内容
     */
    private String afterContent;

    /**
     * 变更原因
     */
    private String changeReason;

    /**
     * 版本状态
     */
    private String status;

    /**
     * 是否需要审批
     */
    private Boolean requiresApproval;

    /**
     * 审批状态
     */
    private String approvalStatus;

    /**
     * 审批人
     */
    private String approver;

    /**
     * 审批时间
     */
    private LocalDateTime approveTime;

    /**
     * 审批意见
     */
    private String approvalComment;

    /**
     * 创建人
     */
    private String creator;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 生效时间
     */
    private LocalDateTime effectiveTime;

    /**
     * 失效时间
     */
    private LocalDateTime expiryTime;
}
