package com.enterprise.edams.workflow.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.workflow.dto.ProcessTaskDTO;
import com.enterprise.edams.workflow.dto.TaskApproveRequest;
import com.enterprise.edams.workflow.entity.ProcessInstance;
import com.enterprise.edams.workflow.entity.ProcessTask;
import com.enterprise.edams.workflow.repository.ProcessInstanceRepository;
import com.enterprise.edams.workflow.repository.ProcessTaskRepository;
import com.enterprise.edams.workflow.service.impl.ProcessTaskServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.flowable.engine.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 流程任务服务单元测试 - 重点测试Kafka消息发送
 *
 * @author EDAMS Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("流程任务服务单元测试")
class ProcessTaskServiceTest {

    @Mock
    private TaskService taskService;

    @Mock
    private ProcessTaskRepository processTaskRepository;

    @Mock
    private ProcessInstanceRepository processInstanceRepository;

    @Mock
    private ProcessHistoryService processHistoryService;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private ProcessTask testTask;

    @Mock
    private ProcessInstance testInstance;

    private ProcessTaskServiceImpl processTaskService;
    private ObjectMapper objectMapper;

    private static final String TASK_ID = "task-001";
    private static final String USER_ID = "user-001";
    private static final String USER_NAME = "测试用户";
    private static final String TASK_REMINDER_TOPIC = "edams-task-reminder";
    private static final String NOTIFICATION_TOPIC = "edams-notification";
    private static final String WORKFLOW_EVENT_TOPIC = "edams-workflow-event";

    @BeforeEach
    void setUp() {
        processTaskService = new ProcessTaskServiceImpl(
                taskService,
                processTaskRepository,
                processInstanceRepository,
                processHistoryService,
                objectMapper = new ObjectMapper(),
                kafkaTemplate
        );

        // 设置配置属性
        ReflectionTestUtils.setField(processTaskService, "taskReminderTopic", TASK_REMINDER_TOPIC);
        ReflectionTestUtils.setField(processTaskService, "notificationTopic", NOTIFICATION_TOPIC);
        ReflectionTestUtils.setField(processTaskService, "workflowEventTopic", WORKFLOW_EVENT_TOPIC);

        // 初始化测试任务
        testTask = mock(ProcessTask.class);
        when(testTask.getId()).thenReturn(TASK_ID);
        when(testTask.getTaskNodeName()).thenReturn("审批节点");
        when(testTask.getProcessInstanceId()).thenReturn("instance-001");
        when(testTask.getProcessDefinitionId()).thenReturn("process-001");
        when(testTask.getAssigneeId()).thenReturn(USER_ID);
        when(testTask.getPriority()).thenReturn(1);
        when(testTask.getStatus()).thenReturn(0); // 待处理
        when(testTask.getCreatedTime()).thenReturn(LocalDateTime.now());
        when(testTask.getReminderCount()).thenReturn(0);

        // 初始化测试实例
        testInstance = mock(ProcessInstance.class);
        when(testInstance.getBusinessTitle()).thenReturn("测试流程");
        when(testInstance.getStarterName()).thenReturn("发起人");
        when(processInstanceRepository.selectById("instance-001")).thenReturn(testInstance);
    }

    @Test
    @DisplayName("发送任务提醒 - Kafka消息发送成功")
    void testSendTaskReminder_Success() {
        // Given
        when(processTaskRepository.selectById(TASK_ID)).thenReturn(testTask);
        when(processTaskRepository.updateById(any(ProcessTask.class))).thenReturn(true);

        CompletableFuture<SendResult<String, Object>> mockFuture = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(eq(TASK_REMINDER_TOPIC), anyString(), anyMap())).thenReturn(mockFuture);
        when(kafkaTemplate.send(eq(NOTIFICATION_TOPIC), anyString(), anyMap())).thenReturn(mockFuture);

        // When
        processTaskService.sendTaskReminder(TASK_ID);

