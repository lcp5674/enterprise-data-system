package com.enterprise.dataplatform.index.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 搜索请求DTO
 *
 * @author Team-D
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest {

    /**
     * 搜索关键词
     */
    private String keyword;

    /**
     * 对象类型：TABLE, FILE, API, MODEL, etc.
     */
    private String objectType;

    /**
     * 领域编码
     */
    private String domainCode;

    /**
     * 负责人
     */
    private String owner;

    /**
     * 敏感度等级
     */
    private String sensitivity;

    /**
     * 状态
     */
    private String status;

    /**
     * 业务分类
     */
    private String businessCategory;

    /**
     * 标签列表
     */
    private List<String> tags;

    /**
     * 页码（从0开始）
     */
    @Min(0)
    @Builder.Default
    private Integer page = 0;

    /**
     * 每页大小
     */
    @Min(1)
    @Max(100)
    @Builder.Default
    private Integer size = 20;

    /**
     * 排序字段
     */
    @Builder.Default
    private String sortBy = "updatedAt";

    /**
     * 排序方向：asc, desc
     */
    @Builder.Default
    private String sortOrder = "desc";

    /**
     * 是否高亮显示
     */
    @Builder.Default
    private Boolean highlight = true;

    /**
     * 聚合字段列表
     */
    private List<String> aggFields;
}
