package com.enterprise.dataplatform.ruleengine.service.impl;

import com.enterprise.dataplatform.ruleengine.domain.entity.RuleDefinition;
import com.enterprise.dataplatform.ruleengine.repository.RuleDefinitionRepository;
import com.enterprise.dataplatform.ruleengine.service.RuleManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 规则管理服务实现
 * 处理规则定义的CRUD操作
 */
@Service
public class RuleManagementServiceImpl implements RuleManagementService {

    @Autowired
    private RuleDefinitionRepository ruleDefinitionRepository;

    @Override
    public Page<RuleDefinition> getRules(String category, String status, Pageable pageable) {
        if (category != null && status != null) {
            return ruleDefinitionRepository.findByCategoryAndStatus(category, status, pageable);
        } else if (category != null) {
            return ruleDefinitionRepository.findByCategory(category, pageable);
        } else if (status != null) {
            return ruleDefinitionRepository.findByStatus(status, pageable);
        }
        return ruleDefinitionRepository.findAll(pageable);
    }

    @Override
    public RuleDefinition getRuleById(Long id) {
        return ruleDefinitionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("规则不存在: id=" + id));
    }

    @Override
    @Transactional
    public RuleDefinition createRule(RuleDefinition rule) {
        if (ruleDefinitionRepository.existsByRuleCode(rule.getRuleCode())) {
            throw new RuntimeException("规则编码已存在: " + rule.getRuleCode());
        }
        rule.setStatus(rule.getStatus() != null ? rule.getStatus() : "DRAFT");
        rule.setVersion(rule.getVersion() != null ? rule.getVersion() : "v1.0");
        rule.setPriority(rule.getPriority() != null ? rule.getPriority() : 0);
        rule.setDeleted(0);
        return ruleDefinitionRepository.save(rule);
    }

    @Override
    @Transactional
    public RuleDefinition updateRule(Long id, RuleDefinition updated) {
        RuleDefinition existing = getRuleById(id);

        if (updated.getRuleName() != null) existing.setRuleName(updated.getRuleName());
        if (updated.getRuleCode() != null && !updated.getRuleCode().equals(existing.getRuleCode())) {
            if (ruleDefinitionRepository.existsByRuleCode(updated.getRuleCode())) {
                throw new RuntimeException("规则编码已存在: " + updated.getRuleCode());
            }
            existing.setRuleCode(updated.getRuleCode());
        }
        if (updated.getCategory() != null) existing.setCategory(updated.getCategory());
        if (updated.getDescription() != null) existing.setDescription(updated.getDescription());
        if (updated.getRuleContent() != null) existing.setRuleContent(updated.getRuleContent());
        if (updated.getRuleFilePath() != null) existing.setRuleFilePath(updated.getRuleFilePath());
        if (updated.getPriority() != null) existing.setPriority(updated.getPriority());
        if (updated.getTriggerCondition() != null) existing.setTriggerCondition(updated.getTriggerCondition());
        if (updated.getParameters() != null) existing.setParameters(updated.getParameters());
        if (updated.getUpdatedBy() != null) existing.setUpdatedBy(updated.getUpdatedBy());

        // 版本号自增
        existing.setVersion(incrementVersion(existing.getVersion()));

        return ruleDefinitionRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteRule(Long id) {
        RuleDefinition rule = getRuleById(id);
        rule.setDeleted(1);
        ruleDefinitionRepository.save(rule);
    }

    @Override
    @Transactional
    public void toggleRuleStatus(Long id, String status) {
        RuleDefinition rule = getRuleById(id);
        if (!"ACTIVE".equals(status) && !"INACTIVE".equals(status) && !"DRAFT".equals(status)) {
            throw new RuntimeException("无效的状态值: " + status);
        }
        rule.setStatus(status);
        ruleDefinitionRepository.save(rule);
    }

    @Override
    public Map<String, Object> getRuleStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRules", ruleDefinitionRepository.count());
        stats.put("activeRules", ruleDefinitionRepository.countByStatus("ACTIVE"));
        stats.put("inactiveRules", ruleDefinitionRepository.countByStatus("INACTIVE"));
        stats.put("draftRules", ruleDefinitionRepository.countByStatus("DRAFT"));

        Map<String, Long> byCategory = new LinkedHashMap<>();
        for (String category : Arrays.asList("QUALITY", "COMPLIANCE", "VALUE", "LIFECYCLE", "GOVERNANCE")) {
            byCategory.put(category, ruleDefinitionRepository.countByCategory(category));
        }
        stats.put("byCategory", byCategory);

        return stats;
    }

    @Override
    public List<String> getCategories() {
        return Arrays.asList("QUALITY", "COMPLIANCE", "VALUE", "LIFECYCLE", "GOVERNANCE");
    }

    /**
     * 版本号自增
     */
    private String incrementVersion(String currentVersion) {
        if (currentVersion == null || !currentVersion.startsWith("v")) {
            return "v1.1";
        }
        try {
            int versionNum = Integer.parseInt(currentVersion.substring(1));
            return "v" + (versionNum + 1);
        } catch (NumberFormatException e) {
            return "v1.1";
        }
    }
}
