package com.enterprise.edams.analytics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.analytics.dto.AnalyticsReportCreateRequest;
import com.enterprise.edams.analytics.dto.AnalyticsReportDTO;
import com.enterprise.edams.analytics.entity.AnalyticsReport;
import com.enterprise.edams.analytics.repository.AnalyticsReportMapper;
import com.enterprise.edams.analytics.service.AnalyticsReportService;
import com.enterprise.edams.common.exception.BusinessException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 * 分析报告服务实现
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsReportServiceImpl implements AnalyticsReportService {

    private final AnalyticsReportMapper reportMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AnalyticsReportDTO createReport(AnalyticsReportCreateRequest request, Long creatorId, String creatorName) {
        // 校验报告名称
        if (reportMapper.existsByReportName(request.getReportName()) > 0) {
            throw new BusinessException("报告名称已存在: " + request.getReportName());
        }

        // 生成报告编码
        String reportCode = StringUtils.isNotBlank(request.getReportCode()) 
                ? request.getReportCode() 
                : generateReportCode();
        
        if (reportMapper.existsByReportCode(reportCode) > 0) {
            throw new BusinessException("报告编码已存在: " + reportCode);
        }

        AnalyticsReport report = new AnalyticsReport();
        BeanUtils.copyProperties(request, report);
        report.setReportCode(reportCode);
        report.setStatus(0); // 草稿
        report.setCreatorId(creatorId);
        report.setCreatorName(creatorName);
        report.setExecuteCount(0);
        report.setViewCount(0);

        // JSON转换
        try {
            if (request.getConfig() != null) {
                report.setConfigJson(objectMapper.writeValueAsString(request.getConfig()));
            }
            if (request.getDimensions() != null) {
                report.setDimensions(objectMapper.writeValueAsString(request.getDimensions()));
            }
            if (request.getMetrics() != null) {
                report.setMetrics(objectMapper.writeValueAsString(request.getMetrics()));
            }
            if (request.getFilters() != null) {
                report.setFilters(objectMapper.writeValueAsString(request.getFilters()));
            }
            if (request.getSortRules() != null) {
                report.setSortRules(objectMapper.writeValueAsString(request.getSortRules()));
            }
        } catch (JsonProcessingException e) {
            throw new BusinessException("JSON转换失败: " + e.getMessage());
        }

        reportMapper.insert(report);
        log.info("报告创建成功: id={}, name={}", report.getId(), report.getReportName());
        return convertToDTO(report);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AnalyticsReportDTO updateReport(Long id, AnalyticsReportCreateRequest request) {
        AnalyticsReport report = reportMapper.selectById(id);
        if (report == null) {
            throw new BusinessException("报告不存在: " + id);
        }

        // 校验名称唯一性
        AnalyticsReport existReport = reportMapper.selectByReportName(request.getReportName());
        if (existReport != null && !existReport.getId().equals(id)) {
            throw new BusinessException("报告名称已存在: " + request.getReportName());
        }

        report.setReportName(request.getReportName());
        report.setDatasourceId(request.getDatasourceId());
        report.setQuerySql(request.getQuerySql());
        report.setDescription(request.getDescription());
        report.setChartType(request.getChartType());
        report.setRefreshType(request.getRefreshType());
        report.setAccessLevel(request.getAccessLevel());

        // JSON转换
        try {
            if (request.getConfig() != null) {
                report.setConfigJson(objectMapper.writeValueAsString(request.getConfig()));
            }
            if (request.getDimensions() != null) {
                report.setDimensions(objectMapper.writeValueAsString(request.getDimensions()));
            }
            if (request.getMetrics() != null) {
                report.setMetrics(objectMapper.writeValueAsString(request.getMetrics()));
            }
            if (request.getFilters() != null) {
                report.setFilters(objectMapper.writeValueAsString(request.getFilters()));
            }
            if (request.getSortRules() != null) {
                report.setSortRules(objectMapper.writeValueAsString(request.getSortRules()));
            }
        } catch (JsonProcessingException e) {
            throw new BusinessException("JSON转换失败: " + e.getMessage());
        }

        reportMapper.updateById(report);
        log.info("报告更新成功: id={}", id);
        return convertToDTO(report);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteReport(Long id) {
        AnalyticsReport report = reportMapper.selectById(id);
        if (report == null) {
            throw new BusinessException("报告不存在: " + id);
        }
        reportMapper.deleteById(id);
        log.info("报告删除成功: id={}", id);
    }

    @Override
    public AnalyticsReportDTO getReportById(Long id) {
        AnalyticsReport report = reportMapper.selectById(id);
        if (report == null) {
            throw new BusinessException("报告不存在: " + id);
        }
        return convertToDTO(report);
    }

    @Override
    public AnalyticsReportDTO getReportByCode(String reportCode) {
        AnalyticsReport report = reportMapper.selectByReportCode(reportCode);
        if (report == null) {
            throw new BusinessException("报告不存在: " + reportCode);
        }
        return convertToDTO(report);
    }

    @Override
    public IPage<AnalyticsReportDTO> queryReports(String keyword, String reportType, Integer status, 
                                                  String accessLevel, int pageNum, int pageSize) {
        Page<AnalyticsReport> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AnalyticsReport> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(AnalyticsReport::getReportName, keyword)
                    .or().like(AnalyticsReport::getReportCode, keyword));
        }
        if (StringUtils.isNotBlank(reportType)) {
            wrapper.eq(AnalyticsReport::getReportType, reportType);
        }
        if (status != null) {
            wrapper.eq(AnalyticsReport::getStatus, status);
        }
        if (StringUtils.isNotBlank(accessLevel)) {
            wrapper.eq(AnalyticsReport::getAccessLevel, accessLevel);
        }

        wrapper.orderByDesc(AnalyticsReport::getCreatedTime);
        
        IPage<AnalyticsReport> reportPage = reportMapper.selectPage(page, wrapper);
        List<AnalyticsReportDTO> dtoList = reportPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        Page<AnalyticsReportDTO> resultPage = new Page<>(reportPage.getCurrent(), reportPage.getSize(), reportPage.getTotal());
        resultPage.setRecords(dtoList);
        return resultPage;
    }

    @Override
    public List<AnalyticsReportDTO> getReportsByType(String reportType) {
        return reportMapper.selectByReportType(reportType).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AnalyticsReportDTO> getReportsByCreator(Long creatorId) {
        return reportMapper.selectByCreatorId(creatorId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishReport(Long id) {
        reportMapper.updateStatus(id, 1);
        log.info("报告发布成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void archiveReport(Long id) {
        reportMapper.updateStatus(id, 2);
        log.info("报告归档成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> executeReport(Long id) {
        AnalyticsReport report = reportMapper.selectById(id);
        if (report == null) {
            throw new BusinessException("报告不存在: " + id);
        }

        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        try {
            // TODO: 实际执行SQL查询
            log.info("执行报告: id={}, sql={}", id, report.getQuerySql());
            
            // 模拟执行结果
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "执行成功");
            result.put("data", new ArrayList<>());
            
            // 更新执行记录
            reportMapper.updateExecuteInfo(id, now, "SUCCESS");
            
            return result;
        } catch (Exception e) {
            reportMapper.updateExecuteInfo(id, now, "FAILED");
            throw new BusinessException("报告执行失败: " + e.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> getReportData(Long id) {
        // TODO: 实际查询数据
        return new ArrayList<>();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementViewCount(Long id) {
        reportMapper.incrementViewCount(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AnalyticsReportDTO cloneReport(Long id, String newReportName, Long creatorId, String creatorName) {
        AnalyticsReport source = reportMapper.selectById(id);
        if (source == null) {
            throw new BusinessException("原报告不存在: " + id);
        }

        AnalyticsReportCreateRequest request = new AnalyticsReportCreateRequest();
        request.setReportName(newReportName);
        request.setReportType(source.getReportType());
        request.setDatasourceId(source.getDatasourceId());
        request.setQuerySql(source.getQuerySql());
        request.setDescription(source.getDescription());
        request.setChartType(source.getChartType());
        request.setRefreshType(source.getRefreshType());
        request.setAccessLevel(source.getAccessLevel());

        // JSON转换
        try {
            if (source.getConfigJson() != null) {
                request.setConfig(objectMapper.readValue(source.getConfigJson(), new TypeReference<Map<String, Object>>() {}));
            }
            if (source.getDimensions() != null) {
                request.setDimensions(objectMapper.readValue(source.getDimensions(), new TypeReference<List<Map<String, Object>>>() {}));
            }
            if (source.getMetrics() != null) {
                request.setMetrics(objectMapper.readValue(source.getMetrics(), new TypeReference<List<Map<String, Object>>>() {}));
            }
        } catch (JsonProcessingException e) {
            throw new BusinessException("JSON解析失败: " + e.getMessage());
        }

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

    private AnalyticsReportDTO convertToDTO(AnalyticsReport report) {
        AnalyticsReportDTO dto = new AnalyticsReportDTO();
        BeanUtils.copyProperties(report, dto);

        // JSON解析
        try {
            if (StringUtils.isNotBlank(report.getConfigJson())) {
                dto.setConfig(objectMapper.readValue(report.getConfigJson(), new TypeReference<Map<String, Object>>() {}));
            }
            if (StringUtils.isNotBlank(report.getDimensions())) {
                dto.setDimensions(objectMapper.readValue(report.getDimensions(), new TypeReference<List<Map<String, Object>>>() {}));
            }
            if (StringUtils.isNotBlank(report.getMetrics())) {
                dto.setMetrics(objectMapper.readValue(report.getMetrics(), new TypeReference<List<Map<String, Object>>>() {}));
            }
            if (StringUtils.isNotBlank(report.getFilters())) {
                dto.setFilters(objectMapper.readValue(report.getFilters(), new TypeReference<Map<String, Object>>() {}));
            }
            if (StringUtils.isNotBlank(report.getSortRules())) {
                dto.setSortRules(objectMapper.readValue(report.getSortRules(), new TypeReference<List<Map<String, Object>>>() {}));
            }
        } catch (JsonProcessingException e) {
            log.error("JSON解析失败: {}", e.getMessage());
        }

        return dto;
    }
}
