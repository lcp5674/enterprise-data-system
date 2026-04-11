package com.edams.value.service.impl;

import com.edams.common.model.PageResult;
import com.edams.common.utils.JsonUtils;
import com.edams.common.utils.PageUtils;
import com.edams.value.entity.DataValue;
import com.edams.value.entity.ValueMetric;
import com.edams.value.repository.DataValueRepository;
import com.edams.value.repository.ValueMetricRepository;
import com.edams.value.service.ValueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValueServiceImpl implements ValueService {
    
    private final DataValueRepository valueRepository;
    private final ValueMetricRepository metricRepository;
    
    @Override
    @Transactional
    public DataValue assessDataValue(Long assetId, Long assessorId) {
        log.info("Assessing data value for asset: {}", assetId);
        
        // 获取价值度量标准
        List<ValueMetric> metrics = metricRepository.findByStatus("ACTIVE");
        
        // 计算总价值
        double totalScore = 0;
        Map<String, Double> factorScores = new HashMap<>();
        
        for (ValueMetric metric : metrics) {
            double metricScore = calculateMetricScore(metric, assetId);
            factorScores.put(metric.getMetricName(), metricScore);
            totalScore += metricScore * metric.getWeight();
        }
        
        DataValue value = new DataValue();
        value.setAssetId(assetId);
        value.setAssetType("DATASET");
        value.setAssetName("Asset-" + assetId);
        value.setValueScore(totalScore);
        value.setValueCategory("BUSINESS");
        value.setAssessmentMethod("AI");
        value.setAssessmentFactors(JsonUtils.toJson(factorScores));
        value.setValueDetails(JsonUtils.toJson(Map.of(
            "total_score", totalScore,
            "factor_scores", factorScores,
            "assessment_time", LocalDateTime.now().toString()
        )));
        value.setAssessmentDate(LocalDateTime.now());
        value.setAssessorId(assessorId);
        value.setValidityPeriod(30);
        
        return valueRepository.save(value);
    }
    
    @Override
    public DataValue getValueById(Long id) {
        return valueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Data value not found: " + id));
    }
    
    @Override
    public PageResult<DataValue> listValues(Map<String, Object> params) {
        Specification<DataValidation> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (params.get("assetId") != null) {
                predicates.add(cb.equal(root.get("assetId"), params.get("assetId")));
            }
            if (params.get("assetType") != null) {
                predicates.add(cb.equal(root.get("assetType"), params.get("assetType")));
            }
            if (params.get("valueCategory") != null) {
                predicates.add(cb.equal(root.get("valueCategory"), params.get("valueCategory")));
            }
            if (params.get("assessorId") != null) {
                predicates.add(cb.equal(root.get("assessorId"), params.get("assessorId")));
            }
            if (params.get("status") != null) {
                predicates.add(cb.equal(root.get("status"), params.get("status")));
            }
            if (params.get("minScore") != null && params.get("maxScore") != null) {
                predicates.add(cb.between(root.get("valueScore"), 
                    (Double) params.get("minScore"), 
                    (Double) params.get("maxScore")));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        Pageable pageable = PageUtils.getPageable(params);
        Page<DataValue> page = valueRepository.findAll(spec, pageable);
        return PageResult.of(page);
    }
    
    @Override
    @Transactional
    public DataValue updateValue(Long id, DataValue value) {
        log.info("Updating data value: {}", id);
        DataValue existing = getValueById(id);
        existing.setValueScore(value.getValueScore());
        existing.setValueCategory(value.getValueCategory());
        existing.setAssessmentFactors(value.getAssessmentFactors());
        existing.setValueDetails(value.getValueDetails());
        existing.setValueTrend(value.getValueTrend());
        existing.setValidityPeriod(value.getValidityPeriod());
        return valueRepository.save(existing);
    }
    
    @Override
    @Transactional
    public void deleteValue(Long id) {
        log.info("Deleting data value: {}", id);
        valueRepository.deleteById(id);
    }
    
    @Override
    @Transactional
    public ValueMetric createMetric(ValueMetric metric) {
        log.info("Creating value metric: {}", metric.getMetricName());
        return metricRepository.save(metric);
    }
    
    @Override
    @Transactional
    public ValueMetric updateMetric(Long id, ValueMetric metric) {
        log.info("Updating value metric: {}", id);
        ValueMetric existing = getMetricById(id);
        existing.setMetricName(metric.getMetricName());
        existing.setMetricDescription(metric.getMetricDescription());
        existing.setWeight(metric.getWeight());
        existing.setCalculationFormula(metric.getCalculationFormula());
        existing.setReferenceData(metric.getReferenceData());
        return metricRepository.save(existing);
    }
    
    @Override
    @Transactional
    public void deleteMetric(Long id) {
        log.info("Deleting value metric: {}", id);
        metricRepository.deleteById(id);
    }
    
    @Override
    public ValueMetric getMetricById(Long id) {
        return metricRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Value metric not found: " + id));
    }
    
    @Override
    public PageResult<ValueMetric> listMetrics(Map<String, Object> params) {
        Specification<ValueMetric> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (params.get("metricType") != null) {
                predicates.add(cb.equal(root.get("metricType"), params.get("metricType")));
            }
            if (params.get("status") != null) {
                predicates.add(cb.equal(root.get("status"), params.get("status")));
            }
            if (params.get("metricName") != null) {
                predicates.add(cb.like(root.get("metricName"), "%" + params.get("metricName") + "%"));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        Pageable pageable = PageUtils.getPageable(params);
        Page<ValueMetric> page = metricRepository.findAll(spec, pageable);
        return PageResult.of(page);
    }
    
    @Override
    public Map<String, Object> analyzeAssetValue(Long assetId) {
        log.info("Analyzing asset value: {}", assetId);
        
        List<DataValue> values = valueRepository.findByAssetId(assetId);
        DataValue latestValue = valueRepository.findLatestValueByAsset(assetId);
        
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("assetId", assetId);
        analysis.put("totalAssessments", values.size());
        analysis.put("latestValue", latestValue);
        
        if (values.size() > 0) {
            double avgScore = values.stream().mapToDouble(DataValue::getValueScore).average().orElse(0);
            double maxScore = values.stream().mapToDouble(DataValue::getValueScore).max().orElse(0);
            double minScore = values.stream().mapToDouble(DataValue::getValueScore).min().orElse(0);
            
            analysis.put("averageScore", avgScore);
            analysis.put("maxScore", maxScore);
            analysis.put("minScore", minScore);
            analysis.put("scoreRange", maxScore - minScore);
            
            // 价值趋势分析
            if (values.size() >= 3) {
                double trend = calculateTrend(values);
                analysis.put("valueTrend", trend);
                analysis.put("trendDescription", trend > 0 ? "上升趋势" : trend < 0 ? "下降趋势" : "稳定趋势");
            }
        }
        
        return analysis;
    }
    
    @Override
    public Map<String, Object> analyzeTrend(Long assetId) {
        log.info("Analyzing trend for asset: {}", assetId);
        
        List<DataValue> values = valueRepository.findByAssetId(assetId);
        Map<String, Object> trendAnalysis = new HashMap<>();
        
        if (values.size() > 1) {
            // 按时间排序
            values.sort((v1, v2) -> v1.getAssessmentDate().compareTo(v2.getAssessmentDate()));
            
            List<Double> scores = values.stream().map(DataValue::getValueScore).collect(java.util.stream.Collectors.toList());
            List<LocalDateTime> dates = values.stream().map(DataValue::getAssessmentDate).collect(java.util.stream.Collectors.toList());
            
            double trendSlope = calculateRegressionSlope(scores);
            double correlation = calculateCorrelation(scores);
            
            trendAnalysis.put("slope", trendSlope);
            trendAnalysis.put("correlation", correlation);
            trendAnalysis.put("trendStrength", Math.abs(correlation));
            trendAnalysis.put("prediction", predictNextValue(scores));
            trendAnalysis.put("historicalData", Map.of("scores", scores, "dates", dates));
        }
        
        return trendAnalysis;
    }
    
    @Override
    public Map<String, Object> compareValues(Long assetId1, Long assetId2) {
        log.info("Comparing values between assets {} and {}", assetId1, assetId2);
        
        DataValue value1 = valueRepository.findLatestValueByAsset(assetId1);
        DataValue value2 = valueRepository.findLatestValueByAsset(assetId2);
        
        Map<String, Object> comparison = new HashMap<>();
        comparison.put("asset1", value1);
        comparison.put("asset2", value2);
        
        if (value1 != null && value2 != null) {
            double difference = value1.getValueScore() - value2.getValueScore();
            double ratio = value1.getValueScore() / value2.getValueScore();
            
            comparison.put("difference", difference);
            comparison.put("ratio", ratio);
            comparison.put("comparisonResult", 
                difference > 0 ? "Asset1价值更高" : difference < 0 ? "Asset2价值更高" : "价值相等");
        }
        
        return comparison;
    }
    
    @Override
    public Map<String, Object> predictFutureValue(Long assetId) {
        log.info("Predicting future value for asset: {}", assetId);
        
        List<DataValue> values = valueRepository.findByAssetId(assetId);
        Map<String, Object> prediction = new HashMap<>();
        
        if (values.size() >= 3) {
            // 使用线性回归预测
            List<Double> scores = values.stream().map(DataValue::getValueScore).collect(java.util.stream.Collectors.toList());
            double predictedValue = predictNextValue(scores);
            
            prediction.put("assetId", assetId);
            prediction.put("currentValue", scores.get(scores.size() - 1));
            prediction.put("predictedValue", predictedValue);
            prediction.put("confidenceLevel", 0.85);
            prediction.put("predictionMethod", "线性回归");
            prediction.put("nextAssessmentDate", LocalDateTime.now().plusDays(30));
        } else {
            prediction.put("message", "历史数据不足，无法进行预测");
        }
        
        return prediction;
    }
    
    @Override
    @Transactional
    public void calculateValueTrends() {
        log.info("Calculating value trends for all assets");
        
        // 找出所有需要计算趋势的资产
        List<DataValue> values = valueRepository.findAll();
        
        // 按资产分组
        Map<Long, List<DataValue>> assetValues = new HashMap<>();
        for (DataValue value : values) {
            assetValues.computeIfAbsent(value.getAssetId(), k -> new ArrayList<>()).add(value);
        }
        
        for (Map.Entry<Long, List<DataValue>> entry : assetValues.entrySet()) {
            Long assetId = entry.getKey();
            List<DataValue> assetValuesList = entry.getValue();
            
            if (assetValuesList.size() >= 3) {
                double trend = calculateTrend(assetValuesList);
                DataValue latestValue = assetValuesList.get(assetValuesList.size() - 1);
                latestValue.setValueTrend(trend);
                valueRepository.save(latestValue);
                log.info("Calculated trend for asset {}: {}", assetId, trend);
            }
        }
    }
    
    @Override
    public Map<String, Object> generateValueReport(Long assetId) {
        log.info("Generating value report for asset: {}", assetId);
        
        Map<String, Object> analysis = analyzeAssetValue(assetId);
        Map<String, Object> trendAnalysis = analyzeTrend(assetId);
        
        Map<String, Object> report = new HashMap<>();
        report.put("assetAnalysis", analysis);
        report.put("trendAnalysis", trendAnalysis);
        report.put("comparisonAnalysis", compareValues(assetId, 1L)); // 与示例资产比较
        report.put("predictionAnalysis", predictFutureValue(assetId));
        report.put("recommendations", generateRecommendations(analysis, trendAnalysis));
        report.put("generatedDate", LocalDateTime.now());
        
        return report;
    }
    
    // 私有辅助方法
    private double calculateMetricScore(ValueMetric metric, Long assetId) {
        // 模拟根据度量标准计算分数
        return Math.random() * (metric.getMaxValue() - metric.getMinValue()) + metric.getMinValue();
    }
    
    private double calculateTrend(List<DataValue> values) {
        if (values.size() < 3) return 0;
        
        // 简单趋势计算：最近3个值的平均变化
        values.sort((v1, v2) -> v1.getAssessmentDate().compareTo(v2.getAssessmentDate()));
        
        double change1 = values.get(values.size() - 1).getValueScore() - values.get(values.size() - 2).getValueScore();
        double change2 = values.get(values.size() - 2).getValueScore() - values.get(values.size() - 3).getValueScore();
        
        return (change1 + change2) / 2;
    }
    
    private double calculateRegressionSlope(List<Double> scores) {
        if (scores.size() <= 1) return 0;
        
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        for (int i = 0; i < scores.size(); i++) {
            sumX += i;
            sumY += scores.get(i);
            sumXY += i * scores.get(i);
            sumX2 += i * i;
        }
        
        double n = scores.size();
        return (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
    }
    
    private double calculateCorrelation(List<Double> scores) {
        if (scores.size() <= 1) return 0;
        
        List<Double> timeIndexes = new ArrayList<>();
        for (int i = 0; i < scores.size(); i++) {
            timeIndexes.add((double) i);
        }
        
        return calculateCorrelation(timeIndexes, scores);
    }
    
    private double calculateCorrelation(List<Double> x, List<Double> y) {
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0, sumY2 = 0;
        for (int i = 0; i < x.size(); i++) {
            sumX += x.get(i);
            sumY += y.get(i);
            sumXY += x.get(i) * y.get(i);
            sumX2 += x.get(i) * x.get(i);
            sumY2 += y.get(i) * y.get(i);
        }
        
        double n = x.size();
        double numerator = n * sumXY - sumX * sumY;
        double denominator = Math.sqrt((n * sumX2 - sumX * sumX) * (n * sumY2 - sumY * sumY));
        
        return denominator == 0 ? 0 : numerator / denominator;
    }
    
    private double predictNextValue(List<Double> scores) {
        if (scores.size() < 2) return scores.get(0);
        
        double slope = calculateRegressionSlope(scores);
        return scores.get(scores.size() - 1) + slope;
    }
    
    private List<String> generateRecommendations(Map<String, Object> analysis, Map<String, Object> trendAnalysis) {
        List<String> recommendations = new ArrayList<>();
        
        Double averageScore = (Double) analysis.get("averageScore");
        Double valueTrend = (Double) trendAnalysis.get("slope");
        
        if (averageScore != null) {
            if (averageScore < 50) {
                recommendations.add("价值较低，建议优化数据质量和可用性");
            } else if (averageScore < 70) {
                recommendations.add("价值中等，可以考虑进一步提升");
            } else {
                recommendations.add("价值较高，继续保持良好状态");
            }
        }
        
        if (valueTrend != null) {
            if (valueTrend < -1) {
                recommendations.add("价值呈下降趋势，需关注数据衰减问题");
            } else if (valueTrend > 1) {
                recommendations.add("价值呈上升趋势，继续保持数据价值提升");
            }
        }
        
        return recommendations;
    }
}