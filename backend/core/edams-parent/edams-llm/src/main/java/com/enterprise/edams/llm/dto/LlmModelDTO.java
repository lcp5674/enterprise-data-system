package com.enterprise.edams.llm.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 大模型DTO
 */
@Data
public class LlmModelDTO {
    private Long id;
    private String modelCode;
    private String modelName;
    private String provider;
    private String providerName;
    private String modelType;
    private String description;
    private String apiVersion;
    private String apiEndpoint;
    private BigDecimal inputPrice;
    private BigDecimal outputPrice;
    private Integer maxContextLength;
    private Integer maxOutputLength;
    private String capabilities;
    private String configParams;
    private Integer requestLimit;
    private Integer concurrentLimit;
    private Boolean enabled;
    private Integer priority;
    private String status;
    private String remark;
}
