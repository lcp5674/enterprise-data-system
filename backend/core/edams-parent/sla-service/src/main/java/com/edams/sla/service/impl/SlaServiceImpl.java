package com.edams.sla.service.impl;

import com.edams.common.model.PageResult;
import com.edams.common.utils.JsonUtils;
import com.edams.common.utils.PageUtils;
import com.edams.sla.entity.SlaAgreement;
import com.edams.sla.entity.SlaReport;
import com.edams.sla.repository.SlaAgreementRepository;
import com.edams.sla.repository.SlaReportRepository;
import com.edams.sla.service.SlaService;
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
public class SlaServiceImpl implements SlaService {
    
    private final SlaAgreementRepository agreementRepository;
    private final SlaReportRepository reportRepository;
    
    @Override
    @Transactional
    public SlaAgreement createAgreement(SlaAgreement agreement) {
        log.info("Creating SLA agreement: {}", agreement.getName());
        return agreementRepository.save(agreement);
    }
    
    @Override
    @Transactional
    public SlaAgreement updateAgreement(Long id, SlaAgreement agreement) {
        log.info("Updating SLA agreement: {}", id);
        SlaAgreement existing = getAgreementById(id);
        existing.setName(agreement.getName());
        existing.setDescription(agreement.getDescription());
        existing.setThresholdValue(agreement.getThresholdValue());
        existing.setWarningLevel(agreement.getWarningLevel());
        existing.setCriticalLevel(agreement.getCriticalLevel());
        existing.setEndTime(agreement.getEndTime());
        return agreementRepository.save(existing);
    }
    
    @Override
    @Transactional
    public void deleteAgreement(Long id) {
        log.info("Deleting SLA agreement: {}", id);
        agreementRepository.deleteById(id);
    }
    
    @Override
    public SlaAgreement getAgreementById(Long id) {
        return agreementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SLA agreement not found: " + id));
    }
    
