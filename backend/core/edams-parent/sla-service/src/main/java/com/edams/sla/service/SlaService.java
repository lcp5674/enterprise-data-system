package com.edams.sla.service;

import com.edams.common.model.PageResult;
import com.edams.sla.entity.SlaAgreement;
import com.edams.sla.entity.SlaReport;

import java.util.Map;

public interface SlaService {
    
    // SLA协议管理
    SlaAgreement createAgreement(SlaAgreement agreement);
    SlaAgreement updateAgreement(Long id, SlaAgreement agreement);
    void deleteAgreement(Long id);
    SlaAgreement getAgreementById(Long id);
    PageResult<SlaAgreement> listAgreements(Map<String, Object> params);
    
    // SLA监控
    void monitorSla(Long agreementId);
    Map<String, Object> checkSlaCompliance(Long agreementId);
    void generateReport(Long agreementId, String reportPeriod, Long userId);
    
    // SLA报告管理
    SlaReport getReportById(Long id);
    PageResult<SlaReport> listReports(Map<String, Object> params);
    void sendReportNotification(Long reportId);
    
    // SLA统计
    Map<String, Object> getSlaStats(Long agreementId);
    Map<String, Object> getServiceSlaStats(String serviceName);
    
    // SLA预警
    void checkViolations();
    void sendViolationAlert(Long agreementId);
}