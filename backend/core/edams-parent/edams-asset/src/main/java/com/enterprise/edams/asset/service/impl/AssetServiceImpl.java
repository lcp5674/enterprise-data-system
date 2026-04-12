package com.enterprise.edams.asset.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.enterprise.edams.asset.dto.AssetCreateRequest;
import com.enterprise.edams.asset.dto.AssetDTO;
import com.enterprise.edams.asset.dto.AssetQueryRequest;
import com.enterprise.edams.asset.dto.AssetUpdateRequest;
import com.enterprise.edams.asset.entity.Asset;
import com.enterprise.edams.asset.entity.AssetTag;
import com.enterprise.edams.asset.entity.AssetTagRelation;
import com.enterprise.edams.asset.repository.AssetMapper;
import com.enterprise.edams.asset.repository.AssetTagMapper;
import com.enterprise.edams.asset.repository.AssetTagRelationMapper;
import com.enterprise.edams.asset.service.AssetService;
import com.enterprise.edams.common.enums.AssetStatus;
import com.enterprise.edams.common.exception.BusinessException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 资产服务实现
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssetServiceImpl implements AssetService {

    private final AssetMapper assetMapper;
    private final AssetTagMapper tagMapper;
    private final AssetTagRelationMapper tagRelationMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AssetDTO createAsset(AssetCreateRequest request, String operator) {
        // 校验资产名称唯一性
        if (assetMapper.existsByAssetName(request.getAssetName()) > 0) {
            throw new BusinessException("资产名称已存在: " + request.getAssetName());
        }

        // 生成资产编码
        String assetCode = StringUtils.isNotBlank(request.getAssetCode()) 
                ? request.getAssetCode() 
                : generateAssetCode();
        
        if (assetMapper.existsByAssetCode(assetCode) > 0) {
            throw new BusinessException("资产编码已存在: " + assetCode);
        }

        // 创建资产实体
        Asset asset = Asset.builder()
                .assetName(request.getAssetName())
                .assetCode(assetCode)
                .assetType(request.getAssetType())
                .description(request.getDescription())
                .ownerId(request.getOwnerId())
                .sensitivity(request.getSensitivity())
                .status(AssetStatus.DRAFT)
                .datasourceId(request.getDatasourceId())
                .datasourceName(request.getDatasourceName())
                .databaseName(request.getDatabaseName())
                .schemaName(request.getSchemaName())
                .tableName(request.getTableName())
                .catalogId(request.getCatalogId())
                .domainId(request.getDomainId())
                .properties(request.getProperties())
                .version(1)
                .build();

        asset.setCreatedBy(operator);
        asset.setUpdatedBy(operator);
        assetMapper.insert(asset);

        // 处理标签
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            addTagsToAsset(asset.getId(), request.getTagIds(), operator);
        }

        log.info("资产创建成功: id={}, name={}", asset.getId(), asset.getAssetName());
        return convertToDTO(asset);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AssetDTO updateAsset(Long id, AssetUpdateRequest request, String operator) {
        Asset asset = assetMapper.selectById(id);
        if (asset == null) {
            throw new BusinessException("资产不存在: " + id);
        }

        // 检查是否可编辑
        if (!asset.isEditable()) {
            throw new BusinessException("当前状态不允许编辑: " + asset.getStatus());
        }

        // 校验资产名称唯一性(排除自己)
        Asset existAsset = assetMapper.selectByAssetName(request.getAssetName());
        if (existAsset != null && !existAsset.getId().equals(id)) {
            throw new BusinessException("资产名称已存在: " + request.getAssetName());
        }

        // 更新属性
        asset.setAssetName(request.getAssetName());
        asset.setDescription(request.getDescription());
        asset.setOwnerId(request.getOwnerId());
        asset.setSensitivity(request.getSensitivity());
        asset.setCatalogId(request.getCatalogId());
        asset.setDomainId(request.getDomainId());
        asset.setProperties(request.getProperties());
        asset.setUpdatedBy(operator);
        
        assetMapper.updateById(asset);
        assetMapper.incrementVersion(id);

        // 更新标签
        if (request.getTagIds() != null) {
            // 先删除旧标签
            tagRelationMapper.deleteByAssetId(id);
            // 添加新标签
            if (!request.getTagIds().isEmpty()) {
                addTagsToAsset(id, request.getTagIds(), operator);
            }
        }

        log.info("资产更新成功: id={}", id);
        return getAssetById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAsset(Long id, String operator) {
        Asset asset = assetMapper.selectById(id);
        if (asset == null) {
            throw new BusinessException("资产不存在: " + id);
        }

        if (asset.getStatus() == AssetStatus.PUBLISHED) {
            throw new BusinessException("已发布资产不能直接删除,请先归档");
        }

        // 删除标签关联
        tagRelationMapper.deleteByAssetId(id);
        
        // 逻辑删除资产
        assetMapper.deleteById(id);
        
        log.info("资产删除成功: id={}", id);
    }

    @Override
    public AssetDTO getAssetById(Long id) {
        Asset asset = assetMapper.selectById(id);
        if (asset == null) {
            throw new BusinessException("资产不存在: " + id);
        }
        return convertToDTO(asset);
    }

    @Override
    public AssetDTO getAssetByCode(String assetCode) {
        Asset asset = assetMapper.selectByAssetCode(assetCode);
        if (asset == null) {
            throw new BusinessException("资产不存在: " + assetCode);
        }
        return convertToDTO(asset);
    }

    @Override
    public IPage<AssetDTO> queryAssets(AssetQueryRequest request) {
        Page<Asset> page = new Page<>(request.getPageNum(), request.getPageSize());
        LambdaQueryWrapper<Asset> wrapper = new LambdaQueryWrapper<>();

        // 关键词搜索
        if (StringUtils.isNotBlank(request.getKeyword())) {
            wrapper.and(w -> w.like(Asset::getAssetName, request.getKeyword())
                    .or().like(Asset::getAssetCode, request.getKeyword())
                    .or().like(Asset::getDescription, request.getKeyword()));
        }

        // 其他条件
        if (StringUtils.isNotBlank(request.getAssetName())) {
            wrapper.like(Asset::getAssetName, request.getAssetName());
        }
        if (StringUtils.isNotBlank(request.getAssetCode())) {
            wrapper.eq(Asset::getAssetCode, request.getAssetCode());
        }
        if (request.getAssetTypes() != null && !request.getAssetTypes().isEmpty()) {
            wrapper.in(Asset::getAssetType, request.getAssetTypes());
        }
        if (request.getStatuses() != null && !request.getStatuses().isEmpty()) {
            wrapper.in(Asset::getStatus, request.getStatuses());
        }
        if (request.getSensitivities() != null && !request.getSensitivities().isEmpty()) {
            wrapper.in(Asset::getSensitivity, request.getSensitivities());
        }
        if (request.getOwnerId() != null) {
            wrapper.eq(Asset::getOwnerId, request.getOwnerId());
        }
        if (request.getDatasourceId() != null) {
            wrapper.eq(Asset::getDatasourceId, request.getDatasourceId());
        }
        if (request.getCatalogId() != null) {
            wrapper.eq(Asset::getCatalogId, request.getCatalogId());
        }
        if (request.getDomainId() != null) {
            wrapper.eq(Asset::getDomainId, request.getDomainId());
        }

        // 排序
        if (Boolean.TRUE.equals(request.getAsc())) {
            wrapper.orderByAsc(Asset::getCreatedTime);
        } else {
            wrapper.orderByDesc(Asset::getCreatedTime);
        }

        IPage<Asset> assetPage = assetMapper.selectPage(page, wrapper);
        
        // 转换为DTO
        List<AssetDTO> dtoList = assetPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        Page<AssetDTO> resultPage = new Page<>(assetPage.getCurrent(), assetPage.getSize(), assetPage.getTotal());
        resultPage.setRecords(dtoList);
        return resultPage;
    }

    @Override
    public List<AssetDTO> searchAssets(String keyword) {
        if (StringUtils.isBlank(keyword)) {
            return new ArrayList<>();
        }
        
        LambdaQueryWrapper<Asset> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.like(Asset::getAssetName, keyword)
                .or().like(Asset::getAssetCode, keyword)
                .or().like(Asset::getDescription, keyword))
                .orderByDesc(Asset::getCreatedTime);
        
        return assetMapper.selectList(wrapper).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAssetStatus(Long id, AssetStatus status, String operator) {
        Asset asset = assetMapper.selectById(id);
        if (asset == null) {
            throw new BusinessException("资产不存在: " + id);
        }
        
        asset.setStatus(status);
        asset.setUpdatedBy(operator);
        assetMapper.updateById(asset);
        
        log.info("资产状态更新成功: id={}, status={}", id, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishAsset(Long id, String operator) {
        Asset asset = assetMapper.selectById(id);
        if (asset == null) {
            throw new BusinessException("资产不存在: " + id);
        }
        
        if (asset.getStatus() != AssetStatus.DRAFT && asset.getStatus() != AssetStatus.REVIEWING) {
            throw new BusinessException("当前状态不允许发布: " + asset.getStatus());
        }
        
        asset.setStatus(AssetStatus.PUBLISHED);
        asset.setPublishTime(LocalDateTime.now());
        asset.setUpdatedBy(operator);
        assetMapper.updateById(asset);
        
        log.info("资产发布成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void archiveAsset(Long id, String operator) {
        Asset asset = assetMapper.selectById(id);
        if (asset == null) {
            throw new BusinessException("资产不存在: " + id);
        }
        
        asset.setStatus(AssetStatus.ARCHIVED);
        asset.setUpdatedBy(operator);
        assetMapper.updateById(asset);
        
        log.info("资产归档成功: id={}", id);
    }

    @Override
    public IPage<AssetDTO> getAssetsByCatalog(Long catalogId, int pageNum, int pageSize) {
        Page<Asset> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Asset> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Asset::getCatalogId, catalogId)
               .orderByDesc(Asset::getCreatedTime);
        
        IPage<Asset> assetPage = assetMapper.selectPage(page, wrapper);
        List<AssetDTO> dtoList = assetPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        Page<AssetDTO> resultPage = new Page<>(assetPage.getCurrent(), assetPage.getSize(), assetPage.getTotal());
        resultPage.setRecords(dtoList);
        return resultPage;
    }

    @Override
    public IPage<AssetDTO> getAssetsByDomain(Long domainId, int pageNum, int pageSize) {
        Page<Asset> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Asset> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Asset::getDomainId, domainId)
               .orderByDesc(Asset::getCreatedTime);
        
        IPage<Asset> assetPage = assetMapper.selectPage(page, wrapper);
        List<AssetDTO> dtoList = assetPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        Page<AssetDTO> resultPage = new Page<>(assetPage.getCurrent(), assetPage.getSize(), assetPage.getTotal());
        resultPage.setRecords(dtoList);
        return resultPage;
    }

    @Override
    public IPage<AssetDTO> getAssetsByOwner(Long ownerId, int pageNum, int pageSize) {
        Page<Asset> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Asset> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Asset::getOwnerId, ownerId)
               .orderByDesc(Asset::getCreatedTime);
        
        IPage<Asset> assetPage = assetMapper.selectPage(page, wrapper);
        List<AssetDTO> dtoList = assetPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        Page<AssetDTO> resultPage = new Page<>(assetPage.getCurrent(), assetPage.getSize(), assetPage.getTotal());
        resultPage.setRecords(dtoList);
        return resultPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void tagAsset(Long assetId, List<Long> tagIds, String operator) {
        // 先删除旧标签
        tagRelationMapper.deleteByAssetId(assetId);
        // 添加新标签
        if (tagIds != null && !tagIds.isEmpty()) {
            addTagsToAsset(assetId, tagIds, operator);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void untagAsset(Long assetId, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        
        for (Long tagId : tagIds) {
            LambdaQueryWrapper<AssetTagRelation> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AssetTagRelation::getAssetId, assetId)
                   .eq(AssetTagRelation::getTagId, tagId);
            tagRelationMapper.delete(wrapper);
            
            // 减少标签使用次数
            tagMapper.decrementUsageCount(tagId);
        }
    }

    @Override
    public List<String> getAssetTags(Long assetId) {
        return tagMapper.selectTagsByAssetId(assetId).stream()
                .map(AssetTag::getTagName)
                .collect(Collectors.toList());
    }

    @Override
    public long countAssetsByOwner(Long ownerId) {
        LambdaQueryWrapper<Asset> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Asset::getOwnerId, ownerId);
        return assetMapper.selectCount(wrapper);
    }

    @Override
    public long countAssetsByDomain(Long domainId) {
        return assetMapper.countByDomainId(domainId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncAssetMetadata(Long id, String operator) {
        Asset asset = assetMapper.selectById(id);
        if (asset == null) {
            throw new BusinessException("资产不存在: " + id);
        }
        
        // TODO: 调用元数据采集服务同步元数据
        asset.setLastSyncTime(LocalDateTime.now());
        asset.setUpdatedBy(operator);
        assetMapper.updateById(asset);
        
        log.info("资产元数据同步成功: id={}", id);
    }

    private void addTagsToAsset(Long assetId, List<Long> tagIds, String operator) {
        for (Long tagId : tagIds) {
            // 检查是否已关联
            if (tagRelationMapper.exists(assetId, tagId) > 0) {
                continue;
            }
            
            AssetTagRelation relation = new AssetTagRelation();
            relation.setAssetId(assetId);
            relation.setTagId(tagId);
            // 安全处理：如果operator是数字字符串则转换，否则设为null或默认值
            Long taggerId = parseOperatorToLong(operator);
            relation.setTaggerId(taggerId);
            relation.setTagTime(LocalDateTime.now());
            relation.setCreatedBy(operator);
            relation.setUpdatedBy(operator);
            tagRelationMapper.insert(relation);
            
            // 增加标签使用次数
            tagMapper.incrementUsageCount(tagId, LocalDateTime.now());
        }
    }

    private String generateAssetCode() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "ASSET" + dateStr + random;
    }

    private AssetDTO convertToDTO(Asset asset) {
        AssetDTO dto = new AssetDTO();
        BeanUtils.copyProperties(asset, dto);
        
        // 处理标签
        List<AssetTag> tags = tagMapper.selectTagsByAssetId(asset.getId());
        dto.setTagList(tags.stream().map(AssetTag::getTagName).collect(Collectors.toList()));
        dto.setTagIds(tags.stream().map(AssetTag::getId).collect(Collectors.toList()));
        
        return dto;
    }
    
    /**
     * 安全解析操作人ID
     * @param operator 操作人（可能是用户名或用户ID）
     * @return 用户ID，如果无法解析则返回null
     */
    private Long parseOperatorToLong(String operator) {
        if (operator == null || operator.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(operator);
        } catch (NumberFormatException e) {
            // 如果是用户名，需要查询用户ID（这里简化处理，返回null）
            log.debug("操作人 {} 不是数字ID，将不设置taggerId", operator);
            return null;
        }
    }
}
