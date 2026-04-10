package com.enterprise.dataplatform.governance.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * AI推荐记录实体
 * 记录AI智能推荐的结果
 */
@Entity
@Table(name = "ai_recommendation", indexes = {
    @Index(name = "idx_recommendation_type", columnList = "recommendationType"),
    @Index(name = "idx_recommendation_status", columnList = "status"),
    @Index(name = "idx_recommendation_time", columnList = "recommendationTime")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 推荐编码
     */
    @Column(name = "recommendation_code", nullable = false, unique = true, length = 64)
    private String recommendationCode;

    /**
     * 推荐类型：GOVERNANCE_SUGGESTION、QUALITY_IMPROVEMENT、STANDARD_MAPPING、RISK_ALERT、OPTIMIZATION
     */
    @Column(name = "recommendation_type", nullable = false, length = 32)
    private String recommendationType;

    /**
     * 推荐标题
     */
    @Column(name = "title", nullable = false, length = 256)
    private String title;

    /**
     * 推荐内容
     */
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    /**
     * 推荐详情（JSON格式）
     */
    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    /**
     * 推荐理由
     */
    @Column(name = "reasoning", columnDefinition = "TEXT")
    private String reasoning;

    /**
     * 置信度（0-100）
     */
    @Column(name = "confidence")
    private Double confidence;

    /**
     * 关联的数据资产ID
     */
    @Column(name = "asset_id", length = 64)
    private String assetId;

    /**
     * 关联的数据资产名称
     */
    @Column(name = "asset_name", length = 256)
    private String assetName;

    /**
     * 关联的实体类型
     */
    @Column(name = "entity_type", length = 32)
    private String entityType;

    /**
     * 关联的实体ID
     */
    @Column(name = "entity_id", length = 64)
    private String entityId;

    /**
     * 推荐状态：PENDING、ACCEPTED、REJECTED、EXPIRED
     */
    @Column(name = "status", nullable = false, length = 32)
    private String status;

    /**
     * 处理人
     */
    @Column(name = "handler", length = 64)
    private String handler;

    /**
     * 处理时间
     */
    @Column(name = "handle_time")
    private LocalDateTime handleTime;

    /**
     * 处理意见
     */
    @Column(name = "handle_comment", columnDefinition = "TEXT")
    private String handleComment;

    /**
     * 推荐时间
     */
    @Column(name = "recommendation_time", nullable = false)
    private LocalDateTime recommendationTime;

    /**
     * 推荐模型
     */
    @Column(name = "model", length = 64)
    private String model;

    /**
     * 向量ID（用于向量检索）
     */
    @Column(name = "vector_id", length = 128)
    private String vectorId;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    /**
     * 相似推荐列表（JSON格式）
     */
    @Column(name = "similar_recommendations", columnDefinition = "TEXT")
    private String similarRecommendations;
}
