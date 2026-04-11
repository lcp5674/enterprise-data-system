package com.enterprise.edams.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 模型统计VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelStatisticsVO {

    /**
     * 总模型数
     */
    private Long totalCount;

    /**
     * 按类型统计
     */
    private Map<String, Long> byType;

    /**
     * 按层级统计
     */
    private Map<String, Long> byLevel;

    /**
     * 按状态统计
     */
    private Map<String, Long> byStatus;

    /**
     * 实体总数
     */
    private Long entityCount;

    /**
     * 属性总数
     */
    private Long attributeCount;

    /**
     * 关系总数
     */
    private Long relationCount;

    /**
     * 本月新增
     */
    private Long monthlyNewCount;
}
