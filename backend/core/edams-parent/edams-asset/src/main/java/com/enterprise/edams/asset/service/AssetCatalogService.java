package com.enterprise.edams.asset.service;

import com.enterprise.edams.asset.dto.AssetCatalogCreateRequest;
import com.enterprise.edams.asset.dto.AssetCatalogDTO;

import java.util.List;

/**
 * 资产目录服务接口
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
public interface AssetCatalogService {

    /**
     * 创建目录
     */
    AssetCatalogDTO createCatalog(AssetCatalogCreateRequest request, String operator);

    /**
     * 更新目录
     */
    AssetCatalogDTO updateCatalog(Long id, AssetCatalogCreateRequest request, String operator);

    /**
     * 删除目录
     */
    void deleteCatalog(Long id, String operator);

    /**
     * 获取目录详情
     */
    AssetCatalogDTO getCatalogById(Long id);

    /**
     * 根据编码获取目录
     */
    AssetCatalogDTO getCatalogByCode(String code);

    /**
     * 获取根目录列表
     */
    List<AssetCatalogDTO> getRootCatalogs();

    /**
     * 获取子目录列表
     */
    List<AssetCatalogDTO> getChildCatalogs(Long parentId);

    /**
     * 获取目录树
     */
    List<AssetCatalogDTO> getCatalogTree();

    /**
     * 获取完整目录树(包含所有层级)
     */
    List<AssetCatalogDTO> getFullCatalogTree();

    /**
     * 移动目录
     */
    void moveCatalog(Long id, Long newParentId, String operator);

    /**
     * 更新目录排序
     */
    void updateSortOrder(Long id, Integer sortOrder, String operator);

    /**
     * 启用目录
     */
    void enableCatalog(Long id, String operator);

    /**
     * 禁用目录
     */
    void disableCatalog(Long id, String operator);

    /**
     * 统计目录下资产数量
     */
    int countAssetsInCatalog(Long catalogId);
}
