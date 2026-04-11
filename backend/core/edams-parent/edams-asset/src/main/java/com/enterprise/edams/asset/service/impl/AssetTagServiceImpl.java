package com.enterprise.edams.asset.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.enterprise.edams.asset.dto.AssetTagCreateRequest;
import com.enterprise.edams.asset.dto.AssetTagDTO;
import com.enterprise.edams.asset.entity.AssetTag;
import com.enterprise.edams.asset.entity.AssetTagRelation;
import com.enterprise.edams.asset.repository.AssetTagMapper;
import com.enterprise.edams.asset.repository.AssetTagRelationMapper;
import com.enterprise.edams.asset.service.AssetTagService;
import com.enterprise.edams.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 资产标签服务实现
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssetTagServiceImpl implements AssetTagService {

    private final AssetTagMapper tagMapper;
    private final AssetTagRelationMapper tagRelationMapper;

    // 预定义标签分类
    private static final List<String> TAG_CATEGORIES = Arrays.asList(
            "业务域", "部门", "项目", "优先级", "状态", "自定义"
    );

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AssetTagDTO createTag(AssetTagCreateRequest request, Long creatorId) {
        // 校验标签名称唯一性
        if (tagMapper.existsByTagName(request.getTagName()) > 0) {
            throw new BusinessException("标签名称已存在: " + request.getTagName());
        }

        // 生成标签编码
        String tagCode = StringUtils.isNotBlank(request.getTagCode()) 
                ? request.getTagCode() 
                : generateTagCode(request.getTagName());
        
        if (tagMapper.existsByTagCode(tagCode) > 0) {
            throw new BusinessException("标签编码已存在: " + tagCode);
        }

        // 默认颜色
        String color = StringUtils.isNotBlank(request.getColor()) 
                ? request.getColor() 
                : generateRandomColor();

        AssetTag tag = new AssetTag();
        tag.setTagName(request.getTagName());
        tag.setTagCode(tagCode);
        tag.setColor(color);
        tag.setDescription(request.getDescription());
        tag.setCategory(StringUtils.isNotBlank(request.getCategory()) ? request.getCategory() : "自定义");
        tag.setStatus(1);
        tag.setUsageCount(0);
        tag.setCreatorId(creatorId);

        tagMapper.insert(tag);
        
        log.info("标签创建成功: id={}, name={}", tag.getId(), tag.getTagName());
        return convertToDTO(tag);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AssetTagDTO updateTag(Long id, AssetTagCreateRequest request) {
        AssetTag tag = tagMapper.selectById(id);
        if (tag == null) {
            throw new BusinessException("标签不存在: " + id);
        }

        // 校验标签名称唯一性(排除自己)
        AssetTag existTag = tagMapper.selectByTagName(request.getTagName());
        if (existTag != null && !existTag.getId().equals(id)) {
            throw new BusinessException("标签名称已存在: " + request.getTagName());
        }

        tag.setTagName(request.getTagName());
        tag.setDescription(request.getDescription());
        tag.setColor(request.getColor());
        if (StringUtils.isNotBlank(request.getCategory())) {
            tag.setCategory(request.getCategory());
        }

        tagMapper.updateById(tag);
        
        log.info("标签更新成功: id={}", id);
        return convertToDTO(tag);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTag(Long id) {
        AssetTag tag = tagMapper.selectById(id);
        if (tag == null) {
            throw new BusinessException("标签不存在: " + id);
        }

        // 检查是否有关联资产
        int assetCount = tagRelationMapper.countByTagId(id);
        if (assetCount > 0) {
            throw new BusinessException("该标签已关联" + assetCount + "个资产,请先解除关联");
        }

        tagMapper.deleteById(id);
        log.info("标签删除成功: id={}", id);
    }

    @Override
    public AssetTagDTO getTagById(Long id) {
        AssetTag tag = tagMapper.selectById(id);
        if (tag == null) {
            throw new BusinessException("标签不存在: " + id);
        }
        return convertToDTO(tag);
    }

    @Override
    public AssetTagDTO getTagByCode(String tagCode) {
        AssetTag tag = tagMapper.selectByTagCode(tagCode);
        if (tag == null) {
            throw new BusinessException("标签不存在: " + tagCode);
        }
        return convertToDTO(tag);
    }

    @Override
    public AssetTagDTO getTagByName(String tagName) {
        AssetTag tag = tagMapper.selectByTagName(tagName);
        if (tag == null) {
            throw new BusinessException("标签不存在: " + tagName);
        }
        return convertToDTO(tag);
    }

    @Override
    public IPage<AssetTagDTO> queryTags(String keyword, String category, Integer status, int pageNum, int pageSize) {
        Page<AssetTag> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AssetTag> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(AssetTag::getTagName, keyword)
                    .or().like(AssetTag::getTagCode, keyword)
                    .or().like(AssetTag::getDescription, keyword));
        }

        if (StringUtils.isNotBlank(category)) {
            wrapper.eq(AssetTag::getCategory, category);
        }

        if (status != null) {
            wrapper.eq(AssetTag::getStatus, status);
        }

        wrapper.orderByDesc(AssetTag::getUsageCount)
               .orderByDesc(AssetTag::getCreatedTime);

        IPage<AssetTag> tagPage = tagMapper.selectPage(page, wrapper);
        
        List<AssetTagDTO> dtoList = tagPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        Page<AssetTagDTO> resultPage = new Page<>(tagPage.getCurrent(), tagPage.getSize(), tagPage.getTotal());
        resultPage.setRecords(dtoList);
        return resultPage;
    }

    @Override
    public List<AssetTagDTO> getAllTags() {
        LambdaQueryWrapper<AssetTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AssetTag::getStatus, 1)
               .orderByDesc(AssetTag::getUsageCount);
        
        return tagMapper.selectList(wrapper).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AssetTagDTO> getTagsByCategory(String category) {
        return tagMapper.selectByCategory(category).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AssetTagDTO> getHotTags(int limit) {
        return tagMapper.selectHotTags(limit).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AssetTagDTO> searchTags(String keyword, int limit) {
        return tagMapper.searchByName(keyword, limit).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addTagToAsset(Long assetId, Long tagId, Long operatorId) {
        // 检查标签是否存在
        AssetTag tag = tagMapper.selectById(tagId);
        if (tag == null) {
            throw new BusinessException("标签不存在: " + tagId);
        }

        // 检查是否已关联
        if (tagRelationMapper.exists(assetId, tagId) > 0) {
            return; // 已存在,不重复添加
        }

        AssetTagRelation relation = new AssetTagRelation();
        relation.setAssetId(assetId);
        relation.setTagId(tagId);
        relation.setTaggerId(operatorId);
        relation.setTagTime(LocalDateTime.now());
        relation.setCreatedBy(String.valueOf(operatorId));
        relation.setUpdatedBy(String.valueOf(operatorId));
        
        tagRelationMapper.insert(relation);
        
        // 增加标签使用次数
        tagMapper.incrementUsageCount(tagId, LocalDateTime.now());
        
        log.info("为资产添加标签: assetId={}, tagId={}", assetId, tagId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addTagsToAsset(Long assetId, List<Long> tagIds, Long operatorId) {
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        
        for (Long tagId : tagIds) {
            addTagToAsset(assetId, tagId, operatorId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeTagFromAsset(Long assetId, Long tagId) {
        LambdaQueryWrapper<AssetTagRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AssetTagRelation::getAssetId, assetId)
               .eq(AssetTagRelation::getTagId, tagId);
        
        int count = tagRelationMapper.delete(wrapper);
        
        if (count > 0) {
            // 减少标签使用次数
            tagMapper.decrementUsageCount(tagId);
            log.info("移除资产标签: assetId={}, tagId={}", assetId, tagId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeAllTagsFromAsset(Long assetId) {
        List<AssetTag> tags = tagMapper.selectTagsByAssetId(assetId);
        
        // 删除关联
        LambdaQueryWrapper<AssetTagRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AssetTagRelation::getAssetId, assetId);
        tagRelationMapper.delete(wrapper);
        
        // 减少使用次数
        for (AssetTag tag : tags) {
            tagMapper.decrementUsageCount(tag.getId());
        }
        
        log.info("移除资产所有标签: assetId={}", assetId);
    }

    @Override
    public List<AssetTagDTO> getTagsByAssetId(Long assetId) {
        return tagMapper.selectTagsByAssetId(assetId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enableTag(Long id) {
        AssetTag tag = tagMapper.selectById(id);
        if (tag == null) {
            throw new BusinessException("标签不存在: " + id);
        }
        
        tag.setStatus(1);
        tagMapper.updateById(tag);
        
        log.info("标签启用成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disableTag(Long id) {
        AssetTag tag = tagMapper.selectById(id);
        if (tag == null) {
            throw new BusinessException("标签不存在: " + id);
        }
        
        tag.setStatus(0);
        tagMapper.updateById(tag);
        
        log.info("标签禁用成功: id={}", id);
    }

    @Override
    public List<String> getTagCategories() {
        return TAG_CATEGORIES;
    }

    private String generateTagCode(String tagName) {
        // 使用标签名称拼音首字母+时间戳
        String pinyin = tagName.replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5]", "");
        return "TAG" + System.currentTimeMillis();
    }

    private String generateRandomColor() {
        // 生成随机柔和颜色
        String[] colors = {
            "#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4", "#FFEAA7",
            "#DDA0DD", "#98D8C8", "#F7DC6F", "#BB8FCE", "#85C1E9",
            "#F8B500", "#6C5CE7", "#00B894", "#E17055", "#74B9FF"
        };
        return colors[(int)(Math.random() * colors.length)];
    }

    private AssetTagDTO convertToDTO(AssetTag tag) {
        AssetTagDTO dto = new AssetTagDTO();
        BeanUtils.copyProperties(tag, dto);
        return dto;
    }
}
