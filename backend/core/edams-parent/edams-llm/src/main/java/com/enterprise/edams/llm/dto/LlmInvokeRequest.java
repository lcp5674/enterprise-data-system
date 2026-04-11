package com.enterprise.edams.llm.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * LLM调用请求DTO
 */
@Data
public class LlmInvokeRequest {
    private Long tenantId;
    private Long userId;
    private String userName;
    private Long modelId;
    private String modelCode;
    private String requestType;
    private String prompt;
    private List<Map<String, String>> messages;
    private Double temperature;
    private Integer maxTokens;
    private String module;
    private String appName;
    private Map<String, Object> extraParams;
}
