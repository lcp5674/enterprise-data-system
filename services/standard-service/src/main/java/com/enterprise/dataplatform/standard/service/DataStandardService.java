package com.enterprise.dataplatform.standard.service;

import com.enterprise.dataplatform.standard.domain.entity.DataStandard;
import com.enterprise.dataplatform.standard.domain.entity.StandardVersion;
import com.enterprise.dataplatform.standard.dto.request.DataStandardRequest;
import com.enterprise.dataplatform.standard.dto.response.DataStandardResponse;
import com.enterprise.dataplatform.standard.dto.response.StandardMappingResponse;
import com.enterprise.dataplatform.standard.dto.response.StandardVersionResponse;
import com.enterprise.dataplatform.standard.repository.DataStandardRepository;
import com.enterprise.dataplatform.standard.repository.StandardVersionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据标准服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataStandardService {

    private final DataStandardRepository standardRepository;
    private final StandardVersionRepository versionRepository;
    private final ObjectMapper objectMapper;

    /**
     * 创建数据标准
     */
    @Transactional
    public DataStandardResponse createStandard(DataStandardRequest request, String creator) {
        log.info("创建数据标准: {}, 创建人: {}", request.getStandardCode(), creator);

        // 检查编码是否已存在
        if (standardRepository.existsByStandardCode(request.getStandardCode())) {
            throw new IllegalArgumentException("标准编码已存在: " + request.getStandardCode());
        }

        // 创建标准
        DataStandard standard = DataStandard.builder()
                .standardCode(request.getStandardCode())
                .standardName(request.getStandardName())
                .description(request.getDescription())
                .category(request.getCategory())
                .standardType(request.getStandardType())
                .ruleContent(request.getRuleContent())
                .dataType(request.getDataType())
                .valueRange(request.getValueRange())
                .precisionRequired(request.getPrecisionRequired())
                .maxLength(request.getMaxLength())
                .required(request.getRequired())
                .defaultValue(request.getDefaultValue())
                .status("DRAFT")
                .priority(request.getPriority())
                .version(1)
                .source(request.getSource())
                .externalRef(request.getExternalRef())
                .applicableScope(request.getApplicableScope())
                .violationHandling(request.getViolationHandling())
                .creator(creator)
                .build();

        standard = standardRepository.save(standard);

        // 创建初始版本
        createVersion(standard, "CREATE", "初始版本", "创建数据标准", creator);

        log.info("数据标准创建成功: {}", standard.getId());
        return toResponse(standard);
    }

    /**
     * 更新数据标准
     */
    @Transactional
    public DataStandardResponse updateStandard(Long id, DataStandardRequest request, String updater) {
        log.info("更新数据标准: {}, 更新人: {}", id, updater);

        DataStandard standard = standardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("数据标准不存在: " + id));

        // 记录变更前的状态
        String beforeContent = toJson(standard);

        // 更新字段
        standard.setStandardName(request.getStandardName());
        standard.setDescription(request.getDescription());
        standard.setCategory(request.getCategory());
        standard.setStandardType(request.getStandardType());
        standard.setRuleContent(request.getRuleContent());
        standard.setDataType(request.getDataType());
        standard.setValueRange(request.getValueRange());
        standard.setPrecisionRequired(request.getPrecisionRequired());
        standard.setMaxLength(request.getMaxLength());
        standard.setRequired(request.getRequired());
        standard.setDefaultValue(request.getDefaultValue());
        standard.setPriority(request.getPriority());
        standard.setSource(request.getSource());
        standard.setExternalRef(request.getExternalRef());
        standard.setApplicableScope(request.getApplicableScope());
        standard.setViolationHandling(request.getViolationHandling());
        standard.setUpdater(updater);

        standard = standardRepository.save(standard);

        // 创建版本记录
        String afterContent = toJson(standard);
        createVersion(standard, "UPDATE", "版本更新", 
                String.format("变更前: %s\n变更后: %s", beforeContent, afterContent), updater);

        log.info("数据标准更新成功: {}", id);
        return toResponse(standard);
    }

    /**
     * 发布数据标准
     */
    @Transactional
    public DataStandardResponse publishStandard(Long id, String publisher) {
        log.info("发布数据标准: {}, 发布人: {}", id, publisher);

        DataStandard standard = standardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("数据标准不存在: " + id));

        standard.setStatus("ACTIVE");
        standard.setUpdater(publisher);
        standard = standardRepository.save(standard);

        // 创建版本记录
        createVersion(standard, "UPDATE", "发布标准", "发布数据标准", publisher);

        log.info("数据标准发布成功: {}", id);
        return toResponse(standard);
    }

    /**
     * 废弃数据标准
     */
    @Transactional
    public DataStandardResponse deprecateStandard(Long id, String deprecator, String reason) {
        log.info("废弃数据标准: {}, 原因: {}", id, reason);

        DataStandard standard = standardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("数据标准不存在: " + id));

        standard.setStatus("DEPRECATED");
        standard.setUpdater(deprecator);
        standard = standardRepository.save(standard);

        // 创建版本记录
        createVersion(standard, "DEPRECATE", "废弃标准", reason, deprecator);

        log.info("数据标准废弃成功: {}", id);
        return toResponse(standard);
    }

    /**
     * 查询数据标准
     */
    public DataStandardResponse getStandard(Long id) {
        DataStandard standard = standardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("数据标准不存在: " + id));
        return toResponse(standard);
    }

    /**
     * 根据编码查询数据标准
     */
    public DataStandardResponse getStandardByCode(String standardCode) {
        DataStandard standard = standardRepository.findByStandardCode(standardCode)
                .orElseThrow(() -> new IllegalArgumentException("数据标准不存在: " + standardCode));
        return toResponse(standard);
    }

    /**
     * 分页查询数据标准
     */
    public Page<DataStandardResponse> searchStandards(
            String category, String status, String standardType, String keyword, Pageable pageable) {
        return standardRepository.searchStandards(category, status, standardType, keyword, pageable)
                .map(this::toResponse);
    }

    /**
     * 查询所有激活的标准
     */
    public List<DataStandardResponse> getAllActiveStandards() {
        return standardRepository.findAllActive().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 删除数据标准
     */
    @Transactional
    public void deleteStandard(Long id) {
        log.info("删除数据标准: {}", id);
        standardRepository.deleteById(id);
    }

    /**
     * 创建版本记录
     */
    private void createVersion(DataStandard standard, String changeType, 
                               String versionDescription, String changeReason, String creator) {
        StandardVersion version = StandardVersion.builder()
                .dataStandard(standard)
                .versionNo(standard.getVersion())
                .versionDescription(versionDescription)
                .changeType(changeType)
                .changeReason(changeReason)
                .beforeContent(null)
                .afterContent(toJson(standard))
                .status(standard.getStatus().equals("ACTIVE") ? "ACTIVE" : "DRAFT")
                .requiresApproval(false)
                .approvalStatus("APPROVED")
                .creator(creator)
                .effectiveTime(LocalDateTime.now())
                .build();

        versionRepository.save(version);
    }

    /**
     * 转换为响应DTO
     */
    private DataStandardResponse toResponse(DataStandard standard) {
        DataStandardResponse response = DataStandardResponse.builder()
                .id(standard.getId())
                .standardCode(standard.getStandardCode())
                .standardName(standard.getStandardName())
                .description(standard.getDescription())
                .category(standard.getCategory())
                .standardType(standard.getStandardType())
                .ruleContent(standard.getRuleContent())
                .dataType(standard.getDataType())
                .valueRange(standard.getValueRange())
                .precisionRequired(standard.getPrecisionRequired())
                .maxLength(standard.getMaxLength())
                .required(standard.getRequired())
                .defaultValue(standard.getDefaultValue())
                .status(standard.getStatus())
                .priority(standard.getPriority())
                .version(standard.getVersion())
                .source(standard.getSource())
                .externalRef(standard.getExternalRef())
                .applicableScope(standard.getApplicableScope())
                .violationHandling(standard.getViolationHandling())
                .creator(standard.getCreator())
                .createTime(standard.getCreateTime())
                .updater(standard.getUpdater())
                .updateTime(standard.getUpdateTime())
                .mappingCount(standard.getMappings() != null ? standard.getMappings().size() : 0)
                .build();

        // 添加版本历史
        if (standard.getVersions() != null && !standard.getVersions().isEmpty()) {
            response.setVersionHistory(standard.getVersions().stream()
                    .map(this::toVersionResponse)
                    .collect(Collectors.toList()));
        }

        // 添加映射列表
        if (standard.getMappings() != null && !standard.getMappings().isEmpty()) {
            response.setMappings(standard.getMappings().stream()
                    .map(this::toMappingResponse)
                    .collect(Collectors.toList()));
        }

        return response;
    }

    private StandardVersionResponse toVersionResponse(StandardVersion version) {
        return StandardVersionResponse.builder()
                .id(version.getId())
                .standardId(version.getDataStandard().getId())
                .versionNo(version.getVersionNo())
                .versionDescription(version.getVersionDescription())
                .changeType(version.getChangeType())
                .changeContent(version.getChangeContent())
                .changeReason(version.getChangeReason())
                .status(version.getStatus())
                .requiresApproval(version.getRequiresApproval())
                .approvalStatus(version.getApprovalStatus())
                .approver(version.getApprover())
                .approveTime(version.getApproveTime())
                .approvalComment(version.getApprovalComment())
                .creator(version.getCreator())
                .createTime(version.getCreateTime())
                .effectiveTime(version.getEffectiveTime())
                .expiryTime(version.getExpiryTime())
                .build();
    }

    private StandardMappingResponse toMappingResponse(com.enterprise.dataplatform.standard.domain.entity.StandardMapping mapping) {
        return StandardMappingResponse.builder()
                .id(mapping.getId())
                .standardId(mapping.getDataStandard().getId())
                .standardCode(mapping.getDataStandard().getStandardCode())
                .standardName(mapping.getDataStandard().getStandardName())
                .assetId(mapping.getAssetId())
                .assetName(mapping.getAssetName())
                .assetType(mapping.getAssetType())
                .fieldName(mapping.getFieldName())
                .fieldChineseName(mapping.getFieldChineseName())
                .fieldDataType(mapping.getFieldDataType())
                .mappingStatus(mapping.getMappingStatus())
                .mappingType(mapping.getMappingType())
                .coverageRate(mapping.getCoverageRate())
                .qualityScore(mapping.getQualityScore())
                .creator(mapping.getCreator())
                .createTime(mapping.getCreateTime())
                .build();
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("序列化对象失败", e);
            return "{}";
        }
    }
}
