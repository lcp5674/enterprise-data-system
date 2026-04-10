package com.enterprise.edams.version.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enterprise.edams.version.entity.VersionRecord;
import com.enterprise.edams.version.repository.VersionRecordRepository;
import com.enterprise.edams.version.service.VersionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 版本服务实现
 *
 * @author EDAMS Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VersionServiceImpl extends ServiceImpl<VersionRecordRepository, VersionRecord>
        implements VersionService {

    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VersionRecord createVersion(String businessType, String businessId, Map<String, Object> dataContent,
                                       String versionTag, String versionComment, String userId, String userName) {
        // 获取当前最大版本号
        Integer maxVersion = baseMapper.findMaxVersionByBusiness(businessType, businessId);
        int newVersion = maxVersion != null ? maxVersion + 1 : 1;

        VersionRecord record = new VersionRecord();
        record.setBusinessType(businessType);
        record.setBusinessId(businessId);
        record.setVersion(newVersion);
        record.setVersionTag(versionTag);
        record.setVersionComment(versionComment);
        record.setCreatedBy(userId);
        record.setCreatedByName(userName);

        try {
            String contentJson = objectMapper.writeValueAsString(dataContent);
            record.setDataContent(contentJson);
            record.setDataDigest(calculateDigest(contentJson));

            // 计算变更内容
            if (newVersion > 1) {
                VersionRecord previousVersion = baseMapper.findByBusinessAndVersion(businessType, businessId, newVersion - 1);
                if (previousVersion != null) {
                    Map<String, Object> diff = calculateDiff(
                            objectMapper.readValue(previousVersion.getDataContent(), Map.class),
                            dataContent
                    );
                    record.setChangeDetail(objectMapper.writeValueAsString(diff));
                    record.setChangeSummary(generateChangeSummary(diff));
                    record.setChangeType(2); // 更新
                }
            } else {
                record.setChangeType(1); // 创建
                record.setChangeSummary("初始版本");
            }
        } catch (Exception e) {
            log.error("创建版本失败", e);
            throw new RuntimeException("创建版本失败: " + e.getMessage());
        }

        save(record);
        log.info("版本创建成功: {}-{} v{}", businessType, businessId, newVersion);
        return record;
    }

    @Override
    public VersionRecord getVersion(String id) {
        VersionRecord record = getById(id);
        if (record == null) {
            throw new RuntimeException("版本不存在");
        }
        return record;
    }

    @Override
    public List<VersionRecord> getVersionsByBusiness(String businessType, String businessId) {
        return baseMapper.findByBusiness(businessType, businessId);
    }

    @Override
    public VersionRecord getLatestVersion(String businessType, String businessId) {
        return baseMapper.findLatestByBusiness(businessType, businessId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> rollbackVersion(String versionId, String userId, String userName) {
        VersionRecord targetVersion = getById(versionId);
        if (targetVersion == null) {
            throw new RuntimeException("版本不存在");
        }

        try {
            // 解析目标版本数据
            Map<String, Object> rollbackData = objectMapper.readValue(targetVersion.getDataContent(), Map.class);

            // 创建新版本记录回滚操作
            VersionRecord rollbackRecord = new VersionRecord();
            rollbackRecord.setBusinessType(targetVersion.getBusinessType());
            rollbackRecord.setBusinessId(targetVersion.getBusinessId());

            Integer maxVersion = baseMapper.findMaxVersionByBusiness(targetVersion.getBusinessType(), targetVersion.getBusinessId());
            rollbackRecord.setVersion(maxVersion != null ? maxVersion + 1 : 1);
            rollbackRecord.setVersionTag("rollback-" + targetVersion.getVersion());
            rollbackRecord.setVersionComment("回滚到版本 " + targetVersion.getVersion());
            rollbackRecord.setDataContent(targetVersion.getDataContent());
            rollbackRecord.setDataDigest(targetVersion.getDataDigest());
            rollbackRecord.setChangeType(2);
            rollbackRecord.setChangeSummary("回滚到版本 " + targetVersion.getVersion());
            rollbackRecord.setCreatedBy(userId);
            rollbackRecord.setCreatedByName(userName);

            save(rollbackRecord);

            Map<String, Object> result = new HashMap<>();
            result.put("rollbackVersion", rollbackRecord.getVersion());
            result.put("targetVersion", targetVersion.getVersion());
            result.put("data", rollbackData);

            log.info("版本回滚成功: {}-{} 回滚到 v{}",
                    targetVersion.getBusinessType(), targetVersion.getBusinessId(), targetVersion.getVersion());

            return result;
        } catch (Exception e) {
            log.error("版本回滚失败", e);
            throw new RuntimeException("版本回滚失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> compareVersions(String versionId1, String versionId2) {
        VersionRecord v1 = getById(versionId1);
        VersionRecord v2 = getById(versionId2);

        if (v1 == null || v2 == null) {
            throw new RuntimeException("版本不存在");
        }

        try {
            Map<String, Object> data1 = objectMapper.readValue(v1.getDataContent(), Map.class);
            Map<String, Object> data2 = objectMapper.readValue(v2.getDataContent(), Map.class);

            Map<String, Object> diff = calculateDiff(data1, data2);

            Map<String, Object> result = new HashMap<>();
            result.put("version1", Map.of(
                    "id", v1.getId(),
                    "version", v1.getVersion(),
                    "createdTime", v1.getCreatedTime(),
                    "createdBy", v1.getCreatedByName()
            ));
            result.put("version2", Map.of(
                    "id", v2.getId(),
                    "version", v2.getVersion(),
                    "createdTime", v2.getCreatedTime(),
                    "createdBy", v2.getCreatedByName()
            ));
            result.put("diff", diff);

            return result;
        } catch (Exception e) {
            log.error("版本对比失败", e);
            throw new RuntimeException("版本对比失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteVersion(String id) {
        removeById(id);
        log.info("版本删除成功: {}", id);
    }

    private String calculateDigest(String content) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(content.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("计算摘要失败", e);
            return "";
        }
    }

    private Map<String, Object> calculateDiff(Map<String, Object> oldData, Map<String, Object> newData) {
        Map<String, Object> diff = new HashMap<>();
        Set<String> allKeys = new HashSet<>();
        allKeys.addAll(oldData.keySet());
        allKeys.addAll(newData.keySet());

        for (String key : allKeys) {
            Object oldValue = oldData.get(key);
            Object newValue = newData.get(key);

            if (oldValue == null && newValue != null) {
                diff.put(key, Map.of("type", "ADDED", "newValue", newValue));
            } else if (oldValue != null && newValue == null) {
                diff.put(key, Map.of("type", "REMOVED", "oldValue", oldValue));
            } else if (oldValue != null && !oldValue.equals(newValue)) {
                diff.put(key, Map.of("type", "MODIFIED", "oldValue", oldValue, "newValue", newValue));
            }
        }

        return diff;
    }

    private String generateChangeSummary(Map<String, Object> diff) {
        int added = 0, removed = 0, modified = 0;
        for (Map.Entry<String, Object> entry : diff.entrySet()) {
            Map<String, Object> change = (Map<String, Object>) entry.getValue();
            String type = (String) change.get("type");
            switch (type) {
                case "ADDED" -> added++;
                case "REMOVED" -> removed++;
                case "MODIFIED" -> modified++;
            }
        }
        return String.format("新增%d项, 删除%d项, 修改%d项", added, removed, modified);
    }
}
