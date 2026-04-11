package com.enterprise.edams.llm.service;

import com.enterprise.edams.llm.dto.LlmInvokeRequest;
import com.enterprise.edams.llm.dto.LlmInvokeResponse;

/**
 * 大模型调用服务接口
 */
public interface LlmInvokeService {

    /**
     * 调用大模型
     */
    LlmInvokeResponse invoke(LlmInvokeRequest request);

    /**
     * 批量调用
     */
    LlmInvokeResponse[] batchInvoke(LlmInvokeRequest[] requests);

    /**
     * 估算成本
     */
    java.math.BigDecimal estimateCost(LlmInvokeRequest request);

    /**
     * 测试模型连接
     */
    boolean testConnection(Long modelId);
}
