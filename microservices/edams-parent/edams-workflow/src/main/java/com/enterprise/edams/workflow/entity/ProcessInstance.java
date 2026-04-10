package com.enterprise.edams.workflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 流程实例实体
 * 
 * @author EDAMS Team
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("wf_process_instance")
public class ProcessInstance {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * Flowable流程实例ID
     */
    private String flowableInstanceId;

    /**
     * 流程定义ID
     */
    private String processDefinitionId;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 业务ID
     */
    private String businessId;

    /**
     * 业务标题
     */
    private String businessTitle;

    /**
     * 发起人ID
     */
    private String starterId;

    /**
     * 发起人名称
     */
    private String starterName;

    /**
     * 发起人部门ID
     */
    private String starterDeptId;

    /**
     * 发起人部门名称
     */
    private String starterDeptName;

    /**
     * 当前节点ID
     */
    private String currentNodeId;

    /**
     * 当前节点名称
     */
    private String currentNodeName;

    /**
     * 当前处理人ID
     */
    private String currentAssigneeId;

    /**
     * 当前处理人名称
     */
    private String currentAssigneeName;

    /**
     * 流程状态：0-运行中，1-已完成，2-已终止，3-已挂起
     */
    private Integer status;

    /**
     * 优先级：1-低，2-中，3-高，4-紧急
     */
    private Integer priority;

    /**
     * 表单数据JSON
     */
    private String formData;

    /**
     * 流程变量JSON
     */
    private String variables;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 持续时间（毫秒）
     */
    private Long duration;

    /**
     * 结果：0-通过，1-拒绝，2-撤回
     */
    private Integer result;

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
