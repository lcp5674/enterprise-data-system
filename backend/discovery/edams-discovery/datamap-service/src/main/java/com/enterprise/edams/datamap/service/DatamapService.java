package com.enterprise.edams.datamap.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.enterprise.edams.datamap.dto.*;

import java.util.List;
import java.util.Map;

/**
 * 数据地图服务接口
 */
public interface DatamapService {

    /**
     * 创建数据资产元信息
     */
    Long createDataAsset(CreateDataAssetRequest request);

    /**
     * 更新数据资产元信息
     */
    boolean updateDataAsset(Long id, UpdateDataAssetRequest request);

    /**
     * 删除数据资产元信息
     */
    boolean deleteDataAsset(Long id);

    /**
     * 获取数据资产详情
     */
    DataAssetDetailVO getDataAssetDetail(Long id);

    /**
     * 分页查询数据资产列表
     */
    IPage<DataAssetVO> listDataAssets(DataAssetQueryDTO query);

    /**
     * 获取数据血缘关系
     */
    LineageGraph getLineage(Long assetId, String direction);

    /**
     * 获取影响分析
     */
    ImpactAnalysis getImpactAnalysis(Long assetId);

    /**
     * 查询数据流向
     */
    List<FlowPath> queryDataFlow(DataFlowQueryDTO query);

    /**
     * 获取字段血缘
     */
    FieldLineage getFieldLineage(Long assetId, String fieldName);

    /**
     * 创建血缘关系
     */
    Long createLineageRelation(CreateLineageRequest request);

    /**
     * 删除血缘关系
     */
    boolean deleteLineageRelation(Long id);

    /**
     * 生成数据流向图数据
     */
    Map<String, Object> generateFlowGraph(Long rootAssetId, Integer depth);

    /**
     * 获取数据资产统计
     */
    DataAssetStatisticsVO getStatistics();
}
