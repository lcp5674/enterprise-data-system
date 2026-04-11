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
        
        // TODO: 实际生成报表逻辑
        log.info("生成报表: id={}, params={}", id, params);
        return new byte[0];
    }

    @Override
    public byte[] exportReport(Long id, String format, Map<String, Object> params) {
        Report report = reportMapper.selectById(id);
        if (report == null) {
            throw new BusinessException("报表不存在: " + id);
        }
        
        // TODO: 实际导出报表逻辑
        log.info("导出报表: id={}, format={}", id, format);
        return new byte[0];
    }

    @Override
    public String previewReport(Long id) {
        Report report = reportMapper.selectById(id);
        if (report == null) {
            throw new BusinessException("报表不存在: " + id);
        }
        
        // TODO: 实际预览报表逻辑
        return "报表预览";
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
            // TODO: 实际执行报表逻辑
            log.info("执行报表: id={}", id);
            
            reportMapper.updateExecuteInfo(id, now, "SUCCESS");
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "执行成功");
            return result;
        } catch (Exception e) {
            reportMapper.updateExecuteInfo(id, now, "FAILED");
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
