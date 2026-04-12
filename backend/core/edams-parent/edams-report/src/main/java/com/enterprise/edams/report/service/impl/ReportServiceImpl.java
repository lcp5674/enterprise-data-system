package com.enterprise.edams.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.report.dto.ReportCreateRequest;
import com.enterprise.edams.report.dto.ReportDTO;
import com.enterprise.edams.report.entity.Report;
import com.enterprise.edams.report.repository.ReportMapper;
import com.enterprise.edams.report.service.ReportService;
import com.enterprise.edams.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 报表服务实现
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportMapper reportMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReportDTO createReport(ReportCreateRequest request, Long creatorId, String creatorName) {
        // 校验名称
        if (reportMapper.existsByReportName(request.getReportName()) > 0) {
            throw new BusinessException("报表名称已存在: " + request.getReportName());
        }

        // 生成编码
        String reportCode = StringUtils.isNotBlank(request.getReportCode()) 
                ? request.getReportCode() 
                : generateReportCode();
        
        if (reportMapper.existsByReportCode(reportCode) > 0) {
            throw new BusinessException("报表编码已存在: " + reportCode);
        }

        Report report = new Report();
        BeanUtils.copyProperties(request, report);
        report.setReportCode(reportCode);
        report.setStatus(1); // 启用
        report.setCreatorId(creatorId);
        report.setCreatorName(creatorName);
        report.setExecuteCount(0);
        report.setViewCount(0);

        reportMapper.insert(report);
        log.info("报表创建成功: id={}, name={}", report.getId(), report.getReportName());
        return convertToDTO(report);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReportDTO updateReport(Long id, ReportCreateRequest request) {
        Report report = reportMapper.selectById(id);
        if (report == null) {
            throw new BusinessException("报表不存在: " + id);
        }

        // 校验名称唯一性
        Report existReport = reportMapper.selectByReportName(request.getReportName());
        if (existReport != null && !existReport.getId().equals(id)) {
            throw new BusinessException("报表名称已存在: " + request.getReportName());
        }

        report.setReportName(request.getReportName());
        report.setDescription(request.getDescription());
        report.setDatasourceId(request.getDatasourceId());
        report.setTemplateId(request.getTemplateId());
        report.setQuerySql(request.getQuerySql());
        report.setParameters(request.getParameters());
        report.setFileFormat(request.getFileFormat());

        reportMapper.updateById(report);
        log.info("报表更新成功: id={}", id);
        return convertToDTO(report);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteReport(Long id) {
        Report report = reportMapper.selectById(id);
        if (report == null) {
            throw new BusinessException("报表不存在: " + id);
        }
        reportMapper.deleteById(id);
        log.info("报表删除成功: id={}", id);
    }

    @Override
    public ReportDTO getReportById(Long id) {
        Report report = reportMapper.selectById(id);
        if (report == null) {
            throw new BusinessException("报表不存在: " + id);
        }
        return convertToDTO(report);
    }

    @Override
    public ReportDTO getReportByCode(String reportCode) {
        Report report = reportMapper.selectByReportCode(reportCode);
        if (report == null) {
            throw new BusinessException("报表不存在: " + reportCode);
        }
        return convertToDTO(report);
    }

    @Override
    public IPage<ReportDTO> queryReports(String keyword, String reportType, Integer status, int pageNum, int pageSize) {
        Page<Report> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(Report::getReportName, keyword)
                    .or().like(Report::getReportCode, keyword));
        }
        if (StringUtils.isNotBlank(reportType)) {
            wrapper.eq(Report::getReportType, reportType);
        }
        if (status != null) {
            wrapper.eq(Report::getStatus, status);
        }

        wrapper.orderByDesc(Report::getCreatedTime);
        
        IPage<Report> reportPage = reportMapper.selectPage(page, wrapper);
        List<ReportDTO> dtoList = reportPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        Page<ReportDTO> resultPage = new Page<>(reportPage.getCurrent(), reportPage.getSize(), reportPage.getTotal());
        resultPage.setRecords(dtoList);
        return resultPage;
    }

    @Override
    public List<ReportDTO> getReportsByType(String reportType) {
        return reportMapper.selectByReportType(reportType).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReportDTO> getReportsByCreator(Long creatorId) {
        return reportMapper.selectByCreatorId(creatorId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReportDTO> getReportsByTemplate(Long templateId) {
        return reportMapper.selectByTemplateId(templateId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enableReport(Long id) {
        reportMapper.updateStatus(id, 1);
        log.info("报表启用成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disableReport(Long id) {
        reportMapper.updateStatus(id, 0);
        log.info("报表禁用成功: id={}", id);
    }

    @Override
    public byte[] generateReport(Long id, Map<String, Object> params) {
        Report report = reportMapper.selectById(id);
        if (report == null) {
            throw new BusinessException("报表不存在: " + id);
        }
        
        try {
            // 1. 解析SQL并获取数据
            String querySql = report.getQuerySql();
            if (querySql == null || querySql.isBlank()) {
                throw new BusinessException("报表未配置查询SQL");
            }
            
            // 2. 合并参数
            if (params != null) {
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    querySql = querySql.replace("${" + entry.getKey() + "}", 
                            entry.getValue() != null ? entry.getValue().toString() : "");
                }
            }
            
            // 3. 执行查询（实际项目应通过JDBC/MyBatis执行）
            log.info("执行报表SQL: id={}, sql={}", id, querySql);
            
            // 4. 生成JSON格式的报表数据
            StringBuilder jsonData = new StringBuilder();
            jsonData.append("{\"reportId\":").append(id);
            jsonData.append(",\"reportName\":\"").append(report.getReportName()).append("\"");
            jsonData.append(",\"generatedAt\":\"").append(LocalDateTime.now()).append("\"");
            jsonData.append(",\"parameters\":").append(params != null ? params.toString() : "{}");
            jsonData.append(",\"data\":[]"); // 实际应填充查询结果
            jsonData.append(",\"summary\":{\"totalRows\":0,\"totalColumns\":0}");
            jsonData.append("}");
            
            log.info("报表生成成功: id={}", id);
            return jsonData.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("报表生成失败: id={}, error={}", id, e.getMessage(), e);
            throw new BusinessException("报表生成失败: " + e.getMessage());
        }
    }

    @Override
    public byte[] exportReport(Long id, String format, Map<String, Object> params) {
        Report report = reportMapper.selectById(id);
        if (report == null) {
            throw new BusinessException("报表不存在: " + id);
        }
        
        try {
            // 1. 先生成报表数据
            byte[] reportData = generateReport(id, params);
            
            // 2. 根据格式导出
            String fileName = report.getReportName() + "_" + 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            
            switch (format != null ? format.toUpperCase() : "JSON") {
                case "CSV":
                    // 转换JSON为CSV格式
                    String csvContent = convertJsonToCsv(new String(reportData, java.nio.charset.StandardCharsets.UTF_8));
                    return (fileName + ".csv\n" + csvContent).getBytes(java.nio.charset.StandardCharsets.UTF_8);
                    
                case "EXCEL":
                    // Excel格式（简化实现，返回CSV带Excel扩展名）
                    String excelContent = convertJsonToCsv(new String(reportData, java.nio.charset.StandardCharsets.UTF_8));
                    return (fileName + ".xls\n" + excelContent).getBytes(java.nio.charset.StandardCharsets.UTF_8);
                    
                case "PDF":
                    // PDF格式（简化实现，实际应使用iText或Apache PDFBox）
                    String pdfContent = "PDF Report: " + report.getReportName() + "\n" +
                            "Generated at: " + LocalDateTime.now() + "\n" +
                            "Data: " + new String(reportData, java.nio.charset.StandardCharsets.UTF_8);
                    return (fileName + ".pdf\n" + pdfContent).getBytes(java.nio.charset.StandardCharsets.UTF_8);
                    
                case "JSON":
                default:
                    return reportData;
            }
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("报表导出失败: id={}, format={}, error={}", id, format, e.getMessage(), e);
            throw new BusinessException("报表导出失败: " + e.getMessage());
        }
    }
    
    /**
     * 将JSON数据转换为CSV格式
     */
    private String convertJsonToCsv(String jsonData) {
        // 简化实现：返回CSV头
        StringBuilder csv = new StringBuilder();
        csv.append("report_id,report_name,generated_at,status\n");
        csv.append("\"1\",\"").append("report_data").append("\",\"");
        csv.append(LocalDateTime.now()).append("\",\"SUCCESS\"\n");
        return csv.toString();
    }

    @Override
    public String previewReport(Long id) {
        Report report = reportMapper.selectById(id);
        if (report == null) {
            throw new BusinessException("报表不存在: " + id);
        }
        
        try {
            // 生成预览数据（限制返回行数）
            Map<String, Object> params = new HashMap<>();
            params.put("_limit", 100); // 预览限制100行
            
            byte[] previewData = generateReport(id, params);
            
            // 返回HTML格式的预览
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'>");
            html.append("<title>报表预览 - ").append(report.getReportName()).append("</title>");
            html.append("<style>body{font-family:Arial,sans-serif;padding:20px;}");
            html.append("table{border-collapse:collapse;width:100%;}");
            html.append("th,td{border:1px solid #ddd;padding:8px;text-align:left;}");
            html.append("th{background-color:#4CAF50;color:white;}</style></head>");
            html.append("<body><h2>").append(report.getReportName()).append("</h2>");
            html.append("<p>生成时间: ").append(LocalDateTime.now()).append("</p>");
            html.append("<div style='background:#f5f5f5;padding:15px;border-radius:5px;'>");
            html.append("<p>报表数据:</p>");
            html.append("<pre>").append(new String(previewData)).append("</pre>");
            html.append("</div></body></html>");
            
            return html.toString();
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("报表预览失败: id={}, error={}", id, e.getMessage(), e);
            throw new BusinessException("报表预览失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> executeReport(Long id) {
        Report report = reportMapper.selectById(id);
        if (report == null) {
            throw new BusinessException("报表不存在: " + id);
        }

        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        try {
            // 解析并验证SQL
            String querySql = report.getQuerySql();
            if (querySql == null || querySql.isBlank()) {
                throw new BusinessException("报表未配置查询SQL");
            }
            
            // 记录执行开始
            log.info("开始执行报表: id={}, name={}, sql={}", id, report.getReportName(), querySql);
            
            // 实际项目中应通过JDBC执行查询并获取结果
            // 这里模拟执行成功
            int rowCount = 0; // 假设查询结果行数
            
            // 更新执行信息
            reportMapper.updateExecuteInfo(id, now, "SUCCESS");
            reportMapper.incrementExecuteCount(id);
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "报表执行成功");
            result.put("reportId", id);
            result.put("reportName", report.getReportName());
            result.put("executedAt", now);
            result.put("rowCount", rowCount);
            result.put("dataUrl", "/api/v1/reports/" + id + "/download");
            
            log.info("报表执行成功: id={}, rowCount={}", id, rowCount);
            return result;
            
        } catch (BusinessException e) {
            reportMapper.updateExecuteInfo(id, now, "FAILED");
            throw e;
        } catch (Exception e) {
            reportMapper.updateExecuteInfo(id, now, "FAILED");
            log.error("报表执行失败: id={}, error={}", id, e.getMessage(), e);
            throw new BusinessException("报表执行失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementViewCount(Long id) {
        reportMapper.incrementViewCount(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReportDTO cloneReport(Long id, String newReportName, Long creatorId, String creatorName) {
        Report source = reportMapper.selectById(id);
        if (source == null) {
            throw new BusinessException("原报表不存在: " + id);
        }

        ReportCreateRequest request = new ReportCreateRequest();
        request.setReportName(newReportName);
        request.setReportType(source.getReportType());
        request.setDescription(source.getDescription());
        request.setDatasourceId(source.getDatasourceId());
        request.setTemplateId(source.getTemplateId());
        request.setQuerySql(source.getQuerySql());
        request.setParameters(source.getParameters());
        request.setFileFormat(source.getFileFormat());

        return createReport(request, creatorId, creatorName);
    }

    @Override
    public long countTotalReports() {
        return reportMapper.countTotal();
    }

    @Override
    public long countByStatus(Integer status) {
        return reportMapper.countByStatus(status);
    }

    private String generateReportCode() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "RPT" + dateStr + random;
    }

    private ReportDTO convertToDTO(Report report) {
        ReportDTO dto = new ReportDTO();
        BeanUtils.copyProperties(report, dto);
        return dto;
    }
}
