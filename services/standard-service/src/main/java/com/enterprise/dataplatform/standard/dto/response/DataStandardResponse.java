package com.enterprise.dataplatform.standard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 数据标准响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataStandardResponse {

    /**
     * 标准ID
     */
    private Long id;

    /**
     * 标准编码
     */
    private String standardCode;

    /**
     * 标准名称
     */
    private String standardName;

    /**
     * 标准描述
     */
    private String description;

    /**
     * 标准类别
     */
    private String category;

    /**
     * 标准类型
     */
    private String standardType;

    /**
     * 标准规则内容
     */
    private String ruleContent;

    /**
     * 数据类型
     */
    private String dataType;

    /**
     * 取值范围
     */
    private String valueRange;

    /**
     * 精度要求
     */
    private Integer precisionRequired;

    /**
     * 最大长度
     */
    private Integer maxLength;

    /**
     * 是否必填
     */
    private Boolean required;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 标准状态
     */
    private String status;

    /**
     * 优先级
     */
    private String priority;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 标准来源
     */
    private String source;

    /**
     * 外部参考编号
     */
    private String externalRef;

    /**
     * 适用范围
     */
    private String applicableScope;

    /**
     * 违规处理建议
     */
    private String violationHandling;

    /**
     * 创建人
     */
    private String creator;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private String updater;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 映射数量
     */
    private Integer mappingCount;

    /**
     * 合规率
     */
    private Double complianceRate;

    /**
     * 版本历史
     */
    private List<StandardVersionResponse> versionHistory;

    /**
     * 映射列表
     */
    private List<StandardMappingResponse> mappings;
}
