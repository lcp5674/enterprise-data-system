package com.enterprise.edams.workflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enterprise.edams.workflow.dto.ProcessTaskDTO;
import com.enterprise.edams.workflow.dto.TaskApproveRequest;
import com.enterprise.edams.workflow.entity.ProcessHistory;
import com.enterprise.edams.workflow.entity.ProcessInstance;
import com.enterprise.edams.workflow.entity.ProcessTask;
import com.enterprise.edams.workflow.repository.ProcessInstanceRepository;
import com.enterprise.edams.workflow.repository.ProcessTaskRepository;
import com.enterprise.edams.workflow.service.ProcessHistoryService;
import com.enterprise.edams.workflow.service.ProcessTaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 流程任务服务实现
 *
 * @author EDAMS Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessTaskServiceImpl extends ServiceImpl<ProcessTaskRepository, ProcessTask>
        implements ProcessTaskService {

    private final TaskService taskService;
    private final ProcessTaskRepository processTaskRepository;
    private final ProcessInstanceRepository processInstanceRepository;
    private final ProcessHistoryService processHistoryService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveTask(TaskApproveRequest request, String userId, String userName) {
        ProcessTask task = getById(request.getTaskId());
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }

        // 验证任务处理人
        if (!userId.equals(task.getAssigneeId())) {
            throw new RuntimeException("无权处理此任务");
        }

        // 完成Flowable任务
        Map<String, Object> variables = new HashMap<>();
        if (request.getFormData() != null) {
            variables.putAll(request.getFormData());
        }
        variables.put("approved", true);
        variables.put("comment", request.getComment());

        taskService.complete(task.getFlowableTaskId(), variables);

        // 更新本地任务记录
        task.setStatus(1); // 已处理
        task.setResult(0); // 通过
        task.setComment(request.getComment());
        task.setHandleTime(LocalDateTime.now());

        try {
            if (request.getFormData() != null) {
                task.setFormData(objectMapper.writeValueAsString(request.getFormData()));
            }
        } catch (Exception e) {
            log.error("序列化表单数据失败", e);
        }

        updateById(task);

        // 记录历史
        recordTaskHistory(task, userId, userName, 2, 0, request.getComment());

        log.info("任务审批通过: {}, user: {}", request.getTaskId(), userName);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectTask(TaskApproveRequest request, String userId, String userName) {
        ProcessTask task = getById(request.getTaskId());
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }

        // 验证任务处理人
        if (!userId.equals(task.getAssigneeId())) {
            throw new RuntimeException("无权处理此任务");
        }

        // 设置拒绝变量
        Map<String, Object> variables = new HashMap<>();
        variables.put("approved", false);
        variables.put("comment", request.getComment());

        taskService.complete(task.getFlowableTaskId(), variables);

        // 更新本地任务记录
        task.setStatus(1); // 已处理
        task.setResult(1); // 拒绝
        task.setComment(request.getComment());
        task.setHandleTime(LocalDateTime.now());
        updateById(task);

        // 更新流程实例状态
        ProcessInstance instance = processInstanceRepository.selectById(task.getProcessInstanceId());
        if (instance != null) {
            instance.setStatus(1); // 已完成
            instance.setEndTime(LocalDateTime.now());
            instance.setResult(1); // 拒绝
            processInstanceRepository.updateById(instance);
        }

        // 记录历史
        recordTaskHistory(task, userId, userName, 2, 1, request.getComment());

        log.info("任务审批拒绝: {}, user: {}", request.getTaskId(), userName);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void backTask(TaskApproveRequest request, String userId, String userName) {
        ProcessTask task = getById(request.getTaskId());
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }

        if (!StringUtils.hasText(request.getBackNodeId())) {
            throw new RuntimeException("退回节点ID不能为空");
        }

        // 使用Flowable的退回功能
        taskService.setVariable(task.getFlowableTaskId(), "backNodeId", request.getBackNodeId());
        taskService.setVariable(task.getFlowableTaskId(), "backComment", request.getComment());

        // 更新本地任务记录
        task.setStatus(1); // 已处理
        task.setResult(2); // 退回
        task.setComment(request.getComment());
        task.setHandleTime(LocalDateTime.now());
        updateById(task);

        // 记录历史
        recordTaskHistory(task, userId, userName, 5, 2, request.getComment());

        log.info("任务退回: {}, backNodeId: {}, user: {}", request.getTaskId(), request.getBackNodeId(), userName);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transferTask(TaskApproveRequest request, String userId, String userName) {
        ProcessTask task = getById(request.getTaskId());
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }

        if (!StringUtils.hasText(request.getTransferToUserId())) {
            throw new RuntimeException("转办人ID不能为空");
        }

        // Flowable任务转办
        taskService.setAssignee(task.getFlowableTaskId(), request.getTransferToUserId());

        // 更新本地任务记录
        task.setStatus(2); // 已转办
        task.setResult(3); // 转办
        task.setComment(request.getComment());
        task.setHandleTime(LocalDateTime.now());
        updateById(task);

        // 记录历史
        recordTaskHistory(task, userId, userName, 3, 3, "转办给: " + request.getTransferToUserId() + ", " + request.getComment());

        log.info("任务转办: {}, transferTo: {}, user: {}", request.getTaskId(), request.getTransferToUserId(), userName);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delegateTask(TaskApproveRequest request, String userId, String userName) {
        ProcessTask task = getById(request.getTaskId());
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }

        if (!StringUtils.hasText(request.getDelegateToUserId())) {
            throw new RuntimeException("委托人ID不能为空");
        }

        // Flowable任务委托
        taskService.delegateTask(task.getFlowableTaskId(), request.getDelegateToUserId());

        // 更新本地任务记录
        task.setStatus(3); // 已委托
        task.setResult(4); // 委托
        task.setComment(request.getComment());
        task.setHandleTime(LocalDateTime.now());
        updateById(task);

        // 记录历史
        recordTaskHistory(task, userId, userName, 4, 4, "委托给: " + request.getDelegateToUserId() + ", " + request.getComment());

        log.info("任务委托: {}, delegateTo: {}, user: {}", request.getTaskId(), request.getDelegateToUserId(), userName);
    }

    @Override
    public ProcessTaskDTO getTask(String taskId) {
        ProcessTask task = getById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        return convertToDTO(task);
    }

    @Override
    public Page<ProcessTaskDTO> listTasks(Page<ProcessTask> page, String processInstanceId, Integer status) {
        LambdaQueryWrapper<ProcessTask> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(processInstanceId)) {
            wrapper.eq(ProcessTask::getProcessInstanceId, processInstanceId);
        }

        if (status != null) {
            wrapper.eq(ProcessTask::getStatus, status);
        }

        wrapper.orderByDesc(ProcessTask::getCreatedTime);
        Page<ProcessTask> resultPage = page(page, wrapper);

        List<ProcessTaskDTO> records = resultPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        Page<ProcessTaskDTO> dtoPage = new Page<>();
        BeanUtils.copyProperties(resultPage, dtoPage);
        dtoPage.setRecords(records);
        return dtoPage;
    }

    @Override
    public Page<ProcessTaskDTO> listTodoTasks(Page<ProcessTask> page, String userId) {
        Page<ProcessTask> resultPage = new Page<>();
        resultPage.setCurrent(page.getCurrent());
        resultPage.setSize(page.getSize());

        List<ProcessTask> tasks = processTaskRepository.findTodoByAssigneeId(userId);
        resultPage.setRecords(tasks);
        resultPage.setTotal(tasks.size());

        List<ProcessTaskDTO> records = tasks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        Page<ProcessTaskDTO> dtoPage = new Page<>();
        BeanUtils.copyProperties(resultPage, dtoPage);
        dtoPage.setRecords(records);
        return dtoPage;
    }

    @Override
    public Page<ProcessTaskDTO> listDoneTasks(Page<ProcessTask> page, String userId) {
        Page<ProcessTask> resultPage = new Page<>();
        resultPage.setCurrent(page.getCurrent());
        resultPage.setSize(page.getSize());

        List<ProcessTask> tasks = processTaskRepository.findDoneByAssigneeId(userId);
        resultPage.setRecords(tasks);
        resultPage.setTotal(tasks.size());

        List<ProcessTaskDTO> records = tasks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        Page<ProcessTaskDTO> dtoPage = new Page<>();
        BeanUtils.copyProperties(resultPage, dtoPage);
        dtoPage.setRecords(records);
        return dtoPage;
    }

    @Override
    public Page<ProcessTaskDTO> listCcTasks(Page<ProcessTask> page, String userId) {
        Page<ProcessTask> resultPage = new Page<>();
        resultPage.setCurrent(page.getCurrent());
        resultPage.setSize(page.getSize());

        List<ProcessTask> tasks = processTaskRepository.findCcByUserId(userId);
        resultPage.setRecords(tasks);
        resultPage.setTotal(tasks.size());

        List<ProcessTaskDTO> records = tasks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        Page<ProcessTaskDTO> dtoPage = new Page<>();
        BeanUtils.copyProperties(resultPage, dtoPage);
        dtoPage.setRecords(records);
        return dtoPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendTaskReminder(String taskId) {
        ProcessTask task = getById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }

        if (task.getStatus() != 0) {
            throw new RuntimeException("任务已处理，无需提醒");
        }

        // 更新提醒次数
        task.setReminderCount(task.getReminderCount() != null ? task.getReminderCount() + 1 : 1);
        task.setLastReminderTime(LocalDateTime.now());
        updateById(task);

        // TODO: 发送提醒消息（通过Kafka或消息服务）
        log.info("发送任务提醒: {}, assignee: {}", taskId, task.getAssigneeId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchApprove(List<String> taskIds, Integer result, String comment, String userId, String userName) {
        for (String taskId : taskIds) {
            TaskApproveRequest request = new TaskApproveRequest();
            request.setTaskId(taskId);
            request.setResult(result);
            request.setComment(comment);

            if (result == 0) {
                approveTask(request, userId, userName);
            } else if (result == 1) {
                rejectTask(request, userId, userName);
            }
        }
    }

    private void recordTaskHistory(ProcessTask task, String operatorId, String operatorName,
                                   Integer operationType, Integer operationResult, String comment) {
        ProcessHistory history = new ProcessHistory();
        history.setProcessInstanceId(task.getProcessInstanceId());
        history.setProcessTaskId(task.getId());
        history.setProcessDefinitionId(task.getProcessDefinitionId());
        history.setNodeId(task.getTaskNodeId());
        history.setNodeName(task.getTaskNodeName());
        history.setNodeType(2);
        history.setOperatorType(1);
        history.setOperatorId(operatorId);
        history.setOperatorName(operatorName);
        history.setOperationType(operationType);
        history.setOperationResult(operationResult);
        history.setComment(comment);
        processHistoryService.save(history);
    }

    private ProcessTaskDTO convertToDTO(ProcessTask task) {
        ProcessTaskDTO dto = new ProcessTaskDTO();
        BeanUtils.copyProperties(task, dto);

        // 获取流程实例信息
        ProcessInstance instance = processInstanceRepository.selectById(task.getProcessInstanceId());
        if (instance != null) {
            dto.setBusinessTitle(instance.getBusinessTitle());
            dto.setStarterName(instance.getStarterName());
            dto.setPriority(instance.getPriority());
        }

        return dto;
    }
}
