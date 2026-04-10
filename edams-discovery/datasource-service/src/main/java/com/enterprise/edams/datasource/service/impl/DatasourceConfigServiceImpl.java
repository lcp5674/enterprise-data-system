package com.enterprise.edams.datasource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.datasource.constant.DatasourceStatus;
import com.enterprise.edams.datasource.constant.DatasourceType;
import com.enterprise.edams.datasource.constant.HealthStatus;
import com.enterprise.edams.datasource.connector.DatasourceConnector;
import com.enterprise.edams.datasource.connector.DatasourceConnectorFactory;
import com.enterprise.edams.datasource.dto.*;
import com.enterprise.edams.datasource.entity.DatasourceConfig;
import com.enterprise.edams.datasource.exception.DatasourceException;
import com.enterprise.edams.datasource.repository.DatasourceConfigRepository;
import com.enterprise.edams.datasource.service.DatasourceConfigService;
import com.enterprise.edams.datasource.vo.DatasourceDetailVO;
import com.enterprise.edams.datasource.vo.DatasourceStatisticsVO;
import com.enterprise.edams.datasource.vo.DatasourceVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据源配置服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DatasourceConfigServiceImpl implements DatasourceConfigService {

    private final DatasourceConfigRepository datasourceConfigRepository;
    private final DatasourceConnectorFactory connectorFactory;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public Long createDatasource(CreateDatasourceRequest request) {
        // 验证编码唯一性
        if (!isCodeUnique(request.getCode())) {
            throw new DatasourceException("数据源编码已存在: " + request.getCode());
        }

        DatasourceConfig config = new DatasourceConfig();
        BeanUtils.copyProperties(request, config);
        
        // 加密密码
        config.setPasswordEnc(encryptPassword(request.getPassword()));
        
        // 转换JSON字段
        config.setProperties(toJson(request.getProperties()));
        config.setJdbcParams(toJson(request.getJdbcParams()));
        config.setHttpHeaders(toJson(request.getHttpHeaders()));
        
        // 设置默认值
        config.setStatus(DatasourceStatus.INACTIVE.name());
        config.setHealthStatus(HealthStatus.UNKNOWN.name());
        config.setSyncEnabled(request.getSyncEnabled() != null ? (request.getSyncEnabled() ? 1 : 0) : 0);
        
        // 设置创建人（实际应从SecurityContext获取）
        config.setCreatedBy("system");
        
        datasourceConfigRepository.insert(config);
        
        log.info("创建数据源配置成功: id={}, code={}", config.getId(), config.getCode());
        return config.getId();
    }

    @Override
    @Transactional
    public boolean updateDatasource(Long id, UpdateDatasourceRequest request) {
        DatasourceConfig config = datasourceConfigRepository.selectById(id);
        if (config == null) {
            throw new DatasourceException("数据源不存在: " + id);
        }

        // 更新非空字段
        if (request.getName() != null) {
            config.setName(request.getName());
        }
        if (request.getDatasourceType() != null) {
            config.setDatasourceType(request.getDatasourceType());
        }
        if (request.getHost() != null) {
            config.setHost(request.getHost());
        }
        if (request.getPort() != null) {
            config.setPort(request.getPort());
        }
        if (request.getDatabaseName() != null) {
            config.setDatabaseName(request.getDatabaseName());
        }
        if (request.getUsername() != null) {
            config.setUsername(request.getUsername());
        }
        if (request.getPassword() != null) {
            config.setPasswordEnc(encryptPassword(request.getPassword()));
        }
        if (request.getConnectionUrl() != null) {
            config.setConnectionUrl(request.getConnectionUrl());
        }
        if (request.getProperties() != null) {
            config.setProperties(toJson(request.getProperties()));
        }
        if (request.getJdbcParams() != null) {
            config.setJdbcParams(toJson(request.getJdbcParams()));
        }
        if (request.getHttpHeaders() != null) {
            config.setHttpHeaders(toJson(request.getHttpHeaders()));
        }
        if (request.getAuthType() != null) {
            config.setAuthType(request.getAuthType());
        }
        if (request.getSyncInterval() != null) {
            config.setSyncInterval(request.getSyncInterval());
        }
        if (request.getSyncEnabled() != null) {
            config.setSyncEnabled(request.getSyncEnabled() ? 1 : 0);
        }
        if (request.getCatalogName() != null) {
            config.setCatalogName(request.getCatalogName());
        }
        if (request.getDescription() != null) {
            config.setDescription(request.getDescription());
        }
        if (request.getTags() != null) {
            config.setTags(request.getTags());
        }
        
        config.setUpdatedBy("system");
        
        datasourceConfigRepository.updateById(config);
        
        log.info("更新数据源配置成功: id={}", id);
        return true;
    }

    @Override
    @Transactional
    public boolean deleteDatasource(Long id) {
        DatasourceConfig config = datasourceConfigRepository.selectById(id);
        if (config == null) {
            throw new DatasourceException("数据源不存在: " + id);
        }
        
        // 逻辑删除
        config.setDeleted(1);
        datasourceConfigRepository.updateById(config);
        
        log.info("删除数据源配置成功: id={}", id);
        return true;
    }

    @Override
    public DatasourceDetailVO getDatasourceDetail(Long id) {
        DatasourceConfig config = datasourceConfigRepository.selectById(id);
        if (config == null) {
            throw new DatasourceException("数据源不存在: " + id);
        }
        
        return convertToDetailVO(config);
    }

    @Override
    public IPage<DatasourceVO> listDatasources(DatasourceQueryDTO query) {
        Page<DatasourceConfig> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<DatasourceConfig> configPage = datasourceConfigRepository.selectPageList(page, query);
        
        return configPage.convert(this::convertToVO);
    }

    @Override
    @Transactional
    public boolean enableDatasource(Long id) {
        DatasourceConfig config = datasourceConfigRepository.selectById(id);
        if (config == null) {
            throw new DatasourceException("数据源不存在: " + id);
        }
        
        config.setStatus(DatasourceStatus.ACTIVE.name());
        config.setUpdatedBy("system");
        datasourceConfigRepository.updateById(config);
        
        log.info("启用数据源成功: id={}", id);
        return true;
    }

    @Override
    @Transactional
    public boolean disableDatasource(Long id) {
        DatasourceConfig config = datasourceConfigRepository.selectById(id);
        if (config == null) {
            throw new DatasourceException("数据源不存在: " + id);
        }
        
        config.setStatus(DatasourceStatus.INACTIVE.name());
        config.setUpdatedBy("system");
        datasourceConfigRepository.updateById(config);
        
        log.info("禁用数据源成功: id={}", id);
        return true;
    }

    @Override
    public ConnectionTestResponse testConnection(ConnectionTestRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            DatasourceConnector connector = connectorFactory.getConnector(request.getDatasourceType());
            boolean success = connector.testConnection(request);
            
            long responseTime = System.currentTimeMillis() - startTime;
            
            return ConnectionTestResponse.builder()
                    .success(success)
                    .message(success ? "连接成功" : "连接失败")
                    .responseTime(responseTime)
                    .build();
        } catch (Exception e) {
            log.error("连接测试失败", e);
            
            long responseTime = System.currentTimeMillis() - startTime;
            
            return ConnectionTestResponse.builder()
                    .success(false)
                    .message("连接失败: " + e.getMessage())
                    .responseTime(responseTime)
                    .errorDetail(e.getMessage())
                    .build();
        }
    }

    @Override
    public ConnectionTestResponse testDatasourceConnection(Long id) {
        DatasourceConfig config = datasourceConfigRepository.selectById(id);
        if (config == null) {
            throw new DatasourceException("数据源不存在: " + id);
        }
        
        ConnectionTestRequest request = new ConnectionTestRequest();
        request.setDatasourceType(config.getDatasourceType());
        request.setHost(config.getHost());
        request.setPort(config.getPort());
        request.setDatabaseName(config.getDatabaseName());
        request.setUsername(config.getUsername());
        request.setPassword(decryptPassword(config.getPasswordEnc()));
        request.setConnectionUrl(config.getConnectionUrl());
        request.setProperties(fromJson(config.getProperties(), Map.class));
        request.setJdbcParams(fromJson(config.getJdbcParams(), Map.class));
        request.setHttpHeaders(fromJson(config.getHttpHeaders(), Map.class));
        request.setAuthType(config.getAuthType());
        
        ConnectionTestResponse response = testConnection(request);
        
        // 更新健康状态
        config.setHealthStatus(response.getSuccess() ? HealthStatus.HEALTHY.name() : HealthStatus.UNHEALTHY.name());
        config.setLastTestTime(LocalDateTime.now());
        config.setLastTestResult(toJson(response));
        config.setUpdatedBy("system");
        datasourceConfigRepository.updateById(config);
        
        return response;
    }

    @Override
    public DatasourceStatisticsVO getStatistics() {
        Map<String, Long> byType = new HashMap<>();
        for (DatasourceType type : DatasourceType.values()) {
            byType.put(type.name(), datasourceConfigRepository.countByType(type.name()));
        }
        
        Map<String, Long> byStatus = new HashMap<>();
        for (DatasourceStatus status : DatasourceStatus.values()) {
            byStatus.put(status.name(), datasourceConfigRepository.countByStatus(status.name()));
        }
        
        Map<String, Long> byHealthStatus = new HashMap<>();
        for (HealthStatus healthStatus : HealthStatus.values()) {
            byHealthStatus.put(healthStatus.name(), datasourceConfigRepository.countByHealthStatus(healthStatus.name()));
        }
        
        Long totalCount = datasourceConfigRepository.selectCount(null);
        Long syncedCount = datasourceConfigRepository.selectCount(
                new LambdaQueryWrapper<DatasourceConfig>()
                        .eq(DatasourceConfig::getSyncEnabled, 1)
                        .isNotNull(DatasourceConfig::getLastSyncTime)
        );
        
        return DatasourceStatisticsVO.builder()
                .totalCount(totalCount)
                .byType(byType)
                .byStatus(byStatus)
                .byHealthStatus(byHealthStatus)
                .syncedCount(syncedCount)
                .unsyncedCount(totalCount - syncedCount)
                .build();
    }

    @Override
    public DatasourceConfig getByCode(String code) {
        return datasourceConfigRepository.selectByCode(code);
    }

    @Override
    public List<DatasourceConfig> listByCodes(List<String> codes) {
        return datasourceConfigRepository.selectByCodes(codes);
    }

    @Override
    public boolean isCodeUnique(String code) {
        DatasourceConfig existing = datasourceConfigRepository.selectByCode(code);
        return existing == null;
    }

    /**
     * 加密密码
     */
    private String encryptPassword(String password) {
        if (password == null) {
            return null;
        }
        // TODO: 实现实际的加密逻辑，如使用AES或SM4
        return password; // 临时实现
    }

    /**
     * 解密密码
     */
    private String decryptPassword(String encryptedPassword) {
        if (encryptedPassword == null) {
            return null;
        }
        // TODO: 实现实际的解密逻辑
        return encryptedPassword; // 临时实现
    }

    /**
     * 转换为JSON字符串
     */
    private String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("JSON序列化失败", e);
            return null;
        }
    }

    /**
     * 从JSON字符串解析对象
     */
    private <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("JSON反序列化失败", e);
            return null;
        }
    }

    /**
     * 转换为VO
     */
    private DatasourceVO convertToVO(DatasourceConfig config) {
        DatasourceVO vo = new DatasourceVO();
        BeanUtils.copyProperties(config, vo);
        vo.setDatasourceTypeDesc(getTypeDesc(config.getDatasourceType()));
        vo.setStatusDesc(getStatusDesc(config.getStatus()));
        vo.setHealthStatusDesc(getHealthStatusDesc(config.getHealthStatus()));
        vo.setSyncEnabled(config.getSyncEnabled() != null && config.getSyncEnabled() == 1);
        return vo;
    }

    /**
     * 转换为详情VO
     */
    private DatasourceDetailVO convertToDetailVO(DatasourceConfig config) {
        DatasourceDetailVO vo = new DatasourceDetailVO();
        BeanUtils.copyProperties(config, vo);
        vo.setDatasourceTypeDesc(getTypeDesc(config.getDatasourceType()));
        vo.setStatusDesc(getStatusDesc(config.getStatus()));
        vo.setHealthStatusDesc(getHealthStatusDesc(config.getHealthStatus()));
        vo.setAuthTypeDesc(getAuthTypeDesc(config.getAuthType()));
        vo.setSyncEnabled(config.getSyncEnabled() != null && config.getSyncEnabled() == 1);
        vo.setProperties(fromJson(config.getProperties(), Map.class));
        vo.setJdbcParams(fromJson(config.getJdbcParams(), Map.class));
        vo.setHttpHeaders(fromJson(config.getHttpHeaders(), Map.class));
        return vo;
    }

    private String getTypeDesc(String type) {
        if (type == null) return "";
        try {
            return DatasourceType.valueOf(type).getDescription();
        } catch (IllegalArgumentException e) {
            return type;
        }
    }

    private String getStatusDesc(String status) {
        if (status == null) return "";
        try {
            return DatasourceStatus.valueOf(status).getDescription();
        } catch (IllegalArgumentException e) {
            return status;
        }
    }

    private String getHealthStatusDesc(String healthStatus) {
        if (healthStatus == null) return "";
        try {
            return HealthStatus.valueOf(healthStatus).getDescription();
        } catch (IllegalArgumentException e) {
            return healthStatus;
        }
    }

    private String getAuthTypeDesc(String authType) {
        // 认证类型描述
        return authType;
    }
}
