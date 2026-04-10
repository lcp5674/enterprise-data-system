package com.enterprise.dataplatform.standard.service;

import com.enterprise.dataplatform.standard.domain.entity.DataStandard;
import com.enterprise.dataplatform.standard.domain.entity.StandardMapping;
import com.enterprise.dataplatform.standard.dto.request.StandardMappingRequest;
import com.enterprise.dataplatform.standard.dto.response.StandardMappingResponse;
import com.enterprise.dataplatform.standard.repository.DataStandardRepository;
import com.enterprise.dataplatform.standard.repository.StandardMappingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 标准映射服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StandardMappingService {

    private final StandardMappingRepository mappingRepository;
    private final DataStandardRepository standardRepository;

    /**
     * 创建标准映射
     */
    @Transactional
    public StandardMappingResponse createMapping(StandardMappingRequest request, String creator) {
        log.info("创建标准映射: 资产={}, 字段={}, 标准ID={}", 
                request.getAssetId(), request.getFieldName(), request.getStandardId());

        // 检查映射是否已存在
        if (mappingRepository.existsByAssetIdAndFieldName(request.getAssetId(), request.getFieldName())) {
            throw new IllegalArgumentException("映射已存在: " + request.getAssetId() + "." + request.getFieldName());
        }

        // 获取数据标准
        DataStandard standard = standardRepository.findById(request.getStandardId())
                .orElseThrow(() -> new IllegalArgumentException("数据标准不存在: " + request.getStandardId()));

        // 创建映射
        StandardMapping mapping = StandardMapping.builder()
                .dataStandard(standard)
                .assetId(request.getAssetId())
                .assetName(request.getAssetName())
                .assetType(request.getAssetType())
                .fieldName(request.getFieldName())
                .fieldChineseName(request.getFieldChineseName())
                .fieldDataType(request.getFieldDataType())
                .mappingStatus("PENDING")
                .mappingType(request.getMappingType())
                .transformRule(request.getTransformRule())
                .mappingDescription(request.getMappingDescription())
                .mappingSource(request.getMappingSource())
                .isKeyField(request.getIsKeyField())
                .sensitivityLevel(request.getSensitivityLevel())
                .version(1)
                .validFrom(LocalDateTime.now())
                .creator(creator)
                .build();

        // 设置有效期
        if (request.getValidTo() != null) {
            mapping.setValidTo(LocalDateTime.parse(request.getValidTo(), 
                    DateTimeFormatter.ISO_DATE_TIME));
        }

        mapping = mappingRepository.save(mapping);

        log.info("标准映射创建成功: {}", mapping.getId());
        return toResponse(mapping);
    }

    /**
     * 批量创建映射
     */
    @Transactional
    public List<StandardMappingResponse> batchCreateMappings(
            List<StandardMappingRequest> requests, String creator) {
        log.info("批量创建标准映射: 数量={}", requests.size());

        List<StandardMapping> mappings = requests.stream()
                .map(request -> {
                    DataStandard standard = standardRepository.findById(request.getStandardId())
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "数据标准不存在: " + request.getStandardId()));

                    StandardMapping mapping = StandardMapping.builder()
                            .dataStandard(standard)
                            .assetId(request.getAssetId())
                            .assetName(request.getAssetName())
                            .assetType(request.getAssetType())
                            .fieldName(request.getFieldName())
                            .fieldChineseName(request.getFieldChineseName())
                            .fieldDataType(request.getFieldDataType())
                            .mappingStatus("PENDING")
                            .mappingType(request.getMappingType())
                            .transformRule(request.getTransformRule())
                            .mappingDescription(request.getMappingDescription())
                            .mappingSource(request.getMappingSource())
                            .isKeyField(request.getIsKeyField())
                            .sensitivityLevel(request.getSensitivityLevel())
                            .version(1)
                            .validFrom(LocalDateTime.now())
                            .creator(creator)
                            .build();

                    if (request.getValidTo() != null) {
                        mapping.setValidTo(LocalDateTime.parse(request.getValidTo(),
                                DateTimeFormatter.ISO_DATE_TIME));
                    }

                    return mapping;
                })
                .collect(Collectors.toList());

        mappings = mappingRepository.saveAll(mappings);

        log.info("批量创建标准映射成功: {}", mappings.size());
        return mappings.stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * 更新映射
     */
    @Transactional
    public StandardMappingResponse updateMapping(Long id, StandardMappingRequest request, String updater) {
        log.info("更新标准映射: {}", id);

        StandardMapping mapping = mappingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("映射不存在: " + id));

        // 更新字段
        mapping.setAssetName(request.getAssetName());
        mapping.setfieldChineseName(request.getFieldChineseName());
        mapping.setFieldDataType(request.getFieldDataType());
        mapping.setMappingType(request.getMappingType());
        mapping.setTransformRule(request.getTransformRule());
        mapping.setMappingDescription(request.getMappingDescription());
        mapping.setIsKeyField(request.getIsKeyField());
        mapping.setSensitivityLevel(request.getSensitivityLevel());
        mapping.setVersion(mapping.getVersion() + 1);
        mapping.setUpdater(updater);

        mapping = mappingRepository.save(mapping);

        log.info("标准映射更新成功: {}", id);
        return toResponse(mapping);
    }

    /**
     * 审批映射
     */
    @Transactional
    public StandardMappingResponse approveMapping(Long id, String approver, String comment) {
        log.info("审批标准映射: {}, 审批人: {}", id, approver);

        StandardMapping mapping = mappingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("映射不存在: " + id));

        mapping.setMappingStatus("APPROVED");
        mapping.setApprover(approver);
        mapping.setApproveTime(LocalDateTime.now());
        mapping.setApprovalComment(comment);
        mapping.setUpdater(approver);

        mapping = mappingRepository.save(mapping);

        log.info("标准映射审批成功: {}", id);
        return toResponse(mapping);
    }

    /**
     * 拒绝映射
     */
    @Transactional
    public StandardMappingResponse rejectMapping(Long id, String approver, String reason) {
        log.info("拒绝标准映射: {}, 原因: {}", id, reason);

        StandardMapping mapping = mappingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("映射不存在: " + id));

        mapping.setMappingStatus("REJECTED");
        mapping.setApprover(approver);
        mapping.setApproveTime(LocalDateTime.now());
        mapping.setApprovalComment(reason);
        mapping.setUpdater(approver);

        mapping = mappingRepository.save(mapping);

        log.info("标准映射拒绝成功: {}", id);
        return toResponse(mapping);
    }

    /**
     * 查询映射
     */
    public StandardMappingResponse getMapping(Long id) {
        StandardMapping mapping = mappingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("映射不存在: " + id));
        return toResponse(mapping);
    }

    /**
     * 根据标准ID查询映射
     */
    public List<StandardMappingResponse> getMappingsByStandardId(Long standardId) {
        return mappingRepository.findByDataStandardId(standardId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 根据资产ID查询映射
     */
    public List<StandardMappingResponse> getMappingsByAssetId(String assetId) {
        return mappingRepository.findByAssetId(assetId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 分页查询映射
     */
    public Page<StandardMappingResponse> searchMappings(
            Long standardId, String assetId, String mappingStatus, Pageable pageable) {
        return mappingRepository.searchMappings(standardId, assetId, mappingStatus, pageable)
                .map(this::toResponse);
    }

    /**
     * 查询待审批的映射
     */
    public Page<StandardMappingResponse> getPendingMappings(Pageable pageable) {
        return mappingRepository.findPendingMappings(pageable)
                .map(this::toResponse);
    }

    /**
     * 删除映射
     */
    @Transactional
    public void deleteMapping(Long id) {
        log.info("删除标准映射: {}", id);
        mappingRepository.deleteById(id);
    }

    /**
     * 转换为响应DTO
     */
    private StandardMappingResponse toResponse(StandardMapping mapping) {
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
                .transformRule(mapping.getTransformRule())
                .coverageRate(mapping.getCoverageRate())
                .qualityScore(mapping.getQualityScore())
                .mappingDescription(mapping.getMappingDescription())
                .mappingSource(mapping.getMappingSource())
                .isKeyField(mapping.getIsKeyField())
                .sensitivityLevel(mapping.getSensitivityLevel())
                .version(mapping.getVersion())
                .validFrom(mapping.getValidFrom())
                .validTo(mapping.getValidTo())
                .creator(mapping.getCreator())
                .createTime(mapping.getCreateTime())
                .updater(mapping.getUpdater())
                .updateTime(mapping.getUpdateTime())
                .approver(mapping.getApprover())
                .approveTime(mapping.getApproveTime())
                .approvalComment(mapping.getApprovalComment())
                .build();
    }
}
