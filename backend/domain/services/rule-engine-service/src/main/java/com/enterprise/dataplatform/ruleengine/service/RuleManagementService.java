package com.enterprise.dataplatform.ruleengine.service;

import com.enterprise.dataplatform.ruleengine.domain.entity.RuleDefinition;
import com.enterprise.dataplatform.ruleengine.domain.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * 规则管理服务接口
 * 管理规则定义的CRUD操作
 */
public interface RuleManagementService {

    /**
     * 获取所有规则
     */
    Page<RuleDefinition> getRules(String category, String status, Pageable pageable);

    /**
     * 根据ID获取规则
     */
    RuleDefinition getRuleById(Long id);

    /**
     * 创建规则
     */
    RuleDefinition createRule(RuleDefinition rule);

    /**
     * 更新规则
     */
    RuleDefinition updateRule(Long id, RuleDefinition rule);

    /**
     * 删除规则
     */
    void deleteRule(Long id);

    /**
     * 启用/禁用规则
     */
    void toggleRuleStatus(Long id, String status);

    /**
     * 获取规则统计信息
     */
    Map<String, Object> getRuleStatistics();

    /**
     * 获取所有规则分类
     */
    List<String> getCategories();
}
