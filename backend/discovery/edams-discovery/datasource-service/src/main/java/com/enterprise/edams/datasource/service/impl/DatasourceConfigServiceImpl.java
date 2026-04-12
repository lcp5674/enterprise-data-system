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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
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

    /**
     * AES-256-GCM加密密钥（必须通过环境变量或K8s Secret配置）
     * P0安全修复：移除硬编码默认密钥，要求外部注入
     */
    @Value("${datasource.encryption.key}")
    private String encryptionKey;

    /**
     * AES-256-GCM IV长度（12字节）
     */
    private static final int GCM_IV_LENGTH = 12;

    /**
     * AES-256-GCM TAG长度（128位）
     */
    private static final int GCM_TAG_LENGTH = 128;

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
     * 加密密码 - 使用AES-256-GCM算法
     * AES-256-GCM提供加密和认证双重保护，比普通AES更安全
     *
     * @param password 明文密码
     * @return Base64编码的加密密码（包含IV）
     */
    private String encryptPassword(String password) {
        if (password == null) {
            return null;
        }
        try {
            // 生成随机IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(iv);

            // 创建GCM参数规格
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

            // 创建AES密钥
            SecretKeySpec keySpec = new SecretKeySpec(
                    padKeyTo32Bytes(encryptionKey).getBytes(StandardCharsets.UTF_8), "AES");

            // 初始化加密器
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);

            // 执行加密
            byte[] encryptedBytes = cipher.doFinal(password.getBytes(StandardCharsets.UTF_8));

            // 合并IV和加密数据
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + encryptedBytes.length);
            byteBuffer.put(iv);
            byteBuffer.put(encryptedBytes);

            // 返回Base64编码
            String encrypted = Base64.getEncoder().encodeToString(byteBuffer.array());
            log.debug("密码加密成功，使用AES-256-GCM");
            return encrypted;
        } catch (Exception e) {
            log.error("密码加密失败", e);
            throw new DatasourceException("密码加密失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解密密码 - 使用AES-256-GCM算法
     *
     * @param encryptedPassword Base64编码的加密密码（包含IV）
     * @return 解密后的明文密码
     */
    private String decryptPassword(String encryptedPassword) {
        if (encryptedPassword == null) {
            return null;
        }
        try {
            // Base64解码
            byte[] decoded = Base64.getDecoder().decode(encryptedPassword);

            // 分离IV和加密数据
            ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);
            byte[] encryptedBytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(encryptedBytes);

            // 创建GCM参数规格
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

            // 创建AES密钥
            SecretKeySpec keySpec = new SecretKeySpec(
                    padKeyTo32Bytes(encryptionKey).getBytes(StandardCharsets.UTF_8), "AES");

            // 初始化解密器
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);

            // 执行解密
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            String decrypted = new String(decryptedBytes, StandardCharsets.UTF_8);

            log.debug("密码解密成功");
            return decrypted;
        } catch (Exception e) {
            log.error("密码解密失败", e);
            throw new DatasourceException("密码解密失败: " + e.getMessage(), e);
        }
    }

    /**
     * 将密钥填充或截断到32字节（256位）
     */
    private String padKeyTo32Bytes(String key) {
        if (key == null) {
            throw new DatasourceException("加密密钥不能为空");
        }
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] paddedKey = new byte[32];
        System.arraycopy(keyBytes, 0, paddedKey, 0, Math.min(keyBytes.length, 32));
        return new String(paddedKey, StandardCharsets.UTF_8);
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
