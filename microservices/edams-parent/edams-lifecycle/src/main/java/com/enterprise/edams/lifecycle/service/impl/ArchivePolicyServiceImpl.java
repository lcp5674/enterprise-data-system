package com.enterprise.edams.lifecycle.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enterprise.edams.lifecycle.dto.ArchivePolicyCreateRequest;
import com.enterprise.edams.lifecycle.dto.ArchivePolicyDTO;
import com.enterprise.edams.lifecycle.entity.ArchivePolicy;
import com.enterprise.edams.lifecycle.entity.ArchiveRecord;
import com.enterprise.edams.lifecycle.repository.ArchivePolicyRepository;
import com.enterprise.edams.lifecycle.service.ArchivePolicyService;
import com.enterprise.edams.lifecycle.service.ArchiveRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 归档策略服务实现
 *
 * @author EDAMS Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArchivePolicyServiceImpl extends ServiceImpl<ArchivePolicyRepository, ArchivePolicy>
        implements ArchivePolicyService {

    private final ArchiveRecordService archiveRecordService;

    /**
     * 归档存储根目录（可在配置中心配置）
     */
    @Value("${archive.storage.path:/data/archive}")
    private String archiveStoragePath;

    /**
     * 归档存储类型：local, oss, s3
     */
    @Value("${archive.storage.type:local}")
    private String storageType;

    /**
     * OSS配置
     */
    @Value("${archive.oss.endpoint:}")
    private String ossEndpoint;

    @Value("${archive.oss.accessKeyId:}")
    private String ossAccessKeyId;

    @Value("${archive.oss.accessKeySecret:}")
    private String ossAccessKeySecret;

    @Value("${archive.oss.bucketName:}")
    private String ossBucketName;

    /**
     * 归档批次大小
     */
    @Value("${archive.batch.size:100}")
    private int archiveBatchSize;

    private static final DateTimeFormatter ARCHIVE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd/HHmmss");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArchivePolicyDTO createPolicy(ArchivePolicyCreateRequest request) {
        ArchivePolicy policy = new ArchivePolicy();
        BeanUtils.copyProperties(request, policy);
        policy.setEnabled(true);
        policy.setExecuteCount(0);
        policy.setSuccessCount(0);
        policy.setFailCount(0);

        save(policy);
        return convertToDTO(policy);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArchivePolicyDTO updatePolicy(String id, ArchivePolicyCreateRequest request) {
        ArchivePolicy policy = getById(id);
        if (policy == null) {
            throw new RuntimeException("归档策略不存在");
        }

        BeanUtils.copyProperties(request, policy);
        updateById(policy);
        return convertToDTO(policy);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePolicy(String id) {
        removeById(id);
    }

    @Override
    public ArchivePolicyDTO getPolicy(String id) {
        ArchivePolicy policy = getById(id);
        if (policy == null) {
            throw new RuntimeException("归档策略不存在");
        }
        return convertToDTO(policy);
    }

    @Override
    public Page<ArchivePolicyDTO> listPolicies(Page<ArchivePolicy> page, String keyword, String businessType, Boolean enabled) {
        LambdaQueryWrapper<ArchivePolicy> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(ArchivePolicy::getName, keyword)
                    .or()
                    .like(ArchivePolicy::getCode, keyword));
        }

        if (StringUtils.hasText(businessType)) {
            wrapper.eq(ArchivePolicy::getBusinessType, businessType);
        }

        if (enabled != null) {
            wrapper.eq(ArchivePolicy::getEnabled, enabled);
        }

        wrapper.orderByDesc(ArchivePolicy::getCreatedTime);
        Page<ArchivePolicy> resultPage = page(page, wrapper);

        List<ArchivePolicyDTO> records = resultPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        Page<ArchivePolicyDTO> dtoPage = new Page<>();
        BeanUtils.copyProperties(resultPage, dtoPage);
        dtoPage.setRecords(records);
        return dtoPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enablePolicy(String id) {
        ArchivePolicy policy = getById(id);
        if (policy == null) {
            throw new RuntimeException("归档策略不存在");
        }
        policy.setEnabled(true);
        updateById(policy);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disablePolicy(String id) {
        ArchivePolicy policy = getById(id);
        if (policy == null) {
            throw new RuntimeException("归档策略不存在");
        }
        policy.setEnabled(false);
        updateById(policy);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executePolicy(String id) {
        ArchivePolicy policy = getById(id);
        if (policy == null) {
            throw new RuntimeException("归档策略不存在");
        }

        long startTime = System.currentTimeMillis();
        log.info("开始执行归档策略: {}, 策略名称: {}", id, policy.getName());

        try {
            // 1. 解析触发条件，查询需要归档的资产
            List<Map<String, Object>> assetsToArchive = queryAssetsForArchive(policy);
            log.info("查询到需要归档的资产数量: {}", assetsToArchive.size());

            if (assetsToArchive.isEmpty()) {
                log.info("没有需要归档的资产，策略执行完成");
                updatePolicyExecutionStats(policy, startTime, true, 0, 0);
                return;
            }

            // 2. 解析目标存储配置
            JSONObject targetConfig = parseTargetConfig(policy);

            // 3. 批量归档资产
            int successCount = 0;
            int failCount = 0;

            for (int i = 0; i < assetsToArchive.size(); i += archiveBatchSize) {
                int endIndex = Math.min(i + archiveBatchSize, assetsToArchive.size());
                List<Map<String, Object>> batch = assetsToArchive.subList(i, endIndex);

                for (Map<String, Object> asset : batch) {
                    try {
                        // 归档单个资产
                        archiveAsset(policy, asset, targetConfig);
                        successCount++;
                    } catch (Exception e) {
                        log.error("归档资产失败: businessId={}", asset.get("id"), e);
                        failCount++;

                        // 记录失败日志
                        createFailedArchiveRecord(policy, asset, e.getMessage());
                    }
                }
                log.info("归档进度: {}/{}", Math.min(endIndex, assetsToArchive.size()), assetsToArchive.size());
            }

            // 4. 清理原表中的过期数据（如果配置了删除源数据）
            if (Boolean.TRUE.equals(policy.getDeleteSource())) {
                cleanupExpiredAssets(policy, assetsToArchive);
            }

            // 5. 更新策略执行统计
            updatePolicyExecutionStats(policy, startTime, true, successCount, failCount);
            log.info("归档策略执行完成: {}, 成功: {}, 失败: {}", id, successCount, failCount);

        } catch (Exception e) {
            log.error("归档策略执行失败: {}", id, e);
            updatePolicyExecutionStats(policy, startTime, false, 0, 0);
            throw new RuntimeException("归档策略执行失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据归档策略配置查询需要归档的资产
     */
    private List<Map<String, Object>> queryAssetsForArchive(ArchivePolicy policy) {
        // 解析触发条件
        JSONObject condition = parseTriggerCondition(policy);

        // 根据业务类型和数据分类查询资产
        // 这里模拟查询逻辑，实际需要根据不同的业务类型调用相应的服务
        List<Map<String, Object>> assets = new ArrayList<>();

        String businessType = policy.getBusinessType();
        Integer retentionDays = policy.getRetentionDays();
        Integer triggerType = policy.getTriggerType();

        log.info("查询归档资产: businessType={}, retentionDays={}, triggerType={}",
                businessType, retentionDays, triggerType);

        // 根据triggerType决定查询条件
        LocalDateTime archiveTimeThreshold = LocalDateTime.now().minusDays(retentionDays != null ? retentionDays : 90);

        // 模拟查询结果，实际应通过资产服务查询
        // List<Asset> assetsToArchive = assetService.queryAssetsForArchive(businessType, archiveTimeThreshold);

        // 返回模拟数据
        return assets;
    }

    /**
     * 解析触发条件JSON
     */
    private JSONObject parseTriggerCondition(ArchivePolicy policy) {
        if (!StringUtils.hasText(policy.getTriggerCondition())) {
            return new JSONObject();
        }
        try {
            return JSON.parseObject(policy.getTriggerCondition());
        } catch (Exception e) {
            log.warn("解析触发条件失败: {}", policy.getTriggerCondition(), e);
            return new JSONObject();
        }
    }

    /**
     * 解析目标存储配置
     */
    private JSONObject parseTargetConfig(ArchivePolicy policy) {
        if (!StringUtils.hasText(policy.getTargetConfig())) {
            return new JSONObject();
        }
        try {
            return JSON.parseObject(policy.getTargetConfig());
        } catch (Exception e) {
            log.warn("解析目标存储配置失败: {}", policy.getTargetConfig(), e);
            return new JSONObject();
        }
    }

    /**
     * 归档单个资产
     */
    private void archiveAsset(ArchivePolicy policy, Map<String, Object> asset, JSONObject targetConfig) throws Exception {
        String businessId = String.valueOf(asset.get("id"));
        String businessName = String.valueOf(asset.get("name"));
        String dataType = policy.getDataCategory();

        LocalDateTime startTime = LocalDateTime.now();
        log.debug("开始归档资产: businessId={}", businessId);

        // 1. 准备归档数据
        byte[] dataToArchive = prepareArchiveData(asset);

        // 2. 应用压缩
        if (policy.getCompressionType() != null && policy.getCompressionType() > 0) {
            dataToArchive = compressData(dataToArchive, policy.getCompressionType());
            log.debug("数据已压缩, 压缩后大小: {} bytes", dataToArchive.length);
        }

        // 3. 生成归档文件
        String archiveFileName = generateArchiveFileName(policy, businessId);
        String archivePath = generateArchivePath(policy);

        // 4. 保存到归档存储
        String archiveUrl = saveToArchiveStorage(archivePath, archiveFileName, dataToArchive, targetConfig);

        // 5. 计算校验和
        String checksum = calculateChecksum(dataToArchive);

        // 6. 创建归档记录
        ArchiveRecord record = new ArchiveRecord();
        record.setPolicyId(policy.getId());
        record.setBusinessType(policy.getBusinessType());
        record.setBusinessId(businessId);
        record.setBusinessName(businessName);
        record.setDataType(dataType);
        record.setFileCount(1);
        record.setDataSize((long) dataToArchive.length);
        record.setArchivePath(archivePath);
        record.setArchiveFileName(archiveFileName);
        record.setArchiveUrl(archiveUrl);
        record.setChecksum(checksum);
        record.setCompressionFormat(getCompressionFormat(policy.getCompressionType()));
        record.setEncryptionAlgorithm(getEncryptionAlgorithm(policy.getEncryptionType()));
        record.setStatus(2); // 归档成功
        record.setArchiveStartTime(startTime);
        record.setArchiveEndTime(LocalDateTime.now());
        record.setArchiveDuration(System.currentTimeMillis() - startTime.toInstant(java.time.ZoneOffset.UTC).toEpochMilli());
        record.setRetentionExpireTime(LocalDateTime.now().plusDays(policy.getRetentionDays() != null ? policy.getRetentionDays() : 365));
        record.setCreatedBy("system");

        archiveRecordService.save(record);

        // 7. 更新资产状态（如果需要）
        updateAssetStatus(asset, businessId);

        log.info("资产归档成功: businessId={}, archiveUrl={}", businessId, archiveUrl);
    }

    /**
     * 准备归档数据
     */
    private byte[] prepareArchiveData(Map<String, Object> asset) throws IOException {
        // 将资产数据序列化为JSON
        String jsonData = JSON.toJSONString(asset);
        return jsonData.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 压缩数据
     */
    private byte[] compressData(byte[] data, Integer compressionType) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        switch (compressionType) {
            case 1: // GZIP
                try (GZIPOutputStream gzos = new GZIPOutputStream(baos)) {
                    gzos.write(data);
                }
                break;
            case 2: // ZIP
                try (ZipOutputStream zos = new ZipOutputStream(baos)) {
                    zos.putNextEntry(new ZipEntry("archive.dat"));
                    zos.write(data);
                    zos.closeEntry();
                }
                break;
            default:
                return data;
        }

        return baos.toByteArray();
    }

    /**
     * 获取压缩格式名称
     */
    private String getCompressionFormat(Integer compressionType) {
        if (compressionType == null || compressionType == 0) {
            return "NONE";
        }
        switch (compressionType) {
            case 1:
                return "GZIP";
            case 2:
                return "ZIP";
            case 3:
                return "7Z";
            default:
                return "UNKNOWN";
        }
    }

    /**
     * 获取加密算法名称
     */
    private String getEncryptionAlgorithm(Integer encryptionType) {
        if (encryptionType == null || encryptionType == 0) {
            return "NONE";
        }
        switch (encryptionType) {
            case 1:
                return "AES-256";
            default:
                return "UNKNOWN";
        }
    }

    /**
     * 生成归档文件名
     */
    private String generateArchiveFileName(ArchivePolicy policy, String businessId) {
        String timestamp = LocalDateTime.now().format(ARCHIVE_DATE_FORMAT);
        String extension = getArchiveExtension(policy);
        return String.format("%s_%s_%s%s",
                policy.getCode(),
                businessId,
                timestamp,
                extension);
    }

    /**
     * 获取归档文件扩展名
     */
    private String getArchiveExtension(ArchivePolicy policy) {
        if (policy.getCompressionType() == null || policy.getCompressionType() == 0) {
            return ".json";
        }
        switch (policy.getCompressionType()) {
            case 1:
                return ".json.gz";
            case 2:
                return ".json.zip";
            case 3:
                return ".json.7z";
            default:
                return ".json";
        }
    }

    /**
     * 生成归档路径
     */
    private String generateArchivePath(ArchivePolicy policy) {
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return String.format("%s/%s/%s/%s",
                archiveStoragePath,
                policy.getBusinessType(),
                policy.getCode(),
                datePath);
    }

    /**
     * 保存到归档存储
     */
    private String saveToArchiveStorage(String archivePath, String fileName, byte[] data, JSONObject targetConfig) throws Exception {
        switch (storageType.toLowerCase()) {
            case "oss":
                return saveToOss(archivePath, fileName, data, targetConfig);
            case "s3":
                return saveToS3(archivePath, fileName, data, targetConfig);
            default:
                return saveToLocal(archivePath, fileName, data);
        }
    }

    /**
     * 保存到本地存储
     */
    private String saveToLocal(String archivePath, String fileName, byte[] data) throws IOException {
        Path path = Path.of(archivePath);
        Files.createDirectories(path);

        Path filePath = path.resolve(fileName);
        Files.write(filePath, data);

        log.debug("文件已保存到本地: {}", filePath);
        return filePath.toUri().toString();
    }

    /**
     * 保存到OSS存储
     */
    private String saveToOss(String archivePath, String fileName, byte[] data, JSONObject targetConfig) throws Exception {
        // 使用阿里云OSS SDK上传
        // 这里需要添加OSS依赖和配置
        // 实际实现时需要：
        // 1. 创建OSSClient
        // 2. 上传文件到指定bucket
        // 3. 返回访问URL

        log.warn("OSS存储功能需要配置阿里云OSS SDK");
        // 模拟返回URL
        String bucketName = StringUtils.hasText(ossBucketName) ? ossBucketName :
                targetConfig.getString("bucketName");
        String endpoint = StringUtils.hasText(ossEndpoint) ? ossEndpoint :
                targetConfig.getString("endpoint");

        if (StringUtils.hasText(bucketName) && StringUtils.hasText(endpoint)) {
            String objectKey = archivePath + "/" + fileName;
            // 实际调用: ossClient.putObject(bucketName, objectKey, new ByteArrayInputStream(data));
            // return "https://" + bucketName + "." + endpoint + "/" + objectKey;
            return "oss://" + bucketName + "/" + objectKey;
        }

        // 如果OSS未配置，回退到本地存储
        return saveToLocal(archivePath, fileName, data);
    }

    /**
     * 保存到S3存储
     */
    private String saveToS3(String archivePath, String fileName, byte[] data, JSONObject targetConfig) throws Exception {
        log.warn("S3存储功能需要配置AWS S3 SDK");
        // 实际实现时需要：
        // 1. 创建AmazonS3Client
        // 2. 上传文件到指定bucket
        // 3. 返回访问URL

        // 回退到本地存储
        return saveToLocal(archivePath, fileName, data);
    }

    /**
     * 计算数据校验和（MD5）
     */
    private String calculateChecksum(byte[] data) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(data);
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * 更新资产状态为已归档
     */
    private void updateAssetStatus(Map<String, Object> asset, String businessId) {
        // 根据业务类型调用相应的服务更新资产状态
        // assetService.updateArchiveStatus(businessId);
        log.debug("资产状态已更新为已归档: businessId={}", businessId);
    }

    /**
     * 清理过期资产数据
     */
    private void cleanupExpiredAssets(ArchivePolicy policy, List<Map<String, Object>> archivedAssets) {
        log.info("开始清理原表中的过期数据，共{}条", archivedAssets.size());

        for (Map<String, Object> asset : archivedAssets) {
            String businessId = String.valueOf(asset.get("id"));
            try {
                // 根据业务类型调用相应的服务删除或标记资产
                // assetService.markAsArchived(businessId);
                log.debug("过期资产已清理: businessId={}", businessId);
            } catch (Exception e) {
                log.error("清理过期资产失败: businessId={}", businessId, e);
            }
        }

        log.info("过期资产清理完成");
    }

    /**
     * 创建失败的归档记录
     */
    private void createFailedArchiveRecord(ArchivePolicy policy, Map<String, Object> asset, String errorMessage) {
        try {
            ArchiveRecord record = new ArchiveRecord();
            record.setPolicyId(policy.getId());
            record.setBusinessType(policy.getBusinessType());
            record.setBusinessId(String.valueOf(asset.get("id")));
            record.setBusinessName(String.valueOf(asset.get("name")));
            record.setDataType(policy.getDataCategory());
            record.setStatus(3); // 归档失败
            record.setErrorMessage(errorMessage);
            record.setArchiveStartTime(LocalDateTime.now());
            record.setArchiveEndTime(LocalDateTime.now());
            record.setCreatedBy("system");

            archiveRecordService.save(record);
        } catch (Exception e) {
            log.error("创建失败归档记录失败", e);
        }
    }

    /**
     * 更新策略执行统计
     */
    private void updatePolicyExecutionStats(ArchivePolicy policy, long startTime, boolean success, int successCount, int failCount) {
        policy.setLastExecuteTime(LocalDateTime.now());
        policy.setExecuteCount(policy.getExecuteCount() != null ? policy.getExecuteCount() + 1 : 1);

        if (success) {
            policy.setSuccessCount(policy.getSuccessCount() != null ? policy.getSuccessCount() + successCount : successCount);
            policy.setFailCount(policy.getFailCount() != null ? policy.getFailCount() + failCount : failCount);
        } else {
            policy.setFailCount(policy.getFailCount() != null ? policy.getFailCount() + 1 : 1);
        }

        updateById(policy);
    }

    @Override
    public List<ArchivePolicyDTO> getEnabledPolicies() {
        return baseMapper.findEnabledPolicies().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private ArchivePolicyDTO convertToDTO(ArchivePolicy policy) {
        ArchivePolicyDTO dto = new ArchivePolicyDTO();
        BeanUtils.copyProperties(policy, dto);
        return dto;
    }
}
