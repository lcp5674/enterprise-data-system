package com.enterprise.edams.workflow.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.enterprise.edams.workflow.entity.WorkflowDefinition;
import java.util.List;
import java.util.Map;

/**
 * 工作流定义服务接口
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
public interface WorkflowService {

    /** 创建流程定义（草稿） */
    WorkflowDefinition create(WorkflowDefinition definition, String operator);

    /** 发布流程定义（部署到Flowable） */
    WorkflowDefinition deploy(Long definitionId, String operator);

    /** 分页查询流程定义 */
    IPage<WorkflowDefinition> queryDefinitions(String keyword, Integer type,
                                               Integer status, int pageNum, int pageSize);

    /** 获取所有已发布的流程定义（下拉选择） */
    List<WorkflowDefinition> getPublishedDefinitions();

    /** 根据ID获取流程定义详情 */
    WorkflowDefinition getById(Long id);

    /** 更新流程定义 */
    void update(Long id, WorkflowDefinition definition, String operator);

    /** 禁用/启用流程定义 */
    void changeStatus(Long id, Integer status, String operator);

    /** 删除流程定义 */
    void delete(Long id, String operator);
}
