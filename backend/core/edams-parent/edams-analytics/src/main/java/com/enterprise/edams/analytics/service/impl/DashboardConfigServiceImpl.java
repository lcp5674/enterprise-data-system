package com.enterprise.edams.analytics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.analytics.dto.DashboardConfigCreateRequest;
import com.enterprise.edams.analytics.dto.DashboardConfigDTO;
import com.enterprise.edams.analytics.entity.DashboardConfig;
import com.enterprise.edams.analytics.repository.DashboardConfigMapper;
import com.enterprise.edams.analytics.service.DashboardConfigService;
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
 * 仪表盘配置服务实现
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardConfigServiceImpl implements DashboardConfigService {

    private final DashboardConfigMapper dashboardMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DashboardConfigDTO createDashboard(DashboardConfigCreateRequest request, Long creatorId, String creatorName) {
        // 生成编码
        String dashboardCode = StringUtils.isNotBlank(request.getDashboardCode()) 
                ? request.getDashboardCode() 
                : generateDashboardCode();
        
        if (dashboardMapper.existsByDashboardCode(dashboardCode) > 0) {
            throw new BusinessException("仪表盘编码已存在: " + dashboardCode);
        }

        DashboardConfig dashboard = new DashboardConfig();
        BeanUtils.copyProperties(request, dashboard);
        dashboard.setDashboardCode(dashboardCode);
        dashboard.setCreatorId(creatorId);
        dashboard.setCreatorName(creatorName);
        dashboard.setStatus(1);
        dashboard.setAccessCount(0);

        if (request.getIsDefault() == null) {
            dashboard.setIsDefault(false);
        }

        // JSON转换
        try {
            if (request.getLayoutConfig() != null) {
                dashboard.setLayoutConfig(objectMapper.writeValueAsString(request.getLayoutConfig()));
            }
            if (request.getWidgetConfig() != null) {
                dashboard.setWidgetConfig(objectMapper.writeValueAsString(request.getWidgetConfig()));
            }
        } catch (JsonProcessingException e) {
            throw new BusinessException("JSON转换失败: " + e.getMessage());
        }

        dashboardMapper.insert(dashboard);
        log.info("仪表盘创建成功: id={}, name={}", dashboard.getId(), dashboard.getDashboardName());
        return convertToDTO(dashboard);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DashboardConfigDTO updateDashboard(Long id, DashboardConfigCreateRequest request) {
        DashboardConfig dashboard = dashboardMapper.selectById(id);
        if (dashboard == null) {
            throw new BusinessException("仪表盘不存在: " + id);
        }

        dashboard.setDashboardName(request.getDashboardName());
        dashboard.setTheme(request.getTheme());
        dashboard.setRefreshInterval(request.getRefreshInterval());
        dashboard.setDescription(request.getDescription());
        dashboard.setAccessLevel(request.getAccessLevel());
        dashboard.setUserId(request.getUserId());

        // JSON转换
        try {
            if (request.getLayoutConfig() != null) {
                dashboard.setLayoutConfig(objectMapper.writeValueAsString(request.getLayoutConfig()));
            }
            if (request.getWidgetConfig() != null) {
                dashboard.setWidgetConfig(objectMapper.writeValueAsString(request.getWidgetConfig()));
            }
        } catch (JsonProcessingException e) {
            throw new BusinessException("JSON转换失败: " + e.getMessage());
        }

        dashboardMapper.updateById(dashboard);
        log.info("仪表盘更新成功: id={}", id);
        return convertToDTO(dashboard);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDashboard(Long id) {
        DashboardConfig dashboard = dashboardMapper.selectById(id);
        if (dashboard == null) {
            throw new BusinessException("仪表盘不存在: " + id);
        }
        dashboardMapper.deleteById(id);
        log.info("仪表盘删除成功: id={}", id);
    }

    @Override
    public DashboardConfigDTO getDashboardById(Long id) {
        DashboardConfig dashboard = dashboardMapper.selectById(id);
        if (dashboard == null) {
            throw new BusinessException("仪表盘不存在: " + id);
        }
        return convertToDTO(dashboard);
    }

    @Override
    public DashboardConfigDTO getDashboardByCode(String dashboardCode) {
        DashboardConfig dashboard = dashboardMapper.selectByDashboardCode(dashboardCode);
        if (dashboard == null) {
            throw new BusinessException("仪表盘不存在: " + dashboardCode);
        }
        return convertToDTO(dashboard);
    }

    @Override
    public IPage<DashboardConfigDTO> queryDashboards(String keyword, String dashboardType, Integer status, 
                                                      int pageNum, int pageSize) {
        Page<DashboardConfig> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<DashboardConfig> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(DashboardConfig::getDashboardName, keyword)
                    .or().like(DashboardConfig::getDashboardCode, keyword));
        }
        if (StringUtils.isNotBlank(dashboardType)) {
            wrapper.eq(DashboardConfig::getDashboardType, dashboardType);
        }
        if (status != null) {
            wrapper.eq(DashboardConfig::getStatus, status);
        }

        wrapper.orderByDesc(DashboardConfig::getCreatedTime);
        
        IPage<DashboardConfig> dashboardPage = dashboardMapper.selectPage(page, wrapper);
        List<DashboardConfigDTO> dtoList = dashboardPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        Page<DashboardConfigDTO> resultPage = new Page<>(dashboardPage.getCurrent(), dashboardPage.getSize(), dashboardPage.getTotal());
        resultPage.setRecords(dtoList);
        return resultPage;
    }

    @Override
    public List<DashboardConfigDTO> getDashboardsByType(String dashboardType) {
        return dashboardMapper.selectByDashboardType(dashboardType).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<DashboardConfigDTO> getDashboardsByUser(Long userId) {
        return dashboardMapper.selectByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public DashboardConfigDTO getDefaultDashboard() {
        DashboardConfig dashboard = dashboardMapper.selectDefaultDashboard();
        return dashboard != null ? convertToDTO(dashboard) : null;
    }

    @Override
    public DashboardConfigDTO getUserDefaultDashboard(Long userId) {
        DashboardConfig dashboard = dashboardMapper.selectUserDefaultDashboard(userId);
        return dashboard != null ? convertToDTO(dashboard) : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setAsDefault(Long id) {
        DashboardConfig dashboard = dashboardMapper.selectById(id);
        if (dashboard == null) {
            throw new BusinessException("仪表盘不存在: " + id);
        }

        // 清除同类型的默认设置
        dashboardMapper.clearDefaultByType(dashboard.getDashboardType());
        // 设置当前为默认
        dashboardMapper.setAsDefault(id);
        
        log.info("设置默认仪表盘成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enableDashboard(Long id) {
        dashboardMapper.updateStatus(id, 1);
        log.info("仪表盘启用成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disableDashboard(Long id) {
        dashboardMapper.updateStatus(id, 0);
        log.info("仪表盘禁用成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordAccess(Long id) {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        dashboardMapper.updateAccessInfo(id, now);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DashboardConfigDTO cloneDashboard(Long id, String newDashboardName, Long userId) {
        DashboardConfig source = dashboardMapper.selectById(id);
        if (source == null) {
            throw new BusinessException("原仪表盘不存在: " + id);
        }

        DashboardConfigCreateRequest request = new DashboardConfigCreateRequest();
        request.setDashboardName(newDashboardName);
        request.setDashboardType(source.getDashboardType());
        request.setTheme(source.getTheme());
        request.setRefreshInterval(source.getRefreshInterval());
        request.setDescription(source.getDescription());
        request.setIsDefault(false);
        request.setUserId(userId);
        request.setAccessLevel(source.getAccessLevel());

        // JSON解析
        try {
            if (source.getLayoutConfig() != null) {
                request.setLayoutConfig(objectMapper.readValue(source.getLayoutConfig(), new TypeReference<Map<String, Object>>() {}));
            }
            if (source.getWidgetConfig() != null) {
                request.setWidgetConfig(objectMapper.readValue(source.getWidgetConfig(), new TypeReference<List<Map<String, Object>>>() {}));
            }
        } catch (JsonProcessingException e) {
            throw new BusinessException("JSON解析失败: " + e.getMessage());
        }

        String creatorName = userId != null ? String.valueOf(userId) : "system";
        return createDashboard(request, userId, creatorName);
    }

    @Override
    public long countTotalDashboards() {
        return dashboardMapper.countTotal();
    }

    private String generateDashboardCode() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "DSH" + dateStr + random;
    }

    private DashboardConfigDTO convertToDTO(DashboardConfig dashboard) {
        DashboardConfigDTO dto = new DashboardConfigDTO();
        BeanUtils.copyProperties(dashboard, dto);

        // JSON解析
        try {
            if (StringUtils.isNotBlank(dashboard.getLayoutConfig())) {
                dto.setLayoutConfig(objectMapper.readValue(dashboard.getLayoutConfig(), new TypeReference<Map<String, Object>>() {}));
            }
            if (StringUtils.isNotBlank(dashboard.getWidgetConfig())) {
                dto.setWidgetConfig(objectMapper.readValue(dashboard.getWidgetConfig(), new TypeReference<List<Map<String, Object>>>() {}));
            }
        } catch (JsonProcessingException e) {
            log.error("JSON解析失败: {}", e.getMessage());
        }

        return dto;
    }
}
