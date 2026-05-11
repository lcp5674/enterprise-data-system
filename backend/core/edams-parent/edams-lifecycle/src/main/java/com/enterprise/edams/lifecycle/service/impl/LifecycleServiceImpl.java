package com.enterprise.edams.lifecycle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.common.exception.BusinessException;
import com.enterprise.edams.common.exception.ComplianceException;
import com.enterprise.edams.lifecycle.entity.ComplianceViolation;
import com.enterprise.edams.lifecycle.entity.DataAsset;
import com.enterprise.edams.lifecycle.entity.DataLifecycle;
import com.enterprise.edams.lifecycle.entity.LifecycleStage;
import com.enterprise.edams.lifecycle.entity.LifecycleTemplate;
import com.enterprise.edams.lifecycle.repository.DataAssetService;
import com.enterprise.edams.lifecycle.repository.LifecycleMapper;
import com.enterprise.edams.lifecycle.repository.LifecycleStageMapper;
import com.enterprise.edams.lifecycle.repository.LifecycleTemplateMapper;
import com.enterprise.edams.lifecycle.service.LifecycleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LifecycleServiceImpl implements LifecycleService {

    private final LifecycleMapper lifecycleMapper;
    private final LifecycleStageMapper lifecycleStageMapper;
    private final LifecycleTemplateMapper lifecycleTemplateMapper;
    private final DataAssetService dataAssetService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public DataLifecycle createLifecycle(DataLifecycle lifecycle) {
        if (lifecycleMapper.findByDataAssetId(lifecycle.getDataAssetId()) != null) {
            throw new BusinessException("数据资产已存在生命周期记录");
        }

        LifecycleStage stage = lifecycleStageMapper.findByStageCode(lifecycle.getCurrentStage());
        if (stage == null) {
            throw new BusinessException("生命周期阶段不存在");
        }

        DataAsset asset = dataAssetService.getAssetById(lifecycle.getDataAssetId());
        if (asset == null) {
            throw new BusinessException("数据资产不存在");
        }

        performComplianceChecks(asset);

        lifecycle.setStageChangedAt(LocalDateTime.now());
        lifecycle.setStatus(1);
        lifecycleMapper.insert(lifecycle);
        
        eventPublisher.publishEvent(new LifecycleCreatedEvent(this, lifecycle));
        
        log.info("创建生命周期记录成功: id={}, assetId={}, stage={}", 
                lifecycle.getId(), lifecycle.getDataAssetId(), lifecycle.getCurrentStage());
        
        return lifecycle;
    }

    private void performComplianceChecks(DataAsset asset) {
        List<ComplianceViolation> violations = new ArrayList<>();

        if (asset.getSecurityLevel() == null) {
            violations.add(ComplianceViolation.builder()
                    .type("MISSING_CLASSIFICATION")
                    .severity("HIGH")
                    .message("数据资产缺少安全等级分类")
                    .suggestion("请先为数据资产设置安全等级")
                    .build());
        }

        if (asset.getOwnerId() == null || asset.getOwnerId().isEmpty()) {
            violations.add(ComplianceViolation.builder()
                    .type("MISSING_OWNER")
                    .severity("HIGH")
                    .message("数据资产缺少负责人")
                    .suggestion("请为数据资产指定负责人")
                    .build());
        }

        if (Boolean.TRUE.equals(asset.getContainsPersonalData())) {
            if (!hasMaskingRules(asset.getId())) {
                violations.add(ComplianceViolation.builder()
                        .type("MISSING_MASKING_RULE")
                        .severity("HIGH")
                        .message("包含个人信息的数据资产缺少脱敏规则")
                        .suggestion("请为数据资产配置脱敏规则")
                        .build());
            }
        }

        if (!violations.isEmpty()) {
            boolean hasHighSeverity = violations.stream()
                    .anyMatch(v -> "HIGH".equals(v.getSeverity()));
            
            if (hasHighSeverity) {
                throw new ComplianceException("合规性检查未通过，无法创建生命周期", violations);
            } else {
                log.warn("生命周期创建检测到合规性警告: assetId={}, violations={}",
                        asset.getId(), violations);
            }
        }
    }

    private boolean hasMaskingRules(Long assetId) {
        return true;
    }

    @Override
    @Transactional
    public DataLifecycle updateLifecycle(Long id, DataLifecycle lifecycle) {
        DataLifecycle existing = lifecycleMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("生命周期记录不存在");
        }

        lifecycle.setPreviousStage(existing.getCurrentStage());
        lifecycle.setStageChangedAt(LocalDateTime.now());

        lifecycleMapper.updateById(lifecycle);
        return lifecycleMapper.selectById(id);
    }

    @Override
    public DataLifecycle getLifecycle(Long id) {
        return lifecycleMapper.selectById(id);
    }

    @Override
    public DataLifecycle getLifecycleByDataAssetId(Long dataAssetId) {
        return lifecycleMapper.findByDataAssetId(dataAssetId);
    }

    @Override
    @Transactional
    public void deleteLifecycle(Long id) {
        lifecycleMapper.deleteById(id);
    }

    @Override
    public IPage<DataLifecycle> listLifecycles(Integer pageNum, Integer pageSize) {
        Page<DataLifecycle> page = new Page<>(pageNum, pageSize);
        QueryWrapper<DataLifecycle> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("stage_changed_at");
        return lifecycleMapper.selectPage(page, wrapper);
    }

    @Override
    public IPage<DataLifecycle> searchLifecycles(String keyword, Integer pageNum, Integer pageSize) {
        Page<DataLifecycle> page = new Page<>(pageNum, pageSize);
        return lifecycleMapper.searchByKeyword(page, keyword);
    }

    @Override
    public IPage<DataLifecycle> listLifecyclesByStage(String stage, Integer pageNum, Integer pageSize) {
        Page<DataLifecycle> page = new Page<>(pageNum, pageSize);
        return lifecycleMapper.findByStage(page, stage);
    }

    @Override
    @Transactional
    public DataLifecycle transitionToNextStage(Long lifecycleId) {
        log.info("开始生命周期阶段转换: lifecycleId={}", lifecycleId);

        DataLifecycle lifecycle = lifecycleMapper.selectById(lifecycleId);
        if (lifecycle == null) {
            throw new BusinessException("生命周期记录不存在: " + lifecycleId);
        }

        String currentStage = lifecycle.getCurrentStage();
        if (currentStage == null || currentStage.isEmpty()) {
            throw new BusinessException("当前阶段为空，请先初始化生命周期");
        }

        LifecycleStage currentStageInfo = lifecycleStageMapper.findByStageCode(currentStage);
        if (currentStageInfo == null) {
            throw new BusinessException("当前阶段配置不存在: " + currentStage);
        }

        String nextStageCode = currentStageInfo.getNextStageCode();
        if (nextStageCode == null || nextStageCode.isEmpty()) {
            throw new BusinessException("当前阶段不允许转换: " + currentStage + "（未配置下一阶段）");
        }

        if (!currentStageInfo.isAllowTransition()) {
            throw new BusinessException("当前阶段不允许转换: " + currentStage);
        }

        List<String> prerequisites = currentStageInfo.getPrerequisites();
        if (prerequisites != null && !prerequisites.isEmpty()) {
            validateTransitionPrerequisites(lifecycle, prerequisites);
        }

        LifecycleStage nextStageInfo = lifecycleStageMapper.findByStageCode(nextStageCode);
        if (nextStageInfo == null) {
            throw new BusinessException("下一阶段配置不存在: " + nextStageCode);
        }

        if (Boolean.TRUE.equals(nextStageInfo.getRequireApproval())) {
            lifecycle.setStatus(2);
            lifecycle.setApprovalStatus("PENDING");
            lifecycle.setApprovalStartedAt(LocalDateTime.now());
            lifecycle.setApprovalRequester(lifecycle.getOwner());

            log.info("阶段转换进入审核流程: lifecycleId={}, from={}, to={}, requiresApproval=true",
                    lifecycleId, currentStage, nextStageCode);
        } else {
            lifecycle.setPreviousStage(currentStage);
            lifecycle.setCurrentStage(nextStageCode);
            lifecycle.setStageChangedAt(LocalDateTime.now());
            lifecycle.setStatus(1);

            log.info("阶段转换完成（无需审核）: lifecycleId={}, from={}, to={}",
                    lifecycleId, currentStage, nextStageCode);
        }

        lifecycleMapper.updateById(lifecycle);

        recordStageTransition(lifecycleId, currentStage, nextStageCode, 
                !Boolean.TRUE.equals(nextStageInfo.getRequireApproval()));

        publishStageTransitionEvent(lifecycle, currentStage, nextStageCode);

        return lifecycle;
    }

    private void validateTransitionPrerequisites(DataLifecycle lifecycle, List<String> prerequisites) {
        List<String> unmetPrerequisites = new ArrayList<>();

        for (String prerequisite : prerequisites) {
            boolean met = checkPrerequisite(lifecycle, prerequisite);
            if (!met) {
                unmetPrerequisites.add(prerequisite);
            }
        }

        if (!unmetPrerequisites.isEmpty()) {
            throw new BusinessException(String.format(
                    "阶段转换前置条件未满足: lifecycleId=%d, unmetConditions=%s",
                    lifecycle.getId(), unmetPrerequisites));
        }
    }

    private boolean checkPrerequisite(DataLifecycle lifecycle, String prerequisite) {
        if (prerequisite == null) {
            return true;
        }

        switch (prerequisite.toUpperCase()) {
            case "ASSET_PUBLISHED":
                return isAssetPublished(lifecycle.getDataAssetId());
            case "QUALITY_PASSED":
                return isQualityPassed(lifecycle.getDataAssetId());
            case "METADATA_COMPLETE":
                return isMetadataComplete(lifecycle.getDataAssetId());
            case "OWNER_CONFIRMED":
                return lifecycle.getOwner() != null && !lifecycle.getOwner().isEmpty();
            case "TAG_ASSOCIATED":
                return hasTags(lifecycle.getDataAssetId());
            case "CLASSIFICATION_SET":
                return hasClassification(lifecycle.getDataAssetId());
            default:
                log.warn("未知的前置条件类型: {}", prerequisite);
                return true;
        }
    }

    private boolean isAssetPublished(Long assetId) {
        return true;
    }

    private boolean isQualityPassed(Long assetId) {
        return true;
    }

    private boolean isMetadataComplete(Long assetId) {
        return true;
    }

    private boolean hasTags(Long assetId) {
        return true;
    }

    private boolean hasClassification(Long assetId) {
        DataAsset asset = dataAssetService.getAssetById(assetId);
        return asset != null && asset.getSecurityLevel() != null;
    }

    @Override
    @Transactional
    public DataLifecycle approveStageTransition(Long lifecycleId, String approver, String approvalComment) {
        DataLifecycle lifecycle = lifecycleMapper.selectById(lifecycleId);
        if (lifecycle == null) {
            throw new BusinessException("生命周期记录不存在: " + lifecycleId);
        }

        if (!"PENDING".equals(lifecycle.getApprovalStatus())) {
            throw new BusinessException("当前状态不是待审核，无法审批");
        }

        lifecycle.setApprovalStatus("APPROVED");
        lifecycle.setApprovalApprover(approver);
        lifecycle.setApprovalComment(approvalComment);
        lifecycle.setApprovalCompletedAt(LocalDateTime.now());

        LifecycleStage currentStageInfo = lifecycleStageMapper.findByStageCode(lifecycle.getCurrentStage());
        String nextStageCode = currentStageInfo.getNextStageCode();

        lifecycle.setPreviousStage(lifecycle.getCurrentStage());
        lifecycle.setCurrentStage(nextStageCode);
        lifecycle.setStageChangedAt(LocalDateTime.now());
        lifecycle.setStatus(1);

        lifecycleMapper.updateById(lifecycle);

        recordStageTransition(lifecycleId, lifecycle.getPreviousStage(), nextStageCode, true, approver, approvalComment);

        log.info("阶段转换审核通过: lifecycleId={}, approver={}, from={}, to={}",
                lifecycleId, approver, lifecycle.getPreviousStage(), nextStageCode);

        return lifecycle;
    }

    private void recordStageTransition(Long lifecycleId, String fromStage, String toStage, boolean autoApproved) {
        recordStageTransition(lifecycleId, fromStage, toStage, autoApproved, null, null);
    }

    private void recordStageTransition(Long lifecycleId, String fromStage, String toStage, 
                                       boolean autoApproved, String approver, String comment) {
        log.debug("记录阶段转换历史: lifecycleId={}, from={}, to={}, approved={}", 
                lifecycleId, fromStage, toStage, autoApproved);
    }

    private void publishStageTransitionEvent(DataLifecycle lifecycle, String fromStage, String toStage) {
        log.debug("发布阶段转换事件: lifecycleId={}, from={}, to={}", lifecycle.getId(), fromStage, toStage);
    }

    @Override
    public List<LifecycleStage> getAllStages() {
        QueryWrapper<LifecycleStage> wrapper = new QueryWrapper<>();
        wrapper.eq("enabled", 1);
        wrapper.orderByAsc("sort_order");
        return lifecycleStageMapper.selectList(wrapper);
    }

    @lombok.Data
    @lombok.Builder
    public static class LifecycleCreatedEvent {
        private Object source;
        private DataLifecycle lifecycle;
    }
}
