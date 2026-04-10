package com.enterprise.edams.chatbot.service;

import com.enterprise.edams.chatbot.dto.ChatResponseDTO;

import java.util.List;

/**
 * 知识检索服务接口
 *
 * @author Chatbot Team
 * @version 1.0.0
 */
public interface KnowledgeRetrievalService {

    /**
     * 检索相关知识
     *
     * @param query 查询文本
     * @param graphId 图谱ID
     * @param threshold 相似度阈值
     * @param topK 返回数量
     * @return 检索结果
     */
    List<ChatResponseDTO.ContextDTO> retrieve(String query, String graphId, 
                                               double threshold, int topK);

    /**
     * 从向量数据库检索
     *
     * @param query 查询文本
     * @param collection 集合名称
     * @param topK 返回数量
     * @return 检索结果
     */
    List<ChatResponseDTO.ContextDTO> searchByVector(String query, String collection, int topK);

    /**
     * 从图谱检索
     *
     * @param query 查询文本
     * @param graphId 图谱ID
     * @param topK 返回数量
     * @return 检索结果
     */
    List<ChatResponseDTO.ContextDTO> searchInGraph(String query, String graphId, int topK);

    /**
     * 生成查询向量
     *
     * @param text 文本
     * @return 向量
     */
    float[] generateEmbedding(String text);
}
