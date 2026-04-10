package com.enterprise.edams.workflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 流程历史记录实体
 * 
 * @author EDAMS Team
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("wf_process_history")
public class ProcessHistory {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 流程实例ID
     */
    private String processInstanceId;

    /**
     * 流程任务ID
     */
    private String processTaskId;

    /**
     * 流程定义ID
     */
    private String processDefinitionId;

    /**
     * 节点ID
     */
    private String nodeId;

    /**
     * 节点名称
     */
    private String nodeName;

    /**
     * 节点类型：1-开始节点，2-任务节点，3-网关，4-结束节点
     */
    private Integer nodeType;

    /**
     * 操作人类型：1-用户，2-系统
     */
    private Integer operatorType;

    /**
     * 操作人ID
     */
    private String operatorId;

    /**
     * 操作人名称
     */
    private String operatorName;

    /**
     * 操作类型：1-发起，2-审批，3-转办，4-委托，5-退回，6-终止，7-撤回
     */
    private Integer operationType;

    /**
     * 操作结果：0-通过，1-拒绝，2-退回，3-转办，4-委托，5-终止
     */
    private Integer operationResult;

    /**
     * 操作意见
     */
    private String comment;

    /**
     * 表单数据JSON
     */
    private String formData;

    /**
     * 流程变量JSON
     */
    private String variables;

    /**
     * 操作时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime operationTime;

    /**
     * 持续时间（毫秒）
     */
    private Long duration;

    /**
     * 是否删除
     */
    @TableLogic
    private Boolean deleted;
}
