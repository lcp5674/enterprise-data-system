package com.enterprise.edams.workflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.common.exception.BusinessException;
import com.enterprise.edams.common.feign.UserFeignClient;
import com.enterprise.edams.workflow.entity.ApprovalTask;
import com.enterprise.edams.workflow.entity.WorkflowDefinition;
import com.enterprise.edams.workflow.entity.WorkflowInstance;
import com.enterprise.edams.workflow.repository.ApprovalTaskMapper;
import com.enterprise.edams.workflow.repository.WorkflowDefinitionMapper;
import com.enterprise.edams.workflow.repository.WorkflowInstanceMapper;
import com.enterprise.edams.workflow.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance as FlowableProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 审批服务实现
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalServiceImpl implements ApprovalService {

    private final WorkflowInstanceMapper instanceMapper;
    private final WorkflowDefinitionMapper definitionMapper;
    private final ApprovalTaskMapper taskMapper;
    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final UserFeignClient userFeignClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkflowInstance startProcess(Long definitionId, Long initiatorId,
                                          String businessTitle, String formData) {
        // 1. 验证流程定义
        WorkflowDefinition def = definitionMapper.selectById(definitionId);
        if (def == null || def.getStatus() != 1) {
            throw new BusinessException("流程定义不存在或未发布");
        }

        // 2. 获取发起人信息
        String initiatorName = "用户" + initiatorId;
        String initiatorDept = "未知部门";
        try {
            var userInfo = userFeignClient.getUserById(initiatorId);
            if (userInfo != null) {
                initiatorName = userInfo.getRealName() != null ? userInfo.getRealName() : userInfo.getUsername();
                if (userInfo.getDepartmentName() != null) {
                    initiatorDept = userInfo.getDepartmentName();
                }
            }
        } catch (Exception e) {
            log.warn("获取用户信息失败, userId={}: {}", initiatorId, e.getMessage());
        }

        // 3. 创建流程实例
        WorkflowInstance instance = new WorkflowInstance();
        instance.setDefinitionId(definitionId);
        instance.setProcessDefKey(def.getCode());
        instance.setBusinessTitle(businessTitle != null ? businessTitle : def.getName());
        instance.setBusinessType(def.getType());
        instance.setBusinessKey(UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        instance.setInitiatorId(initiatorId);
        instance.setInitiatorName(initiatorName);
        instance.setInitiatorDept(initiatorDept);
        instance.setCurrentNodeName("开始");
        instance.setStatus(0); // 运行中
        instance.setPriority(1);
        instance.setFormData(formData);

        // 4. 启动Flowable流程
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("initiatorId", initiatorId);
            variables.put("initiatorName", initiatorName);
            variables.put("businessTitle", businessTitle);

            FlowableProcessInstance flowableInstance = runtimeService
                    .startProcessInstanceByKey(def.getCode(), instance.getBusinessKey(), variables);

            instance.setProcessInstanceId(flowableInstance.getId());
            log.info("Flowable流程实例已启动: {}", flowableInstance.getId());

            // 查找第一个用户任务并创建审批任务记录
            createApprovalTasksFromFlowable(instance.getId(), flowableInstance.getId());

        } catch (Exception e) {
            log.error("Flowable流程启动失败: {}", e.getMessage(), e);
            throw new BusinessException("流程启动失败: " + e.getMessage());
        }

        instanceMapper.insert(instance);

        log.info("流程实例已启动: {} - {} ({})", def.getCode(), businessTitle, instance.getId());
        return instance;
    }

    /**
     * 从Flowable流程实例创建审批任务记录
     */
    private void createApprovalTasksFromFlowable(Long instanceId, String flowableProcessInstanceId) {
        // 查询Flowable中的用户任务
        List<Task> tasks = taskService.createTaskQuery()
                .processInstanceId(flowableProcessInstanceId)
                .list();

        for (Task task : tasks) {
            ApprovalTask approvalTask = new ApprovalTask();
            approvalTask.setInstanceId(instanceId);
            approvalTask.setFlowableTaskId(task.getId());
            approvalTask.setTaskName(task.getName());
            approvalTask.setTaskDefKey(task.getTaskDefinitionKey());

            // 获取任务候选人/办理人
            if (task.getAssignee() != null) {
                approvalTask.setAssigneeId(Long.parseLong(task.getAssignee()));
                // 根据 assigneeId 查询真实姓名
                try {
                    var userInfo = userFeignClient.getUserById(approvalTask.getAssigneeId());
                    if (userInfo != null) {
                        approvalTask.setAssigneeName(
                            userInfo.getRealName() != null ? userInfo.getRealName() : userInfo.getUsername()
                        );
                    }
                } catch (Exception e) {
                    log.warn("获取办理人信息失败: {}", e.getMessage());
                }
            } else if (task.getCandidates() != null && !task.getCandidates().isEmpty()) {
                // 候选人也记录
                approvalTask.setCurrentAssignees(
                    new com.fasterxml.jackson.databind.ObjectMapper()
                        .writeValueAsString(task.getCandidates())
                );
            }

            approvalTask.setStatus(0); // 待处理
            approvalTask.setPriority(task.getPriority());
            approvalTask.setDueDate(task.getDueDate());

            taskMapper.insert(approvalTask);

            // 更新实例的当前节点
            WorkflowInstance inst = instanceMapper.selectById(instanceId);
            if (inst != null && approvalTask.getAssigneeId() != null) {
                inst.setCurrentNodeName(approvalTask.getTaskName());
                inst.setCurrentAssignees("[\"" + approvalTask.getAssigneeName() + "\"]");
                instanceMapper.updateById(inst);
            }
        }
    }

    @Override
    public IPage<WorkflowInstance> queryMyInitiated(Long initiatorId, Integer status,
                                                    int pageNum, int pageSize) {
        Page<WorkflowInstance> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<WorkflowInstance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkflowInstance::getInitiatorId, initiatorId);
        if (status != null) wrapper.eq(WorkflowInstance::getStatus, status);
        wrapper.orderByDesc(WorkflowInstance::getCreatedTime);
        return instanceMapper.selectPage(page, wrapper);
    }

    @Override
    public IPage<WorkflowInstance> queryMyPending(Long assigneeId, int pageNum, int pageSize) {
        Page<WorkflowInstance> page = new Page<>(pageNum, pageSize);
        
        // 通过子查询：查询当前人有待办任务的实例
        LambdaQueryWrapper<ApprovalTask> taskWrapper = new LambdaQueryWrapper<>();
        taskWrapper.eq(ApprovalTask::getAssigneeId, assigneeId)
                   .eq(ApprovalTask::getStatus, 0);
        List<ApprovalTask> pendingTasks = taskMapper.selectList(taskWrapper);

        if (pendingTasks.isEmpty()) {
            return page; // 返回空页
        }

        List<Long> instanceIds = pendingTasks.stream()
                .map(ApprovalTask::getInstanceId).distinct().toList();

        LambdaQueryWrapper<WorkflowInstance> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(WorkflowInstance::getId, instanceIds)
               .eq(WorkflowInstance::getStatus, 0) // 只查运行中的
               .orderByDesc(WorkflowInstance::getCreatedTime);
        return instanceMapper.selectPage(page, wrapper);
    }

    @Override
    public IPage<WorkflowInstance> queryInstances(String keyword, Integer status,
                                                  int pageNum, int pageSize) {
        Page<WorkflowInstance> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<WorkflowInstance> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(WorkflowInstance::getBusinessTitle, keyword)
                    .or().like(WorkflowInstance::getBusinessKey, keyword));
        }
        if (status != null) wrapper.eq(WorkflowInstance::getStatus, status);
        wrapper.orderByDesc(WorkflowInstance::getCreatedTime);

        return instanceMapper.selectPage(page, wrapper);
    }

    @Override
    public WorkflowInstance getInstanceDetail(Long instanceId) {
        return instanceMapper.selectById(instanceId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long taskId, Long assigneeId, String comment) {
        ApprovalTask task = taskMapper.selectById(taskId);
        if (task == null || task.getDeleted() == 1) throw new BusinessException("任务不存在");
        if (task.getStatus() != 0) throw new BusinessException("该任务已被处理");

        // 更新任务状态
        task.setResult(1); // 同意
        task.setComment(comment);
        task.setStatus(1); // 已处理
        task.setCompletedTime(LocalDateTime.now());
        task.setAssigneeId(assigneeId);
        taskMapper.updateById(task);

        log.info("任务{}审批通过: {}", taskId, comment);
        
        // 推进流程（简化：标记完成或创建下一个节点）
        advanceProcess(task.getInstanceId(), taskId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long taskId, Long assigneeId, String comment) {
        ApprovalTask task = taskMapper.selectById(taskId);
        if (task == null || task.getStatus() != 0) throw new BusinessException("任务不存在或已处理");

        task.setResult(2); // 拒绝
        task.setComment(comment);
        task.setStatus(1);
        task.setCompletedTime(LocalDateTime.now());
        task.setAssigneeId(assigneeId);
        taskMapper.updateById(task);

        log.info("任务{}审批拒绝: {}", taskId, comment);

        // 流程结束（拒绝通常直接结束）
        completeProcess(task.getInstanceId(), 3); // 已驳回
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void returnTask(Long taskId, Long assigneeId, String targetNodeKey, String comment) {
        ApprovalTask task = taskMapper.selectById(taskId);
        if (task == null || task.getStatus() != 0) throw new BusinessException("任务不可驳回");

        task.setResult(3); // 驳回
        task.setComment(comment);
        task.setStatus(1);
        task.setCompletedTime(LocalDateTime.now());
        task.setAssigneeId(assigneeId);
        taskMapper.updateById(task);

        log.info("任务{}被驳回至: {}", taskId, targetNodeKey);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long instanceId, String operatorId, String reason) {
        WorkflowInstance instance = instanceMapper.selectById(instanceId);
        if (instance == null) throw new BusinessException("流程实例不存在");
        if (!String.valueOf(instance.getInitiatorId()).equals(operatorId)) {
            throw new BusinessException("只有发起人可以撤销流程");
        }

        // 将所有待处理的任务标记为撤回
        LambdaQueryWrapper<ApprovalTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApprovalTask::getInstanceId, instanceId)
               .eq(ApprovalTask::getStatus, 0);
        
        ApprovalTask cancelUpdate = new ApprovalTask();
        cancelUpdate.setStatus(4); // 已撤回
        cancelUpdate.setComment("发起人撤销: " + reason);
        taskMapper.cancelUpdate(wrapper, cancelUpdate);

        // 更新实例状态
        instance.setStatus(2); // 已撤销
        instanceMapper.updateById(instance);

        log.info("流程实例{}被撤销", instanceId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delegate(Long taskId, Long fromUserId, Long toUserId, String comment) {
        ApprovalTask task = taskMapper.selectById(taskId);
        if (task == null || task.getStatus() != 0) throw new BusinessException("任务不可转办");

        // 获取目标用户真实姓名
        String toUserName = "用户" + toUserId;
        String fromUserName = "用户" + fromUserId;
        try {
            var toUserInfo = userFeignClient.getUserById(toUserId);
            if (toUserInfo != null) {
                toUserName = toUserInfo.getRealName() != null ? toUserInfo.getRealName() : toUserInfo.getUsername();
            }
            var fromUserInfo = userFeignClient.getUserById(fromUserId);
            if (fromUserInfo != null) {
                fromUserName = fromUserInfo.getRealName() != null ? fromUserInfo.getRealName() : fromUserInfo.getUsername();
            }
        } catch (Exception e) {
            log.warn("获取用户信息失败: {}", e.getMessage());
        }

        task.setAssigneeId(toUserId);
        task.setAssigneeName(toUserName);

        // 在Flowable中转办
        try {
            taskService.delegateTask(task.getFlowableTaskId(), String.valueOf(toUserId));
        } catch (Exception e) {
            log.warn("Flowable任务转办失败，使用本地记录: {}", e.getMessage());
        }

        // 保留原处理人信息在comment中
        task.setComment((task.getComment() != null ? task.getComment() + "\n" : "") +
                        "转办自【" + fromUserName + "(" + fromUserId + ")】: " + (comment != null ? comment : ""));
        taskMapper.updateById(task);

        log.info("任务{}已从用户{}转办给用户{}", taskId, fromUserId, toUserId);
    }

    @Override
    public long getPendingCount(Long assigneeId) {
        return taskMapper.countPendingByAssignee(assigneeId);
    }

    /** 推进流程到下一个节点（简化） */
    private void advanceProcess(Long instanceId, Long completedTaskId) {
        // 检查是否还有待处理任务
        long pendingCount = taskMapper.countPendingByInstance(instanceId);
        
        if (pendingCount <= 0) {
            // 所有节点已完成，结束流程
            completeProcess(instanceId, 1); // 已完成
        } else {
            log.debug("流程{}还有{}个待处理节点，继续运行", instanceId, pendingCount);
        }
    }

    /** 结束流程实例 */
    private void completeProcess(Long instanceId, int status) {
        WorkflowInstance inst = instanceMapper.selectById(instanceId);
        if (inst != null) {
            inst.setStatus(status);
            inst.setCompletedTime(LocalDateTime.now());
            if (inst.getCreatedTime() != null) {
                inst.setDurationMs(java.time.Duration.between(
                        inst.getCreatedTime(), LocalDateTime.now()).toMillis());
            }
            instanceMapper.updateById(inst);
            log.info("流程实例{}已结束，状态={}", instanceId, getStatusText(status));
        }
    }

    private static String getStatusText(int s) {
        return switch (s) { case 0 -> "运行中"; case 1 -> "已完成"; case 2 -> "已撤销";
                              case 3 -> "已驳回"; default -> "未知"; };
    }
}
