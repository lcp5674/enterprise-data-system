package com.enterprise.dataplatform.standard.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 标准版本实体
 * 记录数据标准的版本变更历史
 */
@Entity
@Table(name = "standard_version", indexes = {
    @Index(name = "idx_version_standard", columnList = "standard_id"),
    @Index(name = "idx_version_no", columnList = "version_no")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StandardVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 数据标准ID
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "standard_id", nullable = false)
    private DataStandard dataStandard;

    /**
     * 版本号
     */
    @Column(name = "version_no", nullable = false)
    private Integer versionNo;

    /**
     * 版本说明
     */
    @Column(name = "version_description", columnDefinition = "TEXT")
    private String versionDescription;

    /**
     * 变更类型：CREATE、UPDATE、DEPRECATE、ARCHIVE
     */
    @Column(name = "change_type", nullable = false, length = 32)
    private String changeType;

    /**
     * 变更内容（JSON格式）
     */
    @Column(name = "change_content", columnDefinition = "TEXT")
    private String changeContent;

    /**
     * 变更前内容（JSON格式）
     */
    @Column(name = "before_content", columnDefinition = "TEXT")
    private String beforeContent;

    /**
     * 变更后内容（JSON格式）
     */
    @Column(name = "after_content", columnDefinition = "TEXT")
    private String afterContent;

    /**
     * 变更原因
     */
    @Column(name = "change_reason", columnDefinition = "TEXT")
    private String changeReason;

    /**
     * 版本状态：DRAFT、ACTIVE、HISTORICAL
     */
    @Column(name = "status", nullable = false, length = 32)
    private String status;

    /**
     * 是否需要审批
     */
    @Column(name = "requires_approval")
    private Boolean requiresApproval;

    /**
     * 审批状态
     */
    @Column(name = "approval_status", length = 32)
    private String approvalStatus;

    /**
     * 审批人
     */
    @Column(name = "approver", length = 64)
    private String approver;

    /**
     * 审批时间
     */
    @Column(name = "approve_time")
    private LocalDateTime approveTime;

    /**
     * 审批意见
     */
    @Column(name = "approval_comment", columnDefinition = "TEXT")
    private String approvalComment;

    /**
     * 创建人
     */
    @Column(name = "creator", length = 64)
    private String creator;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    /**
     * 生效时间
     */
    @Column(name = "effective_time")
    private LocalDateTime effectiveTime;

    /**
     * 失效时间
     */
    @Column(name = "expiry_time")
    private LocalDateTime expiryTime;
}
