package com.enterprise.edams.workflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.common.exception.BusinessException;
import com.enterprise.edams.workflow.entity.WorkflowDefinition;
import com.enterprise.edams.workflow.repository.WorkflowDefinitionMapper;
import com.enterprise.edams.workflow.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.DeploymentBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 工作流定义服务实现
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowServiceImpl implements WorkflowService {

    private final WorkflowDefinitionMapper definitionMapper;
    private final RepositoryService repositoryService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkflowDefinition create(WorkflowDefinition definition, String operator) {
        if (definitionMapper.findByCode(definition.getCode()) != null) {
            throw new BusinessException("流程编码已存在: " + definition.getCode());
        }

        definition.setStatus(0); // 草稿状态
        definition.setVersion(1);
        definition.setCreatedBy(operator);

        // 设置默认值
        if (definition.getAllowCancel() == null) definition.setAllowCancel(1);
        if (definition.getAllowAddSignee() == null) definition.setAllowAddSignee(0);
        if (definition.getAllowDelegate() == null) definition.setAllowDelegate(1);
        if (definition.getTimeoutHours() == null) definition.setTimeoutHours(72);

        definitionMapper.insert(definition);
        log.info("工作流定义创建成功: {} ({})", definition.getName(), definition.getId());
        return definition;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkflowDefinition deploy(Long definitionId, String operator) {
        WorkflowDefinition def = definitionMapper.selectById(definitionId);
        if (def == null || def.getDeleted() == 1) throw new BusinessException("流程定义不存在");

        // 获取BPMN内容
        String bpmnContent = def.getBpmnContent();
        if (bpmnContent == null || bpmnContent.isEmpty()) {
            throw new BusinessException("流程BPMN内容为空，请先设计流程");
        }

        try {
            // 使用Flowable RepositoryService部署BPMN XML
            DeploymentBuilder deploymentBuilder = repositoryService.createDeployment()
                    .name(def.getName())
                    .key(def.getCode())
                    .category(def.getDescription());

            // 根据内容类型判断：如果是XML字符串，直接添加；如果是BPMN文件路径，则读取文件
            if (bpmnContent.trim().startsWith("<?xml") || bpmnContent.trim().startsWith("<")) {
                deploymentBuilder.addString(def.getCode() + ".bpmn20.xml", bpmnContent);
            } else {
                // 尝试作为classpath资源加载
                deploymentBuilder.addClasspathResource(bpmnContent);
            }

            Deployment deployment = deploymentBuilder.deploy();

            // 更新流程定义信息
            def.setProcessDefKey(deployment.getKey());
            def.setProcessDefId(deployment.getId());
            def.setStatus(1); // 已发布
            def.setPublishedTime(java.time.LocalDateTime.now());
            def.setUpdatedBy(operator);
            definitionMapper.updateById(def);

            log.info("流程定义已发布到Flowable引擎: {} -> deploymentId={}", definitionId, deployment.getId());
            return def;

        } catch (Exception e) {
            log.error("Flowable部署失败: {}", e.getMessage(), e);
            throw new BusinessException("Flowable部署失败: " + e.getMessage());
        }
    }

    @Override
    public IPage<WorkflowDefinition> queryDefinitions(String keyword, Integer type,
                                                       Integer status, int pageNum, int pageSize) {
        Page<WorkflowDefinition> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<WorkflowDefinition> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(WorkflowDefinition::getName, keyword)
                    .or().like(WorkflowDefinition::getCode, keyword));
        }
        if (type != null) wrapper.eq(WorkflowDefinition::getType, type);
        if (status != null) wrapper.eq(WorkflowDefinition::getStatus, status);

        wrapper.orderByDesc(WorkflowDefinition::getCreatedTime);
        return definitionMapper.selectPage(page, wrapper);
    }

    @Override
    public List<WorkflowDefinition> getPublishedDefinitions() {
        LambdaQueryWrapper<WorkflowDefinition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkflowDefinition::getStatus, 1)
               .orderByAsc(WorkflowDefinition::getSortOrder);
        return definitionMapper.selectList(wrapper);
    }

    @Override
    public WorkflowDefinition getById(Long id) {
        WorkflowDefinition def = definitionMapper.selectById(id);
        if (def == null || def.getDeleted() == 1) throw new BusinessException("流程定义不存在");
        return def;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, WorkflowDefinition definition, String operator) {
        WorkflowDefinition existing = definitionMapper.selectById(id);
        if (existing == null || existing.getDeleted() == 1) throw new BusinessException("流程定义不存在");

        existing.setName(definition.getName());
        existing.setCode(definition.getCode());
        existing.setType(definition.getType());
        existing.setDescription(definition.getDescription());
        existing.setBpmnContent(definition.getBpmnContent());
        existing.setFormConfig(definition.getFormConfig());
        existing.setAllowCancel(definition.getAllowCancel());
        existing.setAllowAddSignee(definition.getAllowAddSignee());
        existing.setAllowDelegate(definition.getAllowDelegate());
        existing.setTimeoutHours(definition.getTimeoutHours());

        existing.setUpdatedBy(operator);
        definitionMapper.updateById(existing);
        log.info("流程定义更新成功: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(Long id, Integer status, String operator) {
        WorkflowDefinition def = definitionMapper.selectById(id);
        if (def == null) throw new BusinessException("流程定义不存在");
        def.setStatus(status);
        def.setUpdatedBy(operator);
        definitionMapper.updateById(def);
        log.info("流程定义{}状态变更为: {}", id, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id, String operator) {
        WorkflowDefinition def = definitionMapper.selectById(id);
        if (def == null || def.getDeleted() == 1) throw new BusinessException("流程定义不存在");
        if (def.getStatus() != 0) {
            throw new BusinessException("只能删除草稿状态的流程定义，当前状态为" + 
                    getStatusText(def.getStatus()));
        }
        def.setDeleted(1);
        def.setUpdatedBy(operator);
        definitionMapper.updateById(def);
        log.info("流程定义已删除: {}", id);
    }

    private static String getStatusText(Integer status) {
        if (status == null) return "未知";
        return switch (status) { case 0 -> "草稿"; case 1 -> "已发布"; case 2 -> "已禁用"; default -> "未知"; };
    }
}
