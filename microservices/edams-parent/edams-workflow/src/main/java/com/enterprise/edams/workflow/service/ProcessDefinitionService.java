package com.enterprise.edams.workflow.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.enterprise.edams.workflow.dto.ProcessDefinitionCreateRequest;
import com.enterprise.edams.workflow.dto.ProcessDefinitionDTO;
import com.enterprise.edams.workflow.entity.ProcessDefinition;

import java.util.List;

/**
 * 流程定义服务接口
 * 
 * @author EDAMS Team
 */
public interface ProcessDefinitionService extends IService<ProcessDefinition> {

    /**
     * 创建流程定义
     */
    ProcessDefinitionDTO createProcessDefinition(ProcessDefinitionCreateRequest request);

    /**
     * 更新流程定义
     */
    ProcessDefinitionDTO updateProcessDefinition(String id, ProcessDefinitionCreateRequest request);

    /**
     * 发布流程定义
     */
    void deployProcessDefinition(String id);

    /**
     * 停用流程定义
     */
    void deactivateProcessDefinition(String id);

    /**
     * 删除流程定义
     */
    void deleteProcessDefinition(String id);

    /**
     * 获取流程定义详情
     */
    ProcessDefinitionDTO getProcessDefinition(String id);

    /**
     * 分页查询流程定义
     */
    Page<ProcessDefinitionDTO> listProcessDefinitions(Page<ProcessDefinition> page, String keyword, String category, Integer status);

    /**
     * 获取流程定义的所有版本
     */
    List<ProcessDefinitionDTO> getProcessDefinitionVersions(String processKey);

    /**
     * 获取BPMN XML
     */
    String getBpmnXml(String id);

    /**
     * 获取流程图片
     */
    String getProcessDiagram(String id);
}
