package com.enterprise.edams.workflow.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.enterprise.edams.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 工作流定义实体
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("wf_definition")
public class WorkflowDefinition extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 流程名称 */
    private String name;

    /** 流程编码（唯一标识） */
    private String code;

    /** 流程类型：1-资产审批，2-数据申请，3-权限申请，4-通用审批 */
    private Integer type;

    /** 描述 */
    private String description;

    /** Flowable流程定义Key（部署后由Flowable生成） */
    private String processDefKey;

    /** Flowable流程定义ID */
    private String processDefId;

    /** 版本号（Flowable版本） */
    private Integer version;

    /** 状态：0-草稿，1-已发布，2-已禁用，3-已归档 */
    private Integer status;

    /** 流程定义XML/BPMN内容 */
    private String bpmnContent;

    /** 流程表单配置JSON */
    private String formConfig;

    /** 是否允许发起人撤销 */
    private Integer allowCancel;

    /** 是否允许加签 */
    private Integer allowAddSignee;

    /** 是否允许转办 */
    private Integer allowDelegate;

    /** 默认处理超时时间（小时） */
    private Integer timeoutHours;
}
