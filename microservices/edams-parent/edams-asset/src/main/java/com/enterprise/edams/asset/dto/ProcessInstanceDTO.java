package com.enterprise.edams.asset.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 流程实例DTO
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Data
public class ProcessInstanceDTO {

    /**
     * 流程实例ID
     */
    private String instanceId;

    /**
     * 流程定义ID
     */
    private String processDefinitionId;

    /**
     * 流程名称
     */
    private String processName;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 业务ID
     */
    private String businessId;

    /**
     * 发起人ID
     */
    private String startUserId;

    /**
     * 发起人名称
     */
    private String startUserName;

    /**
     * 当前节点ID
     */
    private String currentNodeId;

    /**
     * 当前节点名称
     */
    private String currentNodeName;

    /**
     * 流程状态: PENDING=待审批, APPROVED=已通过, REJECTED=已拒绝, TERMINATED=已终止, COMPLETED=已完成
     */
    private Integer status;

    /**
     * 发起时间
     */
    private LocalDateTime startTime;

    /**
     * 完成时间
     */
    private LocalDateTime endTime;

    /**
     * 耗时(秒)
     */
    private Long duration;

    /**
     * 流程变量
     */
    private Map<String, Object> variables;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
