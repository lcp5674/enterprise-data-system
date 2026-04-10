package com.enterprise.edams.value.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enterprise.edams.value.dto.AssessmentSearchRequest;
import com.enterprise.edams.value.entity.ValueAssessment;
import com.enterprise.edams.value.mapper.ValueAssessmentMapper;
import com.enterprise.edams.value.service.ValueAssessmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据价值评估服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class ValueAssessmentServiceImpl extends ServiceImpl<ValueAssessmentMapper, ValueAssessment>
        implements ValueAssessmentService {
    
    @Override
    public ValueAssessment createAssessment(Long assetId) {
        log.info("创建价值评估任务: assetId={}", assetId);
        
        ValueAssessment assessment = ValueAssessment.builder()
                .assetId(assetId)
                .status(AssessmentStatus.PENDING)
                .assessmentDate(LocalDateTime.now())
                .overallScore(BigDecimal.ZERO)
                .businessScore(BigDecimal.ZERO)
                .technicalScore(BigDecimal.ZERO)
                .economicScore(BigDecimal.ZERO)
                .usageScore(BigDecimal.ZERO)
                .scarcityScore(BigDecimal.ZERO)
                .build();
        
        this.save(assessment);
        return assessment;
    }
    
    @Override
    public ValueAssessment executeAssessment(Long assessmentId) {
        log.info("执行价值评估: assessmentId={}", assessmentId);
        
        ValueAssessment assessment = this.getById(assessmentId);
        if (assessment == null) {
            throw new RuntimeException("评估记录不存在: " + assessmentId);
        }
        
        assessment.setStatus(AssessmentStatus.IN_PROGRESS);
        this.updateById(assessment);
        
        try {
            // 计算各项评分（实际项目中应该调用评估算法）
            BigDecimal businessScore = calculateBusinessScore(assessment.getAssetId());
            BigDecimal technicalScore = calculateTechnicalScore(assessment.getAssetId());
            BigDecimal economicScore = calculateEconomicScore(assessment.getAssetId());
            BigDecimal usageScore = calculateUsageScore(assessment.getAssetId());
            BigDecimal scarcityScore = calculateScarcityScore(assessment.getAssetId());
            
            // 计算综合评分
            BigDecimal overallScore = businessScore.multiply(BigDecimal.valueOf(0.3))
                    .add(technicalScore.multiply(BigDecimal.valueOf(0.2)))
                    .add(economicScore.multiply(BigDecimal.valueOf(0.25)))
                    .add(usageScore.multiply(BigDecimal.valueOf(0.15)))
                    .add(scarcityScore.multiply(BigDecimal.valueOf(0.1)))
                    .setScale(2, RoundingMode.HALF_UP);
            
            assessment.setBusinessScore(businessScore);
            assessment.setTechnicalScore(technicalScore);
            assessment.setEconomicScore(economicScore);
            assessment.setUsageScore(usageScore);
            assessment.setScarcityScore(scarcityScore);
            assessment.setOverallScore(overallScore);
            assessment.setStatus(AssessmentStatus.COMPLETED);
            assessment.setAssessmentMethod("MULTI_DIMENSION_WEIGHTED");
            
            this.updateById(assessment);
            log.info("价值评估完成: assessmentId={}, score={}", assessmentId, overallScore);
            
        } catch (Exception e) {
            log.error("价值评估失败: assessmentId={}", assessmentId, e);
            assessment.setStatus(AssessmentStatus.FAILED);
            this.updateById(assessment);
        }
        
        return assessment;
    }
    
    @Override
    public ValueAssessment getAssessmentById(Long id) {
        return this.getById(id);
    }
    
    @Override
    public Page<ValueAssessment> searchAssessments(AssessmentSearchRequest request) {
        Page<ValueAssessment> page = new Page<>(request.getPageNum(), request.getPageSize());
        
        LambdaQueryWrapper<ValueAssessment> wrapper = new LambdaQueryWrapper<>();
        
        if (request.getAssetId() != null) {
            wrapper.eq(ValueAssessment::getAssetId, request.getAssetId());
        }
        if (request.getMinScore() != null) {
            wrapper.ge(ValueAssessment::getOverallScore, request.getMinScore());
        }
        if (request.getMaxScore() != null) {
            wrapper.le(ValueAssessment::getOverallScore, request.getMaxScore());
        }
        if (request.getStatus() != null) {
            wrapper.eq(ValueAssessment::getStatus, request.getStatus());
        }
        
        wrapper.orderByDesc(ValueAssessment::getOverallScore);
        
        return this.page(page, wrapper);
    }
    
    @Override
    public ValueAssessment getLatestAssessment(Long assetId) {
        return this.getOne(new LambdaQueryWrapper<ValueAssessment>()
                .eq(ValueAssessment::getAssetId, assetId)
                .eq(ValueAssessment::getStatus, AssessmentStatus.COMPLETED)
                .orderByDesc(ValueAssessment::getAssessmentDate)
                .last("LIMIT 1"));
    }
    
    @Override
    public List<ValueAssessment> batchAssess(List<Long> assetIds) {
        log.info("批量评估资产: count={}", assetIds.size());
        List<ValueAssessment> results = new ArrayList<>();
        
        for (Long assetId : assetIds) {
            try {
                ValueAssessment assessment = createAssessment(assetId);
                assessment = executeAssessment(assessment.getId());
                results.add(assessment);
            } catch (Exception e) {
                log.error("批量评估失败: assetId={}", assetId, e);
            }
        }
        
        return results;
    }
    
    @Override
    public Map<String, Object> getValueDistribution() {
        Map<String, Object> distribution = new HashMap<>();
        
        // 按分数区间统计
        long range0to20 = this.count(new LambdaQueryWrapper<ValueAssessment>()
                .lt(ValueAssessment::getOverallScore, BigDecimal.valueOf(20)));
        long range20to40 = this.count(new LambdaQueryWrapper<ValueAssessment>()
                .ge(ValueAssessment::getOverallScore, BigDecimal.valueOf(20))
                .lt(ValueAssessment::getOverallScore, BigDecimal.valueOf(40)));
        long range40to60 = this.count(new LambdaQueryWrapper<ValueAssessment>()
                .ge(ValueAssessment::getOverallScore, BigDecimal.valueOf(40))
                .lt(ValueAssessment::getOverallScore, BigDecimal.valueOf(60)));
        long range60to80 = this.count(new LambdaQueryWrapper<ValueAssessment>()
                .ge(ValueAssessment::getOverallScore, BigDecimal.valueOf(60))
                .lt(ValueAssessment::getOverallScore, BigDecimal.valueOf(80)));
        long range80to100 = this.count(new LambdaQueryWrapper<ValueAssessment>()
                .ge(ValueAssessment::getOverallScore, BigDecimal.valueOf(80)));
        
        distribution.put("range0to20", range0to20);
        distribution.put("range20to40", range20to40);
        distribution.put("range40to60", range40to60);
        distribution.put("range60to80", range60to80);
        distribution.put("range80to100", range80to100);
        distribution.put("total", this.count());
        
        return distribution;
    }
    
    @Override
    public List<ValueAssessment> getTopValueAssets(int topN) {
        return this.list(new LambdaQueryWrapper<ValueAssessment>()
                .eq(ValueAssessment::getStatus, AssessmentStatus.COMPLETED)
                .orderByDesc(ValueAssessment::getOverallScore)
                .last("LIMIT " + topN));
    }
    
    // ============ 评分计算方法 ============
    
    private BigDecimal calculateBusinessScore(Long assetId) {
        // 模拟业务价值评分计算
        return BigDecimal.valueOf(60 + Math.random() * 30);
    }
    
    private BigDecimal calculateTechnicalScore(Long assetId) {
        // 模拟技术价值评分计算
        return BigDecimal.valueOf(55 + Math.random() * 35);
    }
    
    private BigDecimal calculateEconomicScore(Long assetId) {
        // 模拟经济价值评分计算
        return BigDecimal.valueOf(50 + Math.random() * 40);
    }
    
    private BigDecimal calculateUsageScore(Long assetId) {
        // 模拟使用价值评分计算
        return BigDecimal.valueOf(65 + Math.random() * 25);
    }
    
    private BigDecimal calculateScarcityScore(Long assetId) {
        // 模拟稀缺性评分计算
        return BigDecimal.valueOf(70 + Math.random() * 20);
    }
}
