package com.enterprise.edams.workflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enterprise.edams.workflow.dto.ProcessInstanceDTO;
import com.enterprise.edams.workflow.dto.ProcessInstanceStartRequest;
import com.enterprise.edams.workflow.entity.ProcessDefinition;
import com.enterprise.edams.workflow.entity.ProcessHistory;
import com.enterprise.edams.workflow.entity.ProcessInstance;
import com.enterprise.edams.workflow.repository.ProcessDefinitionRepository;
import com.enterprise.edams.workflow.repository.ProcessInstanceRepository;
import com.enterprise.edams.workflow.service.ProcessHistoryService;
import com.enterprise.edams.workflow.service.ProcessInstanceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstanceBuilder;
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
 * 流程实例服务实现
 *
 * @author EDAMS Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessInstanceServiceImpl extends ServiceImpl<ProcessInstanceRepository, ProcessInstance>
        implements ProcessInstanceService {

    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final ProcessDefinitionRepository processDefinitionRepository;
    private final ProcessHistoryService processHistoryService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProcessInstanceDTO startProcessInstance(ProcessInstanceStartRequest request, String starterId, String starterName) {
        // 获取最新版本的流程定义
        ProcessDefinition definition = processDefinitionRepository.findLatestByProcessKey(request.getProcessDefinitionKey());
        if (definition == null) {
            throw new RuntimeException("流程定义不存在");
        }

        if (definition.getStatus() != 1) {
            throw new RuntimeException("流程定义未发布");
        }

        // 准备流程变量
        Map<String, Object> variables = new HashMap<>();
        if (request.getVariables() != null) {
            variables.putAll(request.getVariables());
        }
        variables.put("starterId", starterId);
        variables.put("starterName", starterName);
        variables.put("businessType", request.getBusinessType());
        variables.put("businessId", request.getBusinessId());

        // 启动Flowable流程实例
        ProcessInstanceBuilder builder = runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey(request.getProcessDefinitionKey())
                .variables(variables);

        if (StringUtils.hasText(request.getBusinessId())) {
            builder.businessKey(request.getBusinessId());
        }

        org.flowable.engine.runtime.ProcessInstance flowableInstance = builder.start();

        // 创建本地流程实例记录
        ProcessInstance instance = new ProcessInstance();
        instance.setFlowableInstanceId(flowableInstance.getId());
        instance.setProcessDefinitionId(definition.getId());
        instance.setBusinessType(request.getBusinessType());
        instance.setBusinessId(request.getBusinessId());
        instance.setBusinessTitle(request.getBusinessTitle());
        instance.setStarterId(starterId);
        instance.setStarterName(starterName);
        instance.setStatus(0); // 运行中
        instance.setPriority(request.getPriority() != null ? request.getPriority() : 2);
        instance.setStartTime(LocalDateTime.now());

        try {
            if (request.getFormData() != null) {
                instance.setFormData(objectMapper.writeValueAsString(request.getFormData()));
            }
            instance.setVariables(objectMapper.writeValueAsString(variables));
        } catch (Exception e) {
            log.error("序列化表单数据失败", e);
        }

        save(instance);

        // 记录历史
        ProcessHistory history = new ProcessHistory();
        history.setProcessInstanceId(instance.getId());
        history.setProcessDefinitionId(definition.getId());
        history.setNodeId("start");
        history.setNodeName("流程发起");
        history.setNodeType(1);
        history.setOperatorType(1);
        history.setOperatorId(starterId);
        history.setOperatorName(starterName);
        history.setOperationType(1);
        history.setOperationResult(0);
        history.setComment("流程发起");
        processHistoryService.save(history);

        log.info("流程实例启动成功: {}, instanceId: {}", definition.getName(), instance.getId());
        return convertToDTO(instance);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void terminateProcessInstance(String instanceId, String reason, String operatorId) {
        ProcessInstance instance = getById(instanceId);
        if (instance == null) {
            throw new RuntimeException("流程实例不存在");
        }

        // 终止Flowable流程实例
        runtimeService.deleteProcessInstance(instance.getFlowableInstanceId(), reason);

        // 更新本地记录
        instance.setStatus(2); // 已终止
        instance.setEndTime(LocalDateTime.now());
        instance.setResult(5); // 终止
        updateById(instance);

        // 记录历史
        ProcessHistory history = new ProcessHistory();
        history.setProcessInstanceId(instanceId);
        history.setProcessDefinitionId(instance.getProcessDefinitionId());
        history.setNodeId("terminate");
        history.setNodeName("流程终止");
        history.setNodeType(4);
        history.setOperatorType(1);
        history.setOperatorId(operatorId);
        history.setOperationType(6);
        history.setOperationResult(5);
        history.setComment(reason);
        processHistoryService.save(history);

        log.info("流程实例终止成功: {}", instanceId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revokeProcessInstance(String instanceId, String reason, String operatorId) {
        ProcessInstance instance = getById(instanceId);
        if (instance == null) {
            throw new RuntimeException("流程实例不存在");
        }

        // 撤回流程实例
        runtimeService.deleteProcessInstance(instance.getFlowableInstanceId(), reason);

        // 更新本地记录
        instance.setStatus(2); // 已终止
        instance.setEndTime(LocalDateTime.now());
        instance.setResult(2); // 撤回
        updateById(instance);

        // 记录历史
        ProcessHistory history = new ProcessHistory();
        history.setProcessInstanceId(instanceId);
        history.setProcessDefinitionId(instance.getProcessDefinitionId());
        history.setNodeId("revoke");
        history.setNodeName("流程撤回");
        history.setNodeType(4);
        history.setOperatorType(1);
        history.setOperatorId(operatorId);
        history.setOperationType(7);
        history.setOperationResult(2);
        history.setComment(reason);
        processHistoryService.save(history);

        log.info("流程实例撤回成功: {}", instanceId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void suspendProcessInstance(String instanceId, String operatorId) {
        ProcessInstance instance = getById(instanceId);
        if (instance == null) {
            throw new RuntimeException("流程实例不存在");
        }

        runtimeService.suspendProcessInstanceById(instance.getFlowableInstanceId());
        instance.setStatus(3); // 已挂起
        updateById(instance);

        log.info("流程实例挂起成功: {}", instanceId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void activateProcessInstance(String instanceId, String operatorId) {
        ProcessInstance instance = getById(instanceId);
        if (instance == null) {
            throw new RuntimeException("流程实例不存在");
        }

        runtimeService.activateProcessInstanceById(instance.getFlowableInstanceId());
        instance.setStatus(0); // 运行中
        updateById(instance);

        log.info("流程实例激活成功: {}", instanceId);
    }

    @Override
    public ProcessInstanceDTO getProcessInstance(String instanceId) {
        ProcessInstance instance = getById(instanceId);
        if (instance == null) {
            throw new RuntimeException("流程实例不存在");
        }
        return convertToDTO(instance);
    }

    @Override
    public Page<ProcessInstanceDTO> listProcessInstances(Page<ProcessInstance> page, String keyword, Integer status, String businessType) {
        LambdaQueryWrapper<ProcessInstance> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(ProcessInstance::getBusinessTitle, keyword)
                    .or()
                    .like(ProcessInstance::getStarterName, keyword));
        }

        if (status != null) {
            wrapper.eq(ProcessInstance::getStatus, status);
        }

        if (StringUtils.hasText(businessType)) {
            wrapper.eq(ProcessInstance::getBusinessType, businessType);
        }

        wrapper.orderByDesc(ProcessInstance::getCreatedTime);
        Page<ProcessInstance> resultPage = page(page, wrapper);

        List<ProcessInstanceDTO> records = resultPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        Page<ProcessInstanceDTO> dtoPage = new Page<>();
        BeanUtils.copyProperties(resultPage, dtoPage);
        dtoPage.setRecords(records);
        return dtoPage;
    }

    @Override
    public Page<ProcessInstanceDTO> listMyStartedInstances(Page<ProcessInstance> page, String starterId, Integer status) {
        LambdaQueryWrapper<ProcessInstance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProcessInstance::getStarterId, starterId);

        if (status != null) {
            wrapper.eq(ProcessInstance::getStatus, status);
        }

        wrapper.orderByDesc(ProcessInstance::getCreatedTime);
        Page<ProcessInstance> resultPage = page(page, wrapper);

        List<ProcessInstanceDTO> records = resultPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        Page<ProcessInstanceDTO> dtoPage = new Page<>();
        BeanUtils.copyProperties(resultPage, dtoPage);
        dtoPage.setRecords(records);
        return dtoPage;
    }

    @Override
    public List<ProcessInstanceDTO> listPendingApproval(String userId) {
        // 查询待我审批的流程实例
        return baseMapper.findRunningInstances().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProcessInstanceDTO> listApprovedByMe(String userId) {
        // 查询我已审批的流程实例
        return list().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private ProcessInstanceDTO convertToDTO(ProcessInstance instance) {
        ProcessInstanceDTO dto = new ProcessInstanceDTO();
        BeanUtils.copyProperties(instance, dto);

        // 获取流程定义名称
        ProcessDefinition definition = processDefinitionRepository.selectById(instance.getProcessDefinitionId());
        if (definition != null) {
            dto.setProcessDefinitionName(definition.getName());
        }

        return dto;
    }
}
