package com.enterprise.edams.workflow.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.enterprise.edams.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 审批任务实体
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("wf_task")
public class ApprovalTask extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 流程实例ID（关联wf_instance.id） */
    private Long instanceId;

    /** Flowable任务ID */
    private String flowableTaskId;

    /** 任务名称/节点名称 */
    private String taskName;

    /** 任务定义Key */
    private String taskDefKey;

    /** 处理人ID */
    private Long assigneeId;

    /** 处理人姓名 */
    private String assigneeName;

    /** 候选组ID列表JSON */
    private String candidateGroups;

    /** 候选人ID列表JSON */
    private String candidateUsers;

    /** 任务状态：0-待处理，1-已处理，2-已委托，3-已转办，4-已驳回 */
    private Integer status;

    /** 审批结果：1-同意，2-拒绝，3-驳回，4-撤回 */
    private Integer result;

    /** 审批意见 */
    private String comment;

    /** 附件路径JSON */
    private String attachments;

    /** 处理时间 */
    private LocalDateTime completedTime;

    /** 到期时间 */
    private LocalDateTime dueDate;

    /** 是否超时：0-未超时，1-已超时 */
    private Integer isTimeout;
}
