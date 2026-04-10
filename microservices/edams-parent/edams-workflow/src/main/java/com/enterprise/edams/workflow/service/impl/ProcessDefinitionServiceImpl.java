package com.enterprise.edams.workflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enterprise.edams.workflow.dto.ProcessDefinitionCreateRequest;
import com.enterprise.edams.workflow.dto.ProcessDefinitionDTO;
import com.enterprise.edams.workflow.entity.ProcessDefinition;
import com.enterprise.edams.workflow.repository.ProcessDefinitionRepository;
import com.enterprise.edams.workflow.service.ProcessDefinitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 流程定义服务实现
 *
 * @author EDAMS Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessDefinitionServiceImpl extends ServiceImpl<ProcessDefinitionRepository, ProcessDefinition>
        implements ProcessDefinitionService {

    private final RepositoryService repositoryService;
    private final ProcessDefinitionRepository processDefinitionRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProcessDefinitionDTO createProcessDefinition(ProcessDefinitionCreateRequest request) {
        // 检查流程Key是否已存在
        ProcessDefinition existing = processDefinitionRepository.findLatestByProcessKey(request.getProcessKey());
        Integer nextVersion = existing != null ? existing.getVersion() + 1 : 1;

        // 如果是新版本，将旧版本标记为非最新
        if (existing != null) {
            existing.setIsLatest(false);
            updateById(existing);
        }

        ProcessDefinition definition = new ProcessDefinition();
        BeanUtils.copyProperties(request, definition);
        definition.setVersion(nextVersion);
        definition.setStatus(0); // 草稿状态
        definition.setIsLatest(true);

        save(definition);
        return convertToDTO(definition);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProcessDefinitionDTO updateProcessDefinition(String id, ProcessDefinitionCreateRequest request) {
        ProcessDefinition definition = getById(id);
        if (definition == null) {
            throw new RuntimeException("流程定义不存在");
        }

        if (definition.getStatus() == 1) {
            throw new RuntimeException("已发布的流程定义不能修改");
        }

        BeanUtils.copyProperties(request, definition);
        updateById(definition);
        return convertToDTO(definition);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deployProcessDefinition(String id) {
        ProcessDefinition definition = getById(id);
        if (definition == null) {
            throw new RuntimeException("流程定义不存在");
        }

        if (definition.getStatus() == 1) {
            throw new RuntimeException("流程定义已发布");
        }

        // 部署到Flowable
        Deployment deployment = repositoryService.createDeployment()
                .name(definition.getName())
                .addString(definition.getProcessKey() + ".bpmn20.xml", definition.getBpmnXml())
                .deploy();

        definition.setStatus(1); // 已发布
        updateById(definition);

        log.info("流程定义部署成功: {}, deploymentId: {}", definition.getName(), deployment.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deactivateProcessDefinition(String id) {
        ProcessDefinition definition = getById(id);
        if (definition == null) {
            throw new RuntimeException("流程定义不存在");
        }

        definition.setStatus(2); // 已停用
        updateById(definition);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProcessDefinition(String id) {
        ProcessDefinition definition = getById(id);
        if (definition == null) {
            throw new RuntimeException("流程定义不存在");
        }

        if (definition.getStatus() == 1) {
            throw new RuntimeException("已发布的流程定义不能删除");
        }

        removeById(id);
    }

    @Override
    public ProcessDefinitionDTO getProcessDefinition(String id) {
        ProcessDefinition definition = getById(id);
        if (definition == null) {
            throw new RuntimeException("流程定义不存在");
        }
        return convertToDTO(definition);
    }

    @Override
    public Page<ProcessDefinitionDTO> listProcessDefinitions(Page<ProcessDefinition> page, String keyword, String category, Integer status) {
        LambdaQueryWrapper<ProcessDefinition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProcessDefinition::getIsLatest, true);

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(ProcessDefinition::getName, keyword)
                    .or()
                    .like(ProcessDefinition::getProcessKey, keyword));
        }

        if (StringUtils.hasText(category)) {
            wrapper.eq(ProcessDefinition::getCategory, category);
        }

        if (status != null) {
            wrapper.eq(ProcessDefinition::getStatus, status);
        }

        wrapper.orderByDesc(ProcessDefinition::getCreatedTime);
        Page<ProcessDefinition> resultPage = page(page, wrapper);

        List<ProcessDefinitionDTO> records = resultPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        Page<ProcessDefinitionDTO> dtoPage = new Page<>();
        BeanUtils.copyProperties(resultPage, dtoPage);
        dtoPage.setRecords(records);
        return dtoPage;
    }

    @Override
    public List<ProcessDefinitionDTO> getProcessDefinitionVersions(String processKey) {
        List<ProcessDefinition> definitions = processDefinitionRepository.findByProcessKey(processKey);
        return definitions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public String getBpmnXml(String id) {
        ProcessDefinition definition = getById(id);
        if (definition == null) {
            throw new RuntimeException("流程定义不存在");
        }
        return definition.getBpmnXml();
    }

    @Override
    public String getProcessDiagram(String id) {
        ProcessDefinition definition = getById(id);
        if (definition == null) {
            throw new RuntimeException("流程定义不存在");
        }
        return definition.getDiagramSvg();
    }

    private ProcessDefinitionDTO convertToDTO(ProcessDefinition definition) {
        ProcessDefinitionDTO dto = new ProcessDefinitionDTO();
        BeanUtils.copyProperties(definition, dto);
        return dto;
    }
}
