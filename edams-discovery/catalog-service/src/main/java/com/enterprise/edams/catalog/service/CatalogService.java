package com.enterprise.edams.catalog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.enterprise.edams.catalog.dto.*;

import java.util.List;

/**
 * 数据目录服务接口
 */
public interface CatalogService {

    /**
     * 创建目录
     */
    Long createCatalog(CreateCatalogRequest request);

    /**
     * 更新目录
     */
    boolean updateCatalog(Long id, UpdateCatalogRequest request);

    /**
     * 删除目录
     */
    boolean deleteCatalog(Long id);

    /**
     * 获取目录详情
     */
    CatalogDetailVO getCatalogDetail(Long id);

    /**
     * 分页查询目录列表
     */
    IPage<CatalogVO> listCatalogs(CatalogQueryDTO query);

    /**
     * 获取目录树
     */
    List<CatalogTreeNode> getCatalogTree();

    /**
     * 获取目录完整路径
     */
    String getCatalogPath(Long id);

    /**
     * 移动目录
     */
    boolean moveCatalog(Long id, Long parentId);

    /**
     * 获取子目录列表
     */
    List<CatalogVO> getChildren(Long parentId);

    /**
     * 统计目录下的资产数量
     */
    Long countAssets(Long catalogId);

    /**
     * 获取目录统计信息
     */
    CatalogStatisticsVO getStatistics();
}
