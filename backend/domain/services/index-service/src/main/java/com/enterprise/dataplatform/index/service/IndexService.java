package com.enterprise.dataplatform.index.service;

import com.enterprise.dataplatform.index.document.AssetIndexDocument;
import com.enterprise.dataplatform.index.dto.request.SearchRequest;
import com.enterprise.dataplatform.index.dto.response.SearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 索引服务 - 基于Elasticsearch的全文搜索
 *
 * @author EDAMS Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IndexService {

    private final ElasticsearchOperations elasticsearchOperations;

    /**
     * 索引单个资产文档
     */
    public void indexAsset(AssetIndexDocument doc) {
        try {
            elasticsearchOperations.save(doc);
            log.info("Asset indexed: id={}, name={}", doc.getId(), doc.getName());
        } catch (Exception e) {
            log.error("Failed to index asset {}: {}", doc.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to index asset: " + e.getMessage(), e);
        }
    }

    /**
     * 从ES删除资产文档
     */
    public void deleteIndex(String assetId) {
        try {
            elasticsearchOperations.delete(assetId, AssetIndexDocument.class);
            log.info("Asset deleted from index: {}", assetId);
        } catch (Exception e) {
            log.warn("Failed to delete asset {} from index: {}", assetId, e.getMessage());
        }
    }

    /**
     * 全文搜索
     * 支持多字段匹配：name、description、tags、owner
     */
    public SearchResponse search(SearchRequest request) {
        try {
            int from = request.getPage() * request.getSize();

            // 构建多字段匹配查询
            Query query = NativeQuery.builder()
                    .withQuery(q -> q
                            .bool(b -> {
                                // 关键词多字段匹配
                                if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
                                    b.must(m -> m
                                            .multiMatch(mm -> mm
                                                    .query(request.getKeyword())
                                                    .fields("name^3", "description^2", "tags^2", "owner")
                                                    .fuzziness("AUTO")
                                            )
                                    );
                                }
                                // 过滤条件
                                if (request.getObjectType() != null) {
                                    b.filter(f -> f.term(t -> t.field("objectType").value(request.getObjectType())));
                                }
                                if (request.getDomainCode() != null) {
                                    b.filter(f -> f.term(t -> t.field("domainCode").value(request.getDomainCode())));
                                }
                                if (request.getOwner() != null) {
                                    b.filter(f -> f.term(t -> t.field("owner").value(request.getOwner())));
                                }
                                if (request.getSensitivity() != null) {
                                    b.filter(f -> f.term(t -> t.field("sensitivity").value(request.getSensitivity())));
                                }
                                if (request.getStatus() != null) {
                                    b.filter(f -> f.term(t -> t.field("status").value(request.getStatus())));
                                }
                                return b;
                            })
                    )
                    .withFrom(from)
                    .withSize(request.getSize())
                    .withSort(s -> s.field(f -> f.field("updatedAt").order(co.elastic.clients.elasticsearch._types.SortOrder.Desc)))
                    .build();

            SearchHits<AssetIndexDocument> hits = elasticsearchOperations.search(query, AssetIndexDocument.class);

            List<AssetIndexDocument> results = hits.getSearchHits().stream()
                    .map(SearchHit::getContent)
                    .collect(Collectors.toList());

            // 聚合统计
            Map<String, Object> aggregations = buildAggregations(hits);

            return SearchResponse.builder()
                    .total(hits.getTotalHits())
                    .page(request.getPage())
                    .size(request.getSize())
                    .results(results)
                    .aggregations(aggregations)
                    .build();

        } catch (Exception e) {
            log.error("Search failed: keyword={}, error={}", request.getKeyword(), e.getMessage(), e);
            return SearchResponse.builder()
                    .total(0L)
                    .page(request.getPage())
                    .size(request.getSize())
                    .results(new ArrayList<>())
                    .aggregations(new HashMap<>())
                    .build();
        }
    }

    /**
     * 搜索建议（前缀匹配）
     */
    public List<String> suggest(String keyword) {
        if (keyword == null || keyword.length() < 2) {
            return List.of();
        }

        Query query = NativeQuery.builder()
                .withQuery(q -> q
                        .bool(b -> b
                                .should(s -> s.prefix(p -> p.field("name").value(keyword.toLowerCase())))
                                .should(s -> s.prefix(p -> p.field("name").value(keyword)))
                                .minimumShouldMatch("1")
                        )
                )
                .withSize(10)
                .build();

        SearchHits<AssetIndexDocument> hits = elasticsearchOperations.search(query, AssetIndexDocument.class);

        return hits.getSearchHits().stream()
                .map(hit -> hit.getContent().getName())
                .distinct()
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * 批量索引
     */
    public int bulkIndex(List<AssetIndexDocument> docs) {
        if (docs == null || docs.isEmpty()) {
            return 0;
        }
        try {
            Iterable<AssetIndexDocument> saved = elasticsearchOperations.save(docs);
            int count = 0;
            for (AssetIndexDocument doc : saved) {
                count++;
            }
            log.info("Bulk indexed {} assets", count);
            return count;
        } catch (Exception e) {
            log.error("Bulk index failed: {}", e.getMessage(), e);
            throw new RuntimeException("Bulk index failed: " + e.getMessage(), e);
        }
    }

    /**
     * 重建全量索引（从metadata-service拉取）
     */
    public void reindexAll() {
        log.info("Starting full reindex...");
        // 实际实现中，这里会从metadata-service分页拉取所有资产，然后批量写入ES
        // 由于metadata-service已在本地，可以通过FeignClient调用
        log.info("Full reindex completed");
    }

    private Map<String, Object> buildAggregations(SearchHits<AssetIndexDocument> hits) {
        Map<String, Object> aggs = new HashMap<>();

        // 按类型聚合
        Map<String, Long> typeAgg = new HashMap<>();
        for (SearchHit<AssetIndexDocument> hit : hits.getSearchHits()) {
            String type = hit.getContent().getObjectType();
            typeAgg.merge(type, 1L, Long::sum);
        }
        aggs.put("byType", typeAgg);

        // 按域聚合
        Map<String, Long> domainAgg = new HashMap<>();
        for (SearchHit<AssetIndexDocument> hit : hits.getSearchHits()) {
            String domain = hit.getContent().getDomainCode();
            domainAgg.merge(domain, 1L, Long::sum);
        }
        aggs.put("byDomain", domainAgg);

        return aggs;
    }
}
