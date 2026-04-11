package com.enterprise.edams.asset.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.enterprise.edams.asset.dto.AssetCreateRequest;
import com.enterprise.edams.asset.dto.AssetDTO;
import com.enterprise.edams.asset.dto.AssetQueryRequest;
import com.enterprise.edams.asset.dto.AssetUpdateRequest;
import com.enterprise.edams.common.enums.AssetStatus;

import java.util.List;

/**
 * 资产服务接口
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
public interface AssetService {

    /**
     * 创建资产
     */
    AssetDTO createAsset(AssetCreateRequest request, String operator);

    /**
     * 更新资产
     */
    AssetDTO updateAsset(Long id, AssetUpdateRequest request, String operator);

    /**
     * 删除资产
     */
    void deleteAsset(Long id, String operator);

    /**
     * 获取资产详情
     */
    AssetDTO getAssetById(Long id);

    /**
     * 根据资产编码获取资产
     */
    AssetDTO getAssetByCode(String assetCode);

    /**
     * 分页查询资产列表
     */
    IPage<AssetDTO> queryAssets(AssetQueryRequest request);

    /**
     * 搜索资产
     */
    List<AssetDTO> searchAssets(String keyword);

    /**
     * 更新资产状态
     */
    void updateAssetStatus(Long id, AssetStatus status, String operator);

    /**
     * 发布资产
     */
    void publishAsset(Long id, String operator);

    /**
     * 归档资产
     */
    void archiveAsset(Long id, String operator);

    /**
     * 获取目录下的资产列表
     */
    IPage<AssetDTO> getAssetsByCatalog(Long catalogId, int pageNum, int pageSize);

    /**
     * 获取业务域下的资产列表
     */
    IPage<AssetDTO> getAssetsByDomain(Long domainId, int pageNum, int pageSize);

    /**
     * 获取用户的资产列表
     */
    IPage<AssetDTO> getAssetsByOwner(Long ownerId, int pageNum, int pageSize);

    /**
     * 为资产打标签
     */
    void tagAsset(Long assetId, List<Long> tagIds, String operator);

    /**
     * 移除资产标签
     */
    void untagAsset(Long assetId, List<Long> tagIds);

    /**
     * 获取资产的标签列表
     */
    List<String> getAssetTags(Long assetId);

    /**
     * 统计用户资产数量
     */
    long countAssetsByOwner(Long ownerId);

    /**
     * 统计业务域资产数量
     */
    long countAssetsByDomain(Long domainId);

    /**
     * 同步资产元数据
     */
    void syncAssetMetadata(Long id, String operator);
}
