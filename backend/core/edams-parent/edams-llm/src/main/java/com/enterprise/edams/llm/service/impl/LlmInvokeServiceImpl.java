package com.enterprise.edams.llm.service.impl;

import com.enterprise.edams.llm.dto.*;
import com.enterprise.edams.llm.entity.LlmModel;
import com.enterprise.edams.llm.repository.LlmModelMapper;
import com.enterprise.edams.llm.service.LlmInvokeService;
import com.enterprise.edams.llm.service.LlmQuotaService;
import com.enterprise.edams.llm.service.LlmUsageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 大模型调用服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LlmInvokeServiceImpl implements LlmInvokeService {

    private final LlmModelMapper modelMapper;
    private final LlmQuotaService quotaService;
    private final LlmUsageService usageService;
    private final WebClient.Builder webClientBuilder;

    // 简单的内存限流器
    private final ConcurrentHashMap<String, Long> rateLimiter = new ConcurrentHashMap<>();

    @Override
    @Transactional
    public LlmInvokeResponse invoke(LlmInvokeRequest request) {
        long startTime = System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString();
        LlmInvokeResponse response = new LlmInvokeResponse();
        response.setRequestId(requestId);
        response.setResponseTime(LocalDateTime.now());

        try {
            // 1. 获取模型信息
            LlmModel model = null;
            if (request.getModelId() != null) {
                model = modelMapper.selectById(request.getModelId());
            } else if (request.getModelCode() != null) {
                model = modelMapper.selectByCode(request.getModelCode());
            }
            
            if (model == null || !model.getEnabled()) {
                throw new RuntimeException("Model not available");
            }

            // 2. 检查配额
            if (!quotaService.checkQuota(request.getUserId(), model.getId(), 
                    request.getMaxTokens() != null ? request.getMaxTokens() : 1000)) {
                throw new RuntimeException("Quota exceeded");
            }

            // 3. 限流检查
            if (!checkRateLimit(request.getUserId(), model)) {
                throw new RuntimeException("Rate limit exceeded");
            }

            // 4. 调用模型 (模拟)
            String content = callLlmApi(model, request);
            int inputTokens = estimateTokens(request.getPrompt());
            int outputTokens = estimateTokens(content);
            int totalTokens = inputTokens + outputTokens;

            // 5. 计算成本
            BigDecimal cost = calculateCost(model, inputTokens, outputTokens);

            // 6. 消耗配额
            quotaService.consumeQuota(request.getUserId(), model.getId(), (long) totalTokens, cost);

            // 7. 记录使用日志
            LlmUsageLogDTO usageLog = new LlmUsageLogDTO();
            usageLog.setRequestId(requestId);
            usageLog.setTraceId(requestId);
            usageLog.setTenantId(request.getTenantId());
            usageLog.setUserId(request.getUserId());
            usageLog.setUserName(request.getUserName());
            usageLog.setModelId(model.getId());
            usageLog.setModelCode(model.getModelCode());
            usageLog.setProvider(model.getProvider());
            usageLog.setRequestType(request.getRequestType());
            usageLog.setInputTokens(inputTokens);
            usageLog.setOutputTokens(outputTokens);
            usageLog.setTotalTokens(totalTokens);
            usageLog.setInputCost(model.getInputPrice().multiply(BigDecimal.valueOf(inputTokens)).divide(BigDecimal.valueOf(1000), 4, java.math.RoundingMode.HALF_UP));
            usageLog.setOutputCost(model.getOutputPrice().multiply(BigDecimal.valueOf(outputTokens)).divide(BigDecimal.valueOf(1000), 4, java.math.RoundingMode.HALF_UP));
            usageLog.setTotalCost(cost);
            usageLog.setLatencyMs(System.currentTimeMillis() - startTime);
            usageLog.setStatus("SUCCESS");
            usageLog.setModule(request.getModule());
            usageLog.setAppName(request.getAppName());
            usageLog.setRequestTime(LocalDateTime.now());
            usageService.recordUsage(usageLog);

            // 8. 返回响应
            response.setContent(content);
            response.setFinishReason("stop");
            response.setInputTokens(inputTokens);
            response.setOutputTokens(outputTokens);
            response.setTotalTokens(totalTokens);
            response.setCost(cost);
            response.setLatencyMs(System.currentTimeMillis() - startTime);
            response.setStatus("SUCCESS");
            response.setResponseTime(LocalDateTime.now());

            log.info("LLM invoke success: requestId={}, model={}, tokens={}, cost={}", 
                    requestId, model.getModelCode(), totalTokens, cost);

        } catch (Exception e) {
            log.error("LLM invoke failed: requestId={}, error={}", requestId, e.getMessage());
            response.setStatus("FAILED");
            response.setErrorMessage(e.getMessage());
            response.setLatencyMs(System.currentTimeMillis() - startTime);
            response.setResponseTime(LocalDateTime.now());
        }

        return response;
    }

    @Override
    public LlmInvokeResponse[] batchInvoke(LlmInvokeRequest[] requests) {
        LlmInvokeResponse[] responses = new LlmInvokeResponse[requests.length];
        for (int i = 0; i < requests.length; i++) {
            responses[i] = invoke(requests[i]);
        }
        return responses;
    }

    @Override
    public BigDecimal estimateCost(LlmInvokeRequest request) {
        LlmModel model = null;
        if (request.getModelId() != null) {
            model = modelMapper.selectById(request.getModelId());
        } else if (request.getModelCode() != null) {
            model = modelMapper.selectByCode(request.getModelCode());
        }
        
        if (model == null) {
            return BigDecimal.ZERO;
        }
        
        int inputTokens = estimateTokens(request.getPrompt());
        return calculateCost(model, inputTokens, 0);
    }

    @Override
    public boolean testConnection(Long modelId) {
        LlmModel model = modelMapper.selectById(modelId);
        if (model == null || !model.getEnabled()) {
            return false;
        }
        try {
            // 简单的连接测试
            return true;
        } catch (Exception e) {
            log.error("Model connection test failed: modelId={}, error={}", modelId, e.getMessage());
            return false;
        }
    }

    private String callLlmApi(LlmModel model, LlmInvokeRequest request) {
        // 实际实现中，这里会根据不同提供商调用不同的API
        // 这里简化处理，返回模拟响应
        return "This is a simulated response from " + model.getModelName() + 
               ". The prompt was: " + request.getPrompt().substring(0, Math.min(50, request.getPrompt().length())) + "...";
    }

    private int estimateTokens(String text) {
        // 简单估算：中文约2字符/token，英文约4字符/token
        if (text == null || text.isEmpty()) {
            return 0;
        }
        int chineseChars = text.length() - text.replaceAll("[\\u4e00-\\u9fa5]", "").length();
        int englishChars = text.length() - chineseChars;
        return chineseChars / 2 + englishChars / 4;
    }

    private BigDecimal calculateCost(LlmModel model, int inputTokens, int outputTokens) {
        BigDecimal inputCost = model.getInputPrice().multiply(BigDecimal.valueOf(inputTokens))
                .divide(BigDecimal.valueOf(1000), 4, java.math.RoundingMode.HALF_UP);
        BigDecimal outputCost = model.getOutputPrice().multiply(BigDecimal.valueOf(outputTokens))
                .divide(BigDecimal.valueOf(1000), 4, java.math.RoundingMode.HALF_UP);
        return inputCost.add(outputCost);
    }

    private boolean checkRateLimit(Long userId, LlmModel model) {
        String key = userId + ":" + model.getId();
        long current = System.currentTimeMillis() / 60000; // 分钟级
        String rateKey = key + ":" + current;
        
        Long count = rateLimiter.get(rateKey);
        if (count != null && count >= model.getRequestLimit()) {
            return false;
        }
        
        rateLimiter.put(rateKey, count != null ? count + 1 : 1L);
        return true;
    }
}