        // Then
        verify(kafkaTemplate, times(1)).send(eq(TASK_REMINDER_TOPIC), eq(TASK_ID), anyMap());
        verify(kafkaTemplate, times(1)).send(eq(NOTIFICATION_TOPIC), eq(USER_ID), anyMap());
        verify(processTaskRepository, times(1)).updateById(any(ProcessTask.class));
    }

    @Test
    @DisplayName("发送任务提醒 - 任务不存在")
    void testSendTaskReminder_TaskNotFound() {
        // Given
        when(processTaskRepository.selectById("non-existent")).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> processTaskService.sendTaskReminder("non-existent"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("任务不存在");

        verify(kafkaTemplate, never()).send(anyString(), anyString(), anyMap());
    }

    @Test
    @DisplayName("发送任务提醒 - 任务已处理，无需提醒")
    void testSendTaskReminder_AlreadyProcessed() {
        // Given
        when(testTask.getStatus()).thenReturn(1); // 已处理
        when(processTaskRepository.selectById(TASK_ID)).thenReturn(testTask);

        // When & Then
        assertThatThrownBy(() -> processTaskService.sendTaskReminder(TASK_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("任务已处理，无需提醒");
    }

    @Test
    @DisplayName("发送任务提醒 - 更新提醒次数")
    void testSendTaskReminder_IncrementReminderCount() {
        // Given
        when(processTaskRepository.selectById(TASK_ID)).thenReturn(testTask);
        when(processTaskRepository.updateById(any(ProcessTask.class))).thenReturn(true);

        CompletableFuture<SendResult<String, Object>> mockFuture = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(anyString(), anyString(), anyMap())).thenReturn(mockFuture);

        // When
        processTaskService.sendTaskReminder(TASK_ID);

        // Then - 验证提醒次数增加
        ArgumentCaptor<ProcessTask> taskCaptor = ArgumentCaptor.forClass(ProcessTask.class);
        verify(processTaskRepository).updateById(taskCaptor.capture());
        assertThat(taskCaptor.getValue().getReminderCount()).isEqualTo(1);
        assertThat(taskCaptor.getValue().getLastReminderTime()).isNotNull();
    }

    @Test
    @DisplayName("发送任务提醒 - Kafka消息包含正确的消息结构")
    void testSendTaskReminder_MessageContent() {
        // Given
        when(processTaskRepository.selectById(TASK_ID)).thenReturn(testTask);
        when(processTaskRepository.updateById(any(ProcessTask.class))).thenReturn(true);

        ArgumentCaptor<Map<String, Object>> messageCaptor = ArgumentCaptor.forClass(Map.class);
        CompletableFuture<SendResult<String, Object>> mockFuture = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(eq(TASK_REMINDER_TOPIC), anyString(), messageCaptor.capture())).thenReturn(mockFuture);
        when(kafkaTemplate.send(eq(NOTIFICATION_TOPIC), anyString(), anyMap())).thenReturn(mockFuture);

        // When
        processTaskService.sendTaskReminder(TASK_ID);

        // Then - 验证消息内容
        Map<String, Object> reminderMessage = messageCaptor.getValue();
        assertThat(reminderMessage).containsKey("messageId");
        assertThat(reminderMessage).containsEntry("messageType", "TASK_REMINDER");
        assertThat(reminderMessage).containsEntry("taskId", TASK_ID);
        assertThat(reminderMessage).containsEntry("taskName", "审批节点");
        assertThat(reminderMessage).containsEntry("processInstanceId", "instance-001");
        assertThat(reminderMessage).containsEntry("assigneeId", USER_ID);
        assertThat(reminderMessage).containsEntry("businessTitle", "测试流程");
    }

    @Test
    @DisplayName("发送通知消息 - Kafka消息发送成功")
    void testSendNotificationMessage_Success() {
        // Given
        when(processTaskRepository.selectById(TASK_ID)).thenReturn(testTask);
        when(processTaskRepository.updateById(any(ProcessTask.class))).thenReturn(true);

        CompletableFuture<SendResult<String, Object>> mockFuture = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(eq(TASK_REMINDER_TOPIC), anyString(), anyMap())).thenReturn(mockFuture);
        when(kafkaTemplate.send(eq(NOTIFICATION_TOPIC), anyString(), anyMap())).thenReturn(mockFuture);

        // When
        processTaskService.sendTaskReminder(TASK_ID);

        // Then
        verify(kafkaTemplate, times(1)).send(eq(NOTIFICATION_TOPIC), eq(USER_ID), anyMap());
    }

    @Test
    @DisplayName("发送通知消息 - 包含正确的渠道信息")
    void testSendNotificationMessage_Channels() {
        // Given
        when(processTaskRepository.selectById(TASK_ID)).thenReturn(testTask);
        when(processTaskRepository.updateById(any(ProcessTask.class))).thenReturn(true);

        ArgumentCaptor<Map<String, Object>> messageCaptor = ArgumentCaptor.forClass(Map.class);
        CompletableFuture<SendResult<String, Object>> mockFuture = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(eq(TASK_REMINDER_TOPIC), anyString(), anyMap())).thenReturn(mockFuture);
        when(kafkaTemplate.send(eq(NOTIFICATION_TOPIC), anyString(), messageCaptor.capture())).thenReturn(mockFuture);

        // When
        processTaskService.sendTaskReminder(TASK_ID);

        // Then - 验证通知渠道
        Map<String, Object> notificationMessage = messageCaptor.getValue();
        assertThat(notificationMessage).containsEntry("messageType", "TASK_NOTIFICATION");
        assertThat(notificationMessage).containsEntry("userId", USER_ID);
        assertThat(notificationMessage).containsEntry("title", "待办任务提醒");
        assertThat(notificationMessage.get("channels")).isEqualTo(List.of("IN_APP", "SMS", "EMAIL"));
    }

    @Test
    @DisplayName("发送流程事件消息 - 成功")
    void testSendWorkflowEventMessage_Success() {
        // Given
        when(processTaskRepository.selectById(TASK_ID)).thenReturn(testTask);

        CompletableFuture<SendResult<String, Object>> mockFuture = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(eq(WORKFLOW_EVENT_TOPIC), anyString(), anyMap())).thenReturn(mockFuture);

        // When
        processTaskService.sendWorkflowEventMessage("TASK_COMPLETED", testTask, USER_ID, USER_NAME);

        // Then
        ArgumentCaptor<Map<String, Object>> messageCaptor = ArgumentCaptor.forClass(Map.class);
        verify(kafkaTemplate).send(eq(WORKFLOW_EVENT_TOPIC), eq("instance-001"), messageCaptor.capture());

        Map<String, Object> eventMessage = messageCaptor.getValue();
        assertThat(eventMessage).containsEntry("eventType", "TASK_COMPLETED");
        assertThat(eventMessage).containsEntry("taskId", TASK_ID);
        assertThat(eventMessage).containsEntry("operatorId", USER_ID);
    }

    @Test
    @DisplayName("批量发送任务提醒 - 成功")
    void testBatchSendTaskReminder_Success() {
        // Given
        List<String> taskIds = List.of("task-001", "task-002", "task-003");

        when(processTaskRepository.selectById("task-001")).thenReturn(testTask);
        when(processTaskRepository.selectById("task-002")).thenReturn(testTask);
        when(processTaskRepository.selectById("task-003")).thenReturn(testTask);
        when(processTaskRepository.updateById(any(ProcessTask.class))).thenReturn(true);

        CompletableFuture<SendResult<String, Object>> mockFuture = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(anyString(), anyString(), anyMap())).thenReturn(mockFuture);

        // When
        processTaskService.batchSendTaskReminder(taskIds);

        // Then
        verify(kafkaTemplate, times(3)).send(eq(TASK_REMINDER_TOPIC), anyString(), anyMap());
    }

    @Test
    @DisplayName("批量发送任务提醒 - 空列表")
    void testBatchSendTaskReminder_EmptyList() {
        // Given
        List<String> taskIds = new ArrayList<>();

        // When
        processTaskService.batchSendTaskReminder(taskIds);

        // Then
        verify(kafkaTemplate, never()).send(anyString(), anyString(), anyMap());
    }

    @Test
    @DisplayName("批量发送任务提醒 - 跳过已处理任务")
    void testBatchSendTaskReminder_SkipProcessed() {
        // Given
        ProcessTask processedTask = mock(ProcessTask.class);
        when(processedTask.getId()).thenReturn("task-002");
        when(processedTask.getStatus()).thenReturn(1); // 已处理

        List<String> taskIds = List.of("task-001", "task-002");

        when(processTaskRepository.selectById("task-001")).thenReturn(testTask);
        when(processTaskRepository.selectById("task-002")).thenReturn(processedTask);
        when(processTaskRepository.updateById(any(ProcessTask.class))).thenReturn(true);

        CompletableFuture<SendResult<String, Object>> mockFuture = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(anyString(), anyString(), anyMap())).thenReturn(mockFuture);

        // When
        processTaskService.batchSendTaskReminder(taskIds);

        // Then - 只发送一条消息（跳过已处理的任务）
        verify(kafkaTemplate, times(1)).send(eq(TASK_REMINDER_TOPIC), eq("task-001"), anyMap());
    }

    @Test
    @DisplayName("审批任务 - 成功")
    void testApproveTask_Success() {
        // Given
        when(processTaskRepository.selectById(TASK_ID)).thenReturn(testTask);
        when(processTaskRepository.updateById(any(ProcessTask.class))).thenReturn(true);
        when(processHistoryService.save(any())).thenReturn(true);

        TaskApproveRequest request = new TaskApproveRequest();
        request.setTaskId(TASK_ID);
        request.setComment("同意");

        Map<String, Object> formData = new HashMap<>();
        formData.put("approved", true);
        request.setFormData(formData);

        // When
        processTaskService.approveTask(request, USER_ID, USER_NAME);

        // Then
        verify(taskService, times(1)).complete(eq("task-001"), anyMap());
        verify(processTaskRepository, times(1)).updateById(any(ProcessTask.class));
        verify(processHistoryService, times(1)).save(any());
    }

    @Test
    @DisplayName("审批任务 - 任务不存在")
    void testApproveTask_NotFound() {
        // Given
        when(processTaskRepository.selectById("non-existent")).thenReturn(null);

        TaskApproveRequest request = new TaskApproveRequest();
        request.setTaskId("non-existent");
        request.setComment("同意");

        // When & Then
        assertThatThrownBy(() -> processTaskService.approveTask(request, USER_ID, USER_NAME))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("任务不存在");
    }

    @Test
    @DisplayName("审批任务 - 无权处理")
    void testApproveTask_Unauthorized() {
        // Given
        when(processTaskRepository.selectById(TASK_ID)).thenReturn(testTask);

        TaskApproveRequest request = new TaskApproveRequest();
        request.setTaskId(TASK_ID);
        request.setComment("同意");

        // When & Then
        assertThatThrownBy(() -> processTaskService.approveTask(request, "other-user", USER_NAME))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("无权处理此任务");
    }

    @Test
    @DisplayName("拒绝任务 - 成功")
    void testRejectTask_Success() {
        // Given
        when(processTaskRepository.selectById(TASK_ID)).thenReturn(testTask);
        when(processTaskRepository.updateById(any(ProcessTask.class))).thenReturn(true);
        when(processInstanceRepository.selectById("instance-001")).thenReturn(testInstance);
        when(processHistoryService.save(any())).thenReturn(true);

        TaskApproveRequest request = new TaskApproveRequest();
        request.setTaskId(TASK_ID);
        request.setComment("不符合条件");

        // When
        processTaskService.rejectTask(request, USER_ID, USER_NAME);

        // Then
        verify(taskService, times(1)).complete(eq("task-001"), anyMap());
        verify(processTaskRepository, times(2)).updateById(any(ProcessTask.class)); // 任务 + 实例
        verify(processHistoryService, times(1)).save(any());
    }

    @Test
    @DisplayName("退回任务 - 成功")
    void testBackTask_Success() {
        // Given
        when(processTaskRepository.selectById(TASK_ID)).thenReturn(testTask);
        when(processTaskRepository.updateById(any(ProcessTask.class))).thenReturn(true);
        when(processHistoryService.save(any())).thenReturn(true);

        TaskApproveRequest request = new TaskApproveRequest();
        request.setTaskId(TASK_ID);
        request.setBackNodeId("node-001");
        request.setComment("需要补充材料");

        // When
        processTaskService.backTask(request, USER_ID, USER_NAME);

        // Then
        verify(taskService, times(1)).setVariable(eq("task-001"), eq("backNodeId"), eq("node-001"));
        verify(processTaskRepository, times(1)).updateById(any(ProcessTask.class));
    }

    @Test
    @DisplayName("退回任务 - 退回节点ID为空")
    void testBackTask_MissingBackNodeId() {
        // Given
        when(processTaskRepository.selectById(TASK_ID)).thenReturn(testTask);

        TaskApproveRequest request = new TaskApproveRequest();
        request.setTaskId(TASK_ID);
        request.setBackNodeId(null);
        request.setComment("需要补充材料");

        // When & Then
        assertThatThrownBy(() -> processTaskService.backTask(request, USER_ID, USER_NAME))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("退回节点ID不能为空");
    }

    @Test
    @DisplayName("转办任务 - 成功")
    void testTransferTask_Success() {
        // Given
        when(processTaskRepository.selectById(TASK_ID)).thenReturn(testTask);
        when(processTaskRepository.updateById(any(ProcessTask.class))).thenReturn(true);
        when(processHistoryService.save(any())).thenReturn(true);

        TaskApproveRequest request = new TaskApproveRequest();
        request.setTaskId(TASK_ID);
        request.setTransferToUserId("new-user-001");
        request.setComment("请帮忙处理");

        // When
        processTaskService.transferTask(request, USER_ID, USER_NAME);

        // Then
        verify(taskService, times(1)).setAssignee(eq("task-001"), eq("new-user-001"));
        verify(processTaskRepository, times(1)).updateById(any(ProcessTask.class));
    }

    @Test
    @DisplayName("委托任务 - 成功")
    void testDelegateTask_Success() {
        // Given
        when(processTaskRepository.selectById(TASK_ID)).thenReturn(testTask);
        when(processTaskRepository.updateById(any(ProcessTask.class))).thenReturn(true);
        when(processHistoryService.save(any())).thenReturn(true);

        TaskApproveRequest request = new TaskApproveRequest();
        request.setTaskId(TASK_ID);
        request.setDelegateToUserId("delegate-user-001");
        request.setComment("委托处理");

        // When
        processTaskService.delegateTask(request, USER_ID, USER_NAME);

        // Then
        verify(taskService, times(1)).delegateTask(eq("task-001"), eq("delegate-user-001"));
        verify(processTaskRepository, times(1)).updateById(any(ProcessTask.class));
    }

    @Test
    @DisplayName("获取任务详情 - 成功")
    void testGetTask_Success() {
        // Given
        when(processTaskRepository.selectById(TASK_ID)).thenReturn(testTask);

        // When
        ProcessTaskDTO result = processTaskService.getTask(TASK_ID);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(TASK_ID);
    }

    @Test
    @DisplayName("获取任务详情 - 不存在")
    void testGetTask_NotFound() {
        // Given
        when(processTaskRepository.selectById("non-existent")).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> processTaskService.getTask("non-existent"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("任务不存在");
    }
}