    @Override
    public PageResult<SlaAgreement> listAgreements(Map<String, Object> params) {
        Specification<SlaAgreement> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (params.get("serviceName") != null) {
                predicates.add(cb.equal(root.get("serviceName"), params.get("serviceName")));
            }
            if (params.get("serviceType") != null) {
                predicates.add(cb.equal(root.get("serviceType"), params.get("serviceType")));
            }
            if (params.get("ownerId") != null) {
                predicates.add(cb.equal(root.get("ownerId"), params.get("ownerId")));
            }
            if (params.get("status") != null) {
                predicates.add(cb.equal(root.get("status"), params.get("status")));
            }
            if (params.get("metricType") != null) {
                predicates.add(cb.equal(root.get("metricType"), params.get("metricType")));
            }
            if (params.get("name") != null) {
                predicates.add(cb.like(root.get("name"), "%" + params.get("name") + "%"));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        Pageable pageable = PageUtils.getPageable(params);
        Page<SlaAgreement> page = agreementRepository.findAll(spec, pageable);
        return PageResult.of(page);
    }
    
    @Override
    @Transactional
    public void monitorSla(Long agreementId) {
        log.info("Monitoring SLA agreement: {}", agreementId);
        SlaAgreement agreement = getAgreementById(agreementId);
        
        // 模拟监控数据收集
        double currentValue = getRandomMetricValue(agreement.getMetricType());
        
        // 检查是否违反SLA
        String status = checkSlaStatus(currentValue, agreement.getThresholdValue(), 
                agreement.getWarningLevel(), agreement.getCriticalLevel());
        
        log.info("SLA monitoring result for {}: current={}, status={}", 
                agreement.getName(), currentValue, status);
        
        if (!"COMPLIANT".equals(status)) {
            sendViolationAlert(agreementId);
        }
    }
    
    @Override
    public Map<String, Object> checkSlaCompliance(Long agreementId) {
        SlaAgreement agreement = getAgreementById(agreementId);
        
        // 模拟过去24小时的数据
        List<Double> historicalData = generateHistoricalData(24);
        
        double avgValue = historicalData.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double maxValue = historicalData.stream().mapToDouble(Double::doubleValue).max().orElse(0);
        double minValue = historicalData.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        
        int violationCount = 0;
        int warningCount = 0;
        int criticalCount = utils.parseInt(0);
        
        for (Double value : historicalData) {
            String status = checkSlaStatus(value, agreement.getThresholdValue(), 
                    agreement.getWarningLevel(), agreement.getCriticalLevel());
            
            if ("VIOLATION".equals(status)) {
                violationCount++;
            } else if ("WARNING".equals(status)) {
                warningCount++;
            } else if ("CRITICAL".equals(status)) {
                criticalCount++;
            }
        }
        
        double complianceRate = historicalData.size() > 0 ? 
                (double) (historicalData.size() - violationCount) / historicalData.size() : 0;
        
        Map<String, Object> result = new HashMap<>();
        result.put("agreement", agreement);
        result.put("avgValue", avgValue);
        result.put("maxValue", maxValue);
        result.put("minValue", minValue);
        result.put("violationCount", violationCount);
        result.put("warningCount", warningCount);
        result.put("criticalCount", criticalCount);
        result.put("complianceRate", complianceRate);
        result.put("totalSamples", historicalData.size());
        result.put("analysisResult", complianceRate >= 0.95 ? "COMPLIANT" : "WARNING");
        
        return result;
    }
    
    @Override
    @Transactional
    public void generateReport(Long agreementId, String reportPeriod, Long userId) {
        log.info("Generating SLA report for agreement {} with period {}", agreementId, reportPeriod);
        
        Map<String, Object> complianceData = checkSlaCompliance(agreementId);
        SlaAgreement agreement = getAgreementById(agreementId);
        
        SlaReport report = new SlaReport();
        report.setAgreementId(agreementId);
        report.setReportPeriod(reportPeriod);
        report.setPeriodStart(LocalDateTime.now().minusDays(1));
        report.setPeriodEnd(LocalDateTime.now());
        report.setMetricValue((Double) complianceData.get("avgValue"));
        report.setComplianceRate((Double) complianceData.get("complianceRate"));
        report.setViolationCount((Integer) complianceData.get("violationCount"));
        report.setWarningCount((Integer) complianceData.get("warningCount"));
        report.setCriticalCount((Integer) complianceData.get("criticalCount"));
        report.setTotalSamples((Integer) complianceData.get("totalSamples"));
        report.setAvgValue((Double) complianceData.get("avgValue"));
        report.setMaxValue((Double) complianceData.get("maxValue"));
        report.setMinValue((Double) complianceData.get("minValue"));
        report.setAnalysisResult((String) complianceData.get("analysisResult"));
        report.setGeneratedBy(userId);
        
        // 生成报告内容
        report.setReportContent(JsonUtils.toJson(Map.of(
            "agreementName", agreement.getName(),
            "serviceName", agreement.getServiceName(),
            "metricType", agreement.getMetricType(),
            "threshold", agreement.getThresholdValue(),
            "warningLevel", agreement.getWarningLevel(),
            "criticalLevel", agreement.getCriticalLevel(),
            "analysis", complianceData,
            "recommendations", generateRecommendations(complianceData)
        )));
        
        reportRepository.save(report);
        log.info("SLA report generated: {}", report.getId());
    }
    
    @Override
    public SlaReport getReportById(Long id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SLA report not found: " + id));
    }
    
    @Override
    public PageResult<SlaReport> listReports(Map<String, Object> params) {
        Specification<SlaReport> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (params.get("agreementId") != null) {
                predicates.add(cb.equal(root.get("agreementId"), params.get("agreementId")));
            }
            if (params.get("reportPeriod") != null) {
                predicates.add(cb.equal(root.get("reportPeriod"), params.get("reportPeriod")));
            }
            if (params.get("analysisResult") != null) {
                predicates.add(cb.equal(root.get("analysisResult"), params.get("analysisResult")));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        Pageable pageable = PageUtils.getPageable(params);
        Page<SlaReport> page = reportRepository.findAll(spec, pageable);
        return PageResult.of(page);
    }
    
    @Override
    @Transactional
    public void sendReportNotification(Long reportId) {
        log.info("Sending report notification: {}", reportId);
        SlaReport report = getReportById(reportId);
        report.setNotificationStatus("SENT");
        reportRepository.save(report);
        log.info("Report notification sent for report {}", reportId);
    }
    
    @Override
    public Map<String, Object> getSlaStats(Long agreementId) {
        SlaAgreement agreement = getAgreementById(agreementId);
        List<SlaReport> reports = reportRepository.findByAgreementId(agreementId);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("agreement", agreement);
        stats.put("totalReports", reports.size());
        stats.put("complianceReports", reports.stream().filter(r -> "COMPLIANT".equals(r.getAnalysisResult())).count());
        stats.put("warningReports", reports.stream().filter(r -> "WARNING".equals(r.getAnalysisResult())).count());
        stats.put("criticalReports", reports.stream().filter(r -> "CRITICAL".equals(r.getAnalysisResult())).count());
        stats.put("avgComplianceRate", reports.stream().mapToDouble(SlaReport::getComplianceRate).average().orElse(0));
        stats.put("latestReport", reports.isEmpty() ? null : reports.get(0));
        
        return stats;
    }
    
    @Override
    public Map<String, Object> getServiceSlaStats(String serviceName) {
        List<SlaAgreement> agreements = agreementRepository.findByServiceName(serviceName);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("serviceName", serviceName);
        stats.put("totalAgreements", agreements.size());
        stats.put("activeAgreements", agreements.stream().filter(a -> "ACTIVE".equals(a.getStatus())).count());
        stats.put("expiredAgreements", agreements.stream().filter(a -> "INACTIVE".equals(a.getStatus())).count());
        
        // 统计每种metric类型
        Map<String, Integer> metricStats = new HashMap<>();
        for (SlaAgreement agreement : agreements) {
            metricStats.merge(agreement.getMetricType(), 1, Integer::sum);
        }
        stats.put("metricTypeStats", metricStats);
        
        return stats;
    }
    
    @Override
    @Transactional
    public void checkViolations() {
        log.info("Checking SLA violations");
        List<SlaAgreement> activeAgreements = agreementRepository.findByStatus("ACTIVE");
        
        for (SlaAgreement agreement : activeAgreements) {
            monitorSla(agreement.getId());
        }
    }
    
    @Override
    @Transactional
    public void sendViolationAlert(Long agreementId) {
        log.info("Sending violation alert for agreement: {}", agreementId);
        SlaAgreement agreement = getAgreementById(agreementId);
        
        // 模拟发送警报
        log.warn("SLA violation alert: Agreement '{}' for service '{}' has violated threshold {} {}",
                agreement.getName(), agreement.getServiceName(), 
                agreement.getThresholdValue(), agreement.getThresholdUnit());
    }
    
    // 私有辅助方法
    private double getRandomMetricValue(String metricType) {
        switch (metricType) {
            case "RESPONSE_TIME":
                return Math.random() * 1000; // 0-1000ms
            case "AVAILABILITY":
                return Math.random() * 100; // 0-100%
            case "ERROR_RATE":
                return Math.random() * 10; // 0-10%
            default:
                return Math.random() * 100;
        }
    }
    
    private String checkSlaStatus(double value, double threshold, double warningLevel, double criticalLevel) {
        if (value <= threshold) {
            return "COMPLIANT";
        } else if (value <= warningLevel) {
            return "WARNING";
        } else if (value <= criticalLevel) {
            return "VIOLATION";
        } else {
            return "CRITICAL";
        }
    }
    
    private List<Double> generateHistoricalData(int hours) {
        List<Double> data = new ArrayList<>();
        for (int i = 0; i < hours; i++) {
            data.add(Math.random() * 100); // 生成随机数据
        }
        return data;
    }
    
    private List<String> generateRecommendations(Map<String, Object> complianceData) {
        double complianceRate = (Double) complianceData.get("complianceRate");
        List<String> recommendations = new ArrayList<>();
        
        if (complianceRate < 0.9) {
            recommendations.add("建议调整SLA阈值，当前达标率较低");
        }
        if (complianceRate < 0.95) {
            recommendations.add("建议优化系统性能，减少SLA违规次数");
        }
        if ((Integer) complianceData.get("criticalCount") > 0) {
            recommendations.add("存在严重违规，建议立即采取行动");
        }
        
        return recommendations;
    }
}