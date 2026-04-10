package com.enterprise.edams.workflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 流程任务实体
 * 
 * @author EDAMS Team
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("wf_process_task")
public class ProcessTask {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * Flowable任务ID
     */
    private String flowableTaskId;

    /**
     * 流程实例ID
     */
    private String processInstanceId;

    /**
     * 流程定义ID
     */
    private String processDefinitionId;

    /**
     * 任务节点ID
     */
    private String taskNodeId;

    /**
     * 任务节点名称
     */
    private String taskNodeName;

    /**
     * 任务类型：1-审批，2-抄送，3-会签
     */
    private Integer taskType;

    /**
     * 处理人ID
     */
    private String assigneeId;

    /**
     * 处理人名称
     */
    private String assigneeName;

    /**
     * 候选人ID列表（逗号分隔）
     */
    private String candidateIds;

    /**
     * 候选人名称列表（逗号分隔）
     */
    private String candidateNames;

    /**
     * 任务状态：0-待处理，1-已处理，2-已转办，3-已委托
     */
    private Integer status;

    /**
     * 审批结果：0-通过，1-拒绝，2-退回，3-转办，4-委托
     */
    private Integer result;

    /**
     * 审批意见
     */
    private String comment;

    /**
     * 表单数据JSON
     */
    private String formData;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /**
     * 截止时间
     */
    private LocalDateTime dueTime;

    /**
     * 处理时间
     */
    private LocalDateTime handleTime;

    /**
     * 提醒次数
     */
    private Integer reminderCount;

    /**
     * 最后提醒时间
     */
    private LocalDateTime lastReminderTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Boolean deleted;
}
