package com.enterprise.edams.workflow.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.enterprise.edams.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 工作流实例实体
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("wf_instance")
public class WorkflowInstance extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 流程定义ID（关联wf_definition.id） */
    private Long definitionId;

    /** Flowable流程实例ID */
    private String processInstanceId;

    /** 流程定义编码 */
    private String processDefKey;

    /** 业务标题 */
    private String businessTitle;

    /** 业务类型 */
    private Integer businessType;

    /** 业务关联键 */
    private String businessKey;

    /** 发起人ID */
    private Long initiatorId;

    /** 发起人姓名 */
    private String initiatorName;

    /** 发起人部门 */
    private String initiatorDept;

    /** 当前节点名称 */
    private String currentNodeName;

    /** 当前处理人列表JSON */
    private String currentAssignees;

    /** 状态：0-运行中，1-已完成，2-已撤销，3-已驳回，4-已超时 */
    private Integer status;

    /** 优先级：1-普通，2-紧急，3-特急 */
    private Integer priority;

    /** 业务表单数据JSON */
    private String formData;

    /** 完成时间 */
    private LocalDateTime completedTime;

    /** 总耗时（毫秒） */
    private Long durationMs;
}
