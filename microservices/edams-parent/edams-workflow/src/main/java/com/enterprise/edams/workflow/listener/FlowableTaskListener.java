package com.enterprise.edams.workflow.listener;

import com.enterprise.edams.workflow.entity.ProcessTask;
import com.enterprise.edams.workflow.repository.ProcessInstanceRepository;
import com.enterprise.edams.workflow.repository.ProcessTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Flowable任务监听器
 *
 * @author EDAMS Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FlowableTaskListener implements TaskListener {

    private final ProcessTaskRepository processTaskRepository;
    private final ProcessInstanceRepository processInstanceRepository;

    @Override
    public void notify(DelegateTask delegateTask) {
        String eventName = delegateTask.getEventName();
        log.info("Flowable任务事件: {}, taskId: {}", eventName, delegateTask.getId());

        switch (eventName) {
            case EVENTNAME_CREATE:
                handleTaskCreate(delegateTask);
                break;
            case EVENTNAME_COMPLETE:
                handleTaskComplete(delegateTask);
                break;
            case EVENTNAME_ASSIGNMENT:
                handleTaskAssignment(delegateTask);
                break;
            default:
                break;
        }
    }

    private void handleTaskCreate(DelegateTask delegateTask) {
        // 创建本地任务记录
        ProcessTask task = new ProcessTask();
        task.setFlowableTaskId(delegateTask.getId());
        task.setProcessInstanceId((String) delegateTask.getVariable("processInstanceId"));
        task.setProcessDefinitionId((String) delegateTask.getVariable("processDefinitionId"));
        task.setTaskNodeId(delegateTask.getTaskDefinitionKey());
        task.setTaskNodeName(delegateTask.getName());
        task.setTaskType(1); // 审批任务
        task.setStatus(0); // 待处理
        task.setCreatedTime(LocalDateTime.now());

        if (delegateTask.getAssignee() != null) {
            task.setAssigneeId(delegateTask.getAssignee());
        }

        processTaskRepository.insert(task);

        // 更新流程实例当前节点
        var instanceOpt = processInstanceRepository.findByFlowableInstanceId(delegateTask.getProcessInstanceId());
        if (instanceOpt != null) {
            instanceOpt.setCurrentNodeId(delegateTask.getTaskDefinitionKey());
            instanceOpt.setCurrentNodeName(delegateTask.getName());
            instanceOpt.setCurrentAssigneeId(delegateTask.getAssignee());
            processInstanceRepository.updateById(instanceOpt);
        }

        log.info("任务创建成功: {}", delegateTask.getId());
    }

    private void handleTaskComplete(DelegateTask delegateTask) {
        // 更新本地任务记录
        ProcessTask task = processTaskRepository.findByFlowableTaskId(delegateTask.getId());
        if (task != null) {
            task.setStatus(1); // 已处理
            task.setHandleTime(LocalDateTime.now());
            processTaskRepository.updateById(task);
        }

        log.info("任务完成: {}", delegateTask.getId());
    }

    private void handleTaskAssignment(DelegateTask delegateTask) {
        // 更新任务处理人
        ProcessTask task = processTaskRepository.findByFlowableTaskId(delegateTask.getId());
        if (task != null) {
            task.setAssigneeId(delegateTask.getAssignee());
            processTaskRepository.updateById(task);
        }

        log.info("任务分配: {}, assignee: {}", delegateTask.getId(), delegateTask.getAssignee());
    }
}
