package com.enterprise.edams.workflow.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.enterprise.edams.workflow.dto.ProcessInstanceDTO;
import com.enterprise.edams.workflow.dto.ProcessInstanceStartRequest;
import com.enterprise.edams.workflow.entity.ProcessInstance;

import java.util.List;

/**
 * 流程实例服务接口
 * 
 * @author EDAMS Team
 */
public interface ProcessInstanceService extends IService<ProcessInstance> {

    /**
     * 启动流程实例
     */
    ProcessInstanceDTO startProcessInstance(ProcessInstanceStartRequest request, String starterId, String starterName);

    /**
     * 终止流程实例
     */
    void terminateProcessInstance(String instanceId, String reason, String operatorId);

    /**
     * 撤回流程实例
     */
    void revokeProcessInstance(String instanceId, String reason, String operatorId);

    /**
     * 挂起流程实例
     */
    void suspendProcessInstance(String instanceId, String operatorId);

    /**
     * 激活流程实例
     */
    void activateProcessInstance(String instanceId, String operatorId);

    /**
     * 获取流程实例详情
     */
    ProcessInstanceDTO getProcessInstance(String instanceId);

    /**
     * 分页查询流程实例
     */
    Page<ProcessInstanceDTO> listProcessInstances(Page<ProcessInstance> page, String keyword, Integer status, String businessType);

    /**
     * 查询用户发起的流程实例
     */
    Page<ProcessInstanceDTO> listMyStartedInstances(Page<ProcessInstance> page, String starterId, Integer status);

    /**
     * 查询待我审批的流程实例
     */
    List<ProcessInstanceDTO> listPendingApproval(String userId);

    /**
     * 查询我已审批的流程实例
     */
    List<ProcessInstanceDTO> listApprovedByMe(String userId);
}
