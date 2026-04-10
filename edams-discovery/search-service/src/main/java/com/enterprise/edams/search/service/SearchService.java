package com.enterprise.edams.search.service;

import com.enterprise.edams.search.dto.*;

import java.util.List;

/**
 * 资产搜索服务接口
 */
public interface SearchService {

    /**
     * 全文检索
     */
    SearchResultVO search(SearchRequest request);

    /**
     * 多维度搜索
     */
    SearchResultVO multiDimensionSearch(MultiDimensionSearchRequest request);

    /**
     * 搜索建议/自动补全
     */
    List<String> getSuggestions(String keyword, Integer limit);

    /**
     * 获取热门搜索
     */
    List<HotSearchVO> getHotSearches(Integer limit);

    /**
     * 获取搜索历史
     */
    List<SearchHistoryVO> getSearchHistory(String userId, Integer limit);

    /**
     * 添加搜索历史
     */
    void addSearchHistory(String userId, String keyword);

    /**
     * 清除搜索历史
     */
    void clearSearchHistory(String userId);

    /**
     * 索引数据
     */
    void indexData(IndexDataRequest request);

    /**
     * 更新索引
     */
    void updateIndex(String assetId, Object data);

    /**
     * 删除索引
     */
    void deleteIndex(String assetId);

    /**
     * 批量重建索引
     */
    void rebuildIndex(List<String> assetIds);

    /**
     * 获取搜索统计
     */
    SearchStatisticsVO getStatistics();
}
