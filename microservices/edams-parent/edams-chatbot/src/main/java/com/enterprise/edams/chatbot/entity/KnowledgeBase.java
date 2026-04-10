package com.enterprise.edams.chatbot.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 知识库实体
 *
 * @author Chatbot Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("chat_knowledge_base")
public class KnowledgeBase extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 知识库ID
     */
    private String knowledgeBaseId;

    /**
     * 知识库名称
     */
    private String name;

    /**
     * 知识库描述
     */
    private String description;

    /**
     * 关联的图谱ID
     */
    private String graphId;

    /**
     * Milvus集合名称
     */
    private String collectionName;

    /**
     * 向量维度
     */
    private Integer dimension;

    /**
     * 文档数量
     */
    private Long documentCount;

    /**
     * 向量数量
     */
    private Long vectorCount;

    /**
     * 状态: ACTIVE-激活, INACTIVE-未激活
     */
    private String status;

    /**
     * 相似度阈值
     */
    private Double similarityThreshold;

    /**
     * 检索数量
     */
    private Integer topK;

    /**
     * 所属租户ID
     */
    private String tenantId;

    /**
     * 创建人
     */
    private String creator;

    /**
     * 配置信息 (JSON)
     */
    private String config;
}
