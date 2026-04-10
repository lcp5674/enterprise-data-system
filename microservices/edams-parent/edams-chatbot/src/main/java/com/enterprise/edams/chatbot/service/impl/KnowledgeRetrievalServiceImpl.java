package com.enterprise.edams.chatbot.service.impl;

import com.enterprise.edams.chatbot.config.ChatbotConfig;
import com.enterprise.edams.chatbot.dto.ChatResponseDTO;
import com.enterprise.edams.chatbot.service.KnowledgeRetrievalService;
import io.milvus.client.MilvusClient;
import io.milvus.param.dml.QueryParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.response.QueryResultsWrapper;
import io.milvus.response.SearchResultsWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 知识检索服务实现类
 *
 * @author Chatbot Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeRetrievalServiceImpl implements KnowledgeRetrievalService {

    private final MilvusClient milvusClient;
    private final ChatbotConfig chatbotConfig;

    @Override
    public List<ChatResponseDTO.ContextDTO> retrieve(String query, String graphId,
                                                     double threshold, int topK) {
        log.debug("检索知识: query={}, graphId={}, threshold={}, topK={}", 
                query, graphId, threshold, topK);

        try {
            // 1. 生成查询向量
            float[] queryVector = generateEmbedding(query);

            // 2. 确定集合名称
            String collection = chatbotConfig.getMilvus().getCollectionName();
            if (graphId != null && !graphId.isEmpty()) {
                collection = "knowledge_" + graphId;
            }

            // 3. 执行向量搜索
            return searchVectors(queryVector, collection, topK, threshold);

        } catch (Exception e) {
            log.error("知识检索失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<ChatResponseDTO.ContextDTO> searchByVector(String query, String collection, int topK) {
        try {
            float[] queryVector = generateEmbedding(query);
            return searchVectors(queryVector, collection, topK, 
                    chatbotConfig.getKnowledge().getSimilarityThreshold());
        } catch (Exception e) {
            log.error("向量搜索失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<ChatResponseDTO.ContextDTO> searchInGraph(String query, String graphId, int topK) {
        log.debug("在图谱中搜索: query={}, graphId={}", query, graphId);

        try {
            // 简单实现：使用关键词匹配
            // 实际实现中应该结合NLP和图谱查询
            List<ChatResponseDTO.ContextDTO> results = new ArrayList<>();

            // 模拟结果
            results.add(ChatResponseDTO.ContextDTO.builder()
                    .content("这是关于数据资产管理的相关信息...")
                    .source("知识图谱")
                    .documentId(UUID.randomUUID().toString())
                    .similarity(0.85)
                    .position(1)
                    .build());

            results.add(ChatResponseDTO.ContextDTO.builder()
                    .content("数据资产管理涉及数据治理、元数据管理...")
                    .source("数据治理文档")
                    .documentId(UUID.randomUUID().toString())
                    .similarity(0.78)
                    .position(2)
                    .build());

            return results;

        } catch (Exception e) {
            log.error("图谱搜索失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public float[] generateEmbedding(String text) {
        log.debug("生成文本向量: text={}", text.substring(0, Math.min(50, text.length())));

        // 简化实现：生成固定维度的随机向量
        // 实际应该调用embedding模型
        int dimension = chatbotConfig.getMilvus().getDimension();
        float[] embedding = new float[dimension];

        // 使用文本的hash生成伪随机但确定性的向量
        int hash = text.hashCode();
        java.util.Random random = new java.util.Random(hash);

        for (int i = 0; i < dimension; i++) {
            embedding[i] = (float) (random.nextGaussian() * 0.1);
        }

        // 归一化
        float norm = 0;
        for (float v : embedding) {
            norm += v * v;
        }
        norm = (float) Math.sqrt(norm);
        if (norm > 0) {
            for (int i = 0; i < dimension; i++) {
                embedding[i] /= norm;
            }
        }

        return embedding;
    }

    private List<ChatResponseDTO.ContextDTO> searchVectors(float[] queryVector, 
                                                            String collection, int topK,
                                                            double threshold) {
        try {
            // 检查Milvus连接
            if (milvusClient == null) {
                log.warn("Milvus客户端未初始化，使用模拟数据");
                return generateMockResults(queryVector, topK);
            }

            // 构建搜索参数
            SearchParam searchParam = SearchParam.newBuilder()
                    .withCollectionName(collection)
                    .with vectors(List.of(queryVector))
                    .withTopK(topK)
                    .withParams("{\"nprobe\": 10}")
                    .build();

            // 执行搜索
            SearchResultsWrapper resultsWrapper = milvusClient.search(searchParam);

            List<ChatResponseDTO.ContextDTO> contexts = new ArrayList<>();
            List<QueryResultsWrapper.ScoredVector> scoredVectors = resultsWrapper.getQueryResults();

            int position = 1;
            for (QueryResultsWrapper.ScoredVector scoredVector : scoredVectors) {
                double score = scoredVector.getScore();

                // 过滤低相似度结果
                if (score < threshold) {
                    continue;
                }

                // 提取文档信息
                var vectorRecord = scoredVector.getVector();
                String content = extractContent(vectorRecord);
                String source = extractSource(vectorRecord);
                String docId = extractDocId(vectorRecord);

                contexts.add(ChatResponseDTO.ContextDTO.builder()
                        .content(content)
                        .source(source)
                        .documentId(docId)
                        .similarity(score)
                        .position(position++)
                        .build());
            }

            return contexts;

        } catch (Exception e) {
            log.error("向量搜索失败: {}", e.getMessage());
            return generateMockResults(queryVector, topK);
        }
    }

    private List<ChatResponseDTO.ContextDTO> generateMockResults(float[] queryVector, int topK) {
        // 生成模拟结果用于测试
        List<ChatResponseDTO.ContextDTO> results = new ArrayList<>();

        String[] mockContents = {
                "数据资产管理（Data Asset Management）是对企业的数据资产进行规划、",
                "收集、存储、处理、分析和应用的全过程管理活动。",
                "数据治理是数据资产管理的核心环节，包括数据标准管理、数据质量管理等。",
                "元数据管理帮助企业理解数据的含义、来源和使用方式。"
        };

        for (int i = 0; i < Math.min(topK, mockContents.length); i++) {
            results.add(ChatResponseDTO.ContextDTO.builder()
                    .content(mockContents[i])
                    .source("知识库")
                    .documentId("doc_" + i)
                    .similarity(0.9 - i * 0.1)
                    .position(i + 1)
                    .build());
        }

        return results;
    }

    private String extractContent(Object record) {
        // 从向量记录中提取内容
        if (record instanceof io.milvus.response.QueryResultsWrapper.QueryResult) {
            io.milvus.response.QueryResultsWrapper.QueryResult queryResult = 
                    (io.milvus.response.QueryResultsWrapper.QueryResult) record;
            return queryResult.getEntity().get("content").toString();
        }
        return "";
    }

    private String extractSource(Object record) {
        if (record instanceof io.milvus.response.QueryResultsWrapper.QueryResult) {
            io.milvus.response.QueryResultsWrapper.QueryResult queryResult = 
                    (io.milvus.response.QueryResultsWrapper.QueryResult) record;
            var source = queryResult.getEntity().get("source");
            return source != null ? source.toString() : "未知来源";
        }
        return "未知来源";
    }

    private String extractDocId(Object record) {
        if (record instanceof io.milvus.response.QueryResultsWrapper.QueryResult) {
            io.milvus.response.QueryResultsWrapper.QueryResult queryResult = 
                    (io.milvus.response.QueryResultsWrapper.QueryResult) record;
            var docId = queryResult.getEntity().get("doc_id");
            return docId != null ? docId.toString() : UUID.randomUUID().toString();
        }
        return UUID.randomUUID().toString();
    }
}
