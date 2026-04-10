package com.enterprise.edams.workflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 审批规则实体
 * 
 * @author EDAMS Team
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("wf_approval_rule")
public class ApprovalRule {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 规则名称
     */
    private String name;

    /**
     * 规则编码
     */
    private String code;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 流程定义ID
     */
    private String processDefinitionId;

    /**
     * 规则条件JSON
     */
    private String conditions;

    /**
     * 审批人配置JSON
     */
    private String approverConfig;

    /**
     * 审批方式：1-或签（一人通过即可），2-会签（全部通过），3-顺序签
     */
    private Integer approvalMode;

    /**
     * 会签通过比例（百分比）
     */
    private Integer signPassRate;

    /**
     * 超时时间（小时）
     */
    private Integer timeoutHours;

    /**
     * 超时处理方式：1-自动通过，2-自动拒绝，3-提醒上级
     */
    private Integer timeoutAction;

    /**
     * 是否允许转办
     */
    private Boolean allowTransfer;

    /**
     * 是否允许委托
     */
    private Boolean allowDelegate;

    /**
     * 是否允许退回
     */
    private Boolean allowBack;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 优先级
     */
    private Integer priority;

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
     * 更新人
     */
    private String updatedBy;

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
