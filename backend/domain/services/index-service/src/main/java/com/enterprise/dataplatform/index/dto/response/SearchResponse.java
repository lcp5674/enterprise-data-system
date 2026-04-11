package com.enterprise.dataplatform.index.dto.response;

import com.enterprise.dataplatform.index.document.AssetIndexDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 搜索响应DTO
 *
 * @author Team-D
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse {

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页码
     */
    private Integer page;

    /**
     * 每页大小
     */
    private Integer size;

    /**
     * 总页数
     */
    private Integer totalPages;

    /**
     * 搜索结果列表
     */
    private List<AssetIndexDocument> results;

    /**
     * 聚合结果
     */
    private Map<String, AggregationBucket> aggregations;

    /**
     * 搜索耗时（毫秒）
     */
    private Long took;

    /**
     * 是否还有下一页
     */
    private Boolean hasMore;

    /**
     * 聚合桶
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AggregationBucket {
        /**
         * 桶名称/键
         */
        private String key;

        /**
         * 文档数量
         */
        private Long docCount;

        /**
         * 子聚合
         */
        private Map<String, AggregationBucket> subAggregations;
    }
}
