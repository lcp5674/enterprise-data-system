package com.enterprise.edams.asset.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.enterprise.edams.asset.dto.AssetTagCreateRequest;
import com.enterprise.edams.asset.dto.AssetTagDTO;

import java.util.List;

/**
 * 资产标签服务接口
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
public interface AssetTagService {

    /**
     * 创建标签
     */
    AssetTagDTO createTag(AssetTagCreateRequest request, Long creatorId);

    /**
     * 更新标签
     */
    AssetTagDTO updateTag(Long id, AssetTagCreateRequest request);

    /**
     * 删除标签
     */
    void deleteTag(Long id);

    /**
     * 获取标签详情
     */
    AssetTagDTO getTagById(Long id);

    /**
     * 根据编码获取标签
     */
    AssetTagDTO getTagByCode(String tagCode);

    /**
     * 根据名称获取标签
     */
    AssetTagDTO getTagByName(String tagName);

    /**
     * 分页查询标签列表
     */
    IPage<AssetTagDTO> queryTags(String keyword, String category, Integer status, int pageNum, int pageSize);

    /**
     * 获取所有标签列表
     */
    List<AssetTagDTO> getAllTags();

    /**
     * 根据分类获取标签
     */
    List<AssetTagDTO> getTagsByCategory(String category);

    /**
     * 获取热门标签
     */
    List<AssetTagDTO> getHotTags(int limit);

    /**
     * 搜索标签
     */
    List<AssetTagDTO> searchTags(String keyword, int limit);

    /**
     * 为资产添加标签
     */
    void addTagToAsset(Long assetId, Long tagId, Long operatorId);

    /**
     * 为资产添加多个标签
     */
    void addTagsToAsset(Long assetId, List<Long> tagIds, Long operatorId);

    /**
     * 移除资产的标签
     */
    void removeTagFromAsset(Long assetId, Long tagId);

    /**
     * 移除资产的所有标签
     */
    void removeAllTagsFromAsset(Long assetId);

    /**
     * 获取资产的标签列表
     */
    List<AssetTagDTO> getTagsByAssetId(Long assetId);

    /**
     * 启用标签
     */
    void enableTag(Long id);

    /**
     * 禁用标签
     */
    void disableTag(Long id);

    /**
     * 获取标签分类列表
     */
    List<String> getTagCategories();
}
