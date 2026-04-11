package com.enterprise.edams.workflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.common.exception.BusinessException;
import com.enterprise.edams.workflow.entity.ApprovalTask;
import com.enterprise.edams.workflow.entity.WorkflowDefinition;
import com.enterprise.edams.workflow.entity.WorkflowInstance;
import com.enterprise.edams.workflow.repository.ApprovalTaskMapper;
import com.enterprise.edams.workflow.repository.WorkflowDefinitionMapper;
import com.enterprise.edams.workflow.repository.WorkflowInstanceMapper;
import com.enterprise.edams.workflow.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkflowInstance startProcess(Long definitionId, Long initiatorId,
                                          String businessTitle, String formData) {
        // 1. 验证流程定义
        WorkflowDefinition def = definitionMapper.selectById(definitionId);
        if (def == null || def.getStatus() != 1) {
            throw new BusinessException("流程定义不存在或未发布");
        }

        // 2. 创建流程实例
        WorkflowInstance instance = new WorkflowInstance();
        instance.setDefinitionId(definitionId);
        instance.setProcessDefKey(def.getCode());
        instance.setBusinessTitle(businessTitle != null ? businessTitle : def.getName());
        instance.setBusinessType(def.getType());
        instance.setBusinessKey(UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        instance.setInitiatorId(initiatorId);
        // TODO: 从用户服务获取发起人姓名和部门
        instance.setInitiatorName("用户" + initiatorId);
        instance.setInitiatorDept("未知部门");
        instance.setCurrentNodeName("开始");
        instance.setStatus(0); // 运行中
        instance.setPriority(1);
        instance.setFormData(formData);

        // 模拟Flowable启动流程
        instance.setProcessInstanceId("PROC-" + System.currentTimeMillis());

        instanceMapper.insert(instance);

        // 3. 创建第一个审批任务（模拟）
        createFirstApprovalTask(instance.getId(), initiatorId);

        log.info("流程实例已启动: {} - {} ({})", def.getCode(), businessTitle, instance.getId());
        return instance;
    }

    /** 创建第一个审批任务 */
    private void createFirstApprovalTask(Long instanceId, Long initiatorId) {
        // 这里简化处理：实际应根据BPMN定义创建对应的审批节点任务
        ApprovalTask task = new ApprovalTask();
        task.setInstanceId(instanceId);
        task.setFlowableTaskId("TASK-" + UUID.randomUUID().toString().substring(0, 8));
        task.setTaskName("部门经理审批"); // 第一个节点名称
        task.setTaskDefKey("dept_manager_approve");
        // TODO: 根据业务逻辑确定实际的处理人
        task.setAssigneeId(null);  // 待分配
        task.setAssigneeName("");   // 待确定
        task.setStatus(0);          // 待处理

        taskMapper.insert(task);

        // 更新实例的当前节点信息
        WorkflowInstance inst = instanceMapper.selectById(instanceId);
        if (inst != null) {
            inst.setCurrentNodeName(task.getTaskName());
            inst.setCurrentAssignees("[\"待分配\"]");
            instanceMapper.updateById(inst);
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

        task.setAssigneeId(toUserId);
        task.setAssigneeName("用户" + toUserId); // TODO: 从用户服务查询真实姓名
        // 保留原处理人信息在comment中
        task.setComment((task.getComment() != null ? task.getComment() + "\n" : "") +
                        "转办自用户" + fromUserId + ": " + (comment != null ? comment : ""));
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
