package com.enterprise.edams.llm.service.impl;

import com.enterprise.edams.llm.config.LlmConfig;
import com.enterprise.edams.llm.dto.ChatRequestDTO;
import com.enterprise.edams.llm.dto.ChatResponseDTO;
import com.enterprise.edams.llm.dto.ProviderDTO;
import com.enterprise.edams.llm.service.LlmChatService;
import com.enterprise.edams.llm.service.QuotaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * LLM聊天服务实现类
 *
 * @author LLM Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LlmChatServiceImpl implements LlmChatService {

    private final LlmConfig llmConfig;
    private final QuotaService quotaService;
    private final ObjectMapper objectMapper;

    // 简单的模型映射，实际应该从数据库或配置获取
    private final Map<String, ChatClient> chatClients = new HashMap<>();

    @Override
    @CircuitBreaker(name = "llm", fallbackMethod = "chatFallback")
    @Retry(name = "llm")
    @RateLimiter(name = "llm")
    public ChatResponseDTO chat(ChatRequestDTO request) {
        log.info("处理聊天请求: userId={}, strategy={}, model={}",
                request.getUserId(), request.getStrategy(), request.getModel());

        long startTime = System.currentTimeMillis();

        try {
            // 选择模型
            String model = selectModel(request.getStrategy(), Map.of(
                    "temperature", request.getTemperature(),
                    "maxTokens", request.getMaxTokens()
            ));

            if (request.getModel() != null) {
                model = request.getModel();
            }

            // 构建提示
            Prompt prompt = buildPrompt(request);

            // 调用LLM (这里使用OpenAI作为示例)
            ChatResponse response = callLlm(model, prompt);

            // 计算Token和费用
            int inputTokens = estimateInputTokens(request);
            int outputTokens = estimateOutputTokens(response);
            BigDecimal cost = calculateCost(model, inputTokens, outputTokens);

            // 消耗配额
            quotaService.consumeQuota(request.getUserId(), getProviderByModel(model),
                    inputTokens, outputTokens, cost);

            return ChatResponseDTO.builder()
                    .sessionId(request.getSessionId())
                    .messageId(UUID.randomUUID().toString())
                    .content(response.getResult().getOutput().getContent())
                    .finishReason(response.getResult().getFinishReason().name())
                    .model(model)
                    .provider(getProviderByModel(model))
                    .inputTokens(inputTokens)
                    .outputTokens(outputTokens)
                    .totalTokens(inputTokens + outputTokens)
                    .inputCost(cost.multiply(BigDecimal.valueOf(0.3)))
                    .outputCost(cost.multiply(BigDecimal.valueOf(0.7)))
                    .totalCost(cost)
                    .latency(System.currentTimeMillis() - startTime)
                    .timestamp(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("聊天请求处理失败", e);
            return ChatResponseDTO.builder()
                    .sessionId(request.getSessionId())
                    .messageId(UUID.randomUUID().toString())
                    .error(e.getMessage())
                    .latency(System.currentTimeMillis() - startTime)
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    @Override
    public Flux<ChatResponseDTO> chatStream(ChatRequestDTO request) {
        log.info("处理流式聊天请求: userId={}", request.getUserId());

        return Flux.create(emitter -> {
            try {
                String model = selectModel(request.getStrategy(), Map.of());
                if (request.getModel() != null) {
                    model = request.getModel();
                }

                Prompt prompt = buildPrompt(request);

                // 流式调用
                ChatClient streamingClient = chatClients.get(model);
                if (streamingClient == null) {
                    streamingClient = createChatClient(model);
                }

                StringBuilder fullContent = new StringBuilder();
                AtomicInteger tokenCount = new AtomicInteger(0);

                // 模拟流式响应
                ChatResponse response = streamingClient.call(prompt);
                String content = response.getResult().getOutput().getContent();

                // 分段发送
                String[] chunks = content.split("(?<=\\G.{50})");
                for (String chunk : chunks) {
                    if (emitter.isCancelled()) {
                        break;
                    }

                    emitter.next(ChatResponseDTO.builder()
                            .sessionId(request.getSessionId())
                            .content(chunk)
                            .model(model)
                            .provider(getProviderByModel(model))
                            .timestamp(LocalDateTime.now())
                            .build());

                    fullContent.append(chunk);
                    tokenCount.addAndGet(chunk.length() / 4);
                }

                // 发送完成信号
                emitter.next(ChatResponseDTO.builder()
                        .sessionId(request.getSessionId())
                        .content("")
                        .finishReason("STOP")
                        .model(model)
                        .outputTokens(tokenCount.get())
                        .totalTokens(tokenCount.get() + estimateInputTokens(request))
                        .timestamp(LocalDateTime.now())
                        .build());

                emitter.complete();

            } catch (Exception e) {
                log.error("流式聊天处理失败", e);
                emitter.error(e);
            }
        });
    }

    @Override
    public List<ChatResponseDTO> batchChat(List<ChatRequestDTO> requests) {
        log.info("批量处理聊天请求: count={}", requests.size());

        return requests.stream()
                .map(this::chat)
                .toList();
    }

    @Override
    public List<ProviderDTO.ModelDTO> getAvailableModels(String provider) {
        LlmConfig.ProviderConfig providerConfig = llmConfig.getProviders().get(provider.toLowerCase());
        if (providerConfig == null) {
            return List.of();
        }

        return providerConfig.getModels().stream()
                .map(model -> ProviderDTO.ModelDTO.builder()
                        .code(model.getName())
                        .name(model.getName())
                        .displayName(model.getDisplayName())
                        .maxTokens(model.getMaxTokens())
                        .inputPrice(model.getInputPrice())
                        .outputPrice(model.getOutputPrice())
                        .streamingSupported(true)
                        .functionCallSupported(false)
                        .visionSupported(false)
                        .status("ENABLED")
                        .build())
                .toList();
    }

    @Override
    public List<ProviderDTO> getProviders() {
        return llmConfig.getProviders().entrySet().stream()
                .filter(e -> e.getValue().getEnabled())
                .map(e -> {
                    LlmConfig.ProviderConfig config = e.getValue();
                    return ProviderDTO.builder()
                            .code(e.getKey())
                            .name(e.getKey().toUpperCase())
                            .status("ENABLED")
                            .priority(config.getPriority())
                            .baseUrl(config.getBaseUrl())
                            .models(getAvailableModels(e.getKey()))
                            .defaultModel(config.getModels().isEmpty() ? null :
                                    config.getModels().get(0).getName())
                            .build();
                })
                .sorted(Comparator.comparingInt(ProviderDTO::getPriority))
                .toList();
    }

    @Override
    public String selectModel(String strategy, Map<String, Object> requirements) {
        List<ProviderDTO> providers = getProviders();
        if (providers.isEmpty()) {
            return "gpt-3.5-turbo";
        }

        return switch (strategy.toUpperCase()) {
            case "COST_OPTIMIZED" -> selectCostOptimizedModel(providers);
            case "LATENCY_OPTIMIZED" -> selectLatencyOptimizedModel(providers);
            case "QUALITY_FIRST" -> selectQualityFirstModel(providers);
            default -> selectBalancedModel(providers);
        };
    }

    private String selectCostOptimizedModel(List<ProviderDTO> providers) {
        return providers.stream()
                .filter(p -> p.getModels() != null && !p.getModels().isEmpty())
                .flatMap(p -> p.getModels().stream())
                .filter(m -> m.getInputPrice() != null)
                .min(Comparator.comparing(ProviderDTO.ModelDTO::getInputPrice))
                .map(ProviderDTO.ModelDTO::getCode)
                .orElse("gpt-3.5-turbo");
    }

    private String selectLatencyOptimizedModel(List<ProviderDTO> providers) {
        // 优先选择响应速度快的模型
        return providers.stream()
                .filter(p -> "openai".equals(p.getCode()))
                .findFirst()
                .map(p -> p.getModels().isEmpty() ? "gpt-3.5-turbo" : p.getModels().get(0).getCode())
                .orElse("gpt-3.5-turbo");
    }

    private String selectQualityFirstModel(List<ProviderDTO> providers) {
        return providers.stream()
                .filter(p -> p.getModels() != null && !p.getModels().isEmpty())
                .flatMap(p -> p.getModels().stream())
                .filter(m -> m.getMaxTokens() != null && m.getMaxTokens() > 30000)
                .max(Comparator.comparing(ProviderDTO.ModelDTO::getMaxTokens))
                .map(ProviderDTO.ModelDTO::getCode)
                .orElse("gpt-4-turbo-preview");
    }

    private String selectBalancedModel(List<ProviderDTO> providers) {
        // 均衡选择
        return providers.stream()
                .filter(p -> "openai".equals(p.getCode()))
                .findFirst()
                .map(p -> p.getModels().stream()
                        .filter(m -> "gpt-4-turbo-preview".equals(m.getCode()))
                        .findFirst()
                        .map(ProviderDTO.ModelDTO::getCode)
                        .orElse(p.getModels().isEmpty() ? "gpt-4-turbo-preview" : p.getModels().get(0).getCode()))
                .orElse("gpt-4-turbo-preview");
    }

    private Prompt buildPrompt(ChatRequestDTO request) {
        List<Message> messages = new ArrayList<>();

        // 添加系统提示
        if (request.getSystemPrompt() != null && !request.getSystemPrompt().isEmpty()) {
            messages.add(new SystemMessage(request.getSystemPrompt()));
        }

        // 添加历史消息
        if (request.getHistory() != null) {
            for (ChatRequestDTO.ChatMessage msg : request.getHistory()) {
                messages.add(new UserMessage(msg.getContent()));
            }
        }

        // 添加当前消息
        messages.add(new UserMessage(request.getMessage()));

        return Prompt.builder()
                .messages(messages)
                .temperature(request.getTemperature() != null ? request.getTemperature() : 0.7)
                .maxTokens(request.getMaxTokens() != null ? request.getMaxTokens() : 2000)
                .build();
    }

    private ChatResponse callLlm(String model, Prompt prompt) {
        // 根据模型创建对应的客户端
        ChatClient client = chatClients.computeIfAbsent(model, this::createChatClient);
        return client.call(prompt);
    }

    private ChatClient createChatClient(String model) {
        // 根据模型选择对应的客户端
        // 这里应该根据配置动态创建不同提供商的客户端
        // 简化实现，使用OpenAI客户端
        log.info("创建ChatClient for model: {}", model);
        
        // 返回一个简单的实现
        return new OpenAiChatClient(
                org.springframework.ai.openai.OpenAiApi.builder()
                        .baseUrl("https://api.openai.com")
                        .apiKey("dummy")
                        .build()
        );
    }

    private int estimateInputTokens(ChatRequestDTO request) {
        // 简单估算，实际应该使用tokenizer
        int length = request.getMessage().length();
        if (request.getSystemPrompt() != null) {
            length += request.getSystemPrompt().length();
        }
        if (request.getHistory() != null) {
            for (ChatRequestDTO.ChatMessage msg : request.getHistory()) {
                length += msg.getContent().length();
            }
        }
        return length / 4;
    }

    private int estimateOutputTokens(ChatResponse response) {
        String content = response.getResult().getOutput().getContent();
        return content != null ? content.length() / 4 : 0;
    }

    private BigDecimal calculateCost(String model, int inputTokens, int outputTokens) {
        // 获取模型价格
        LlmConfig.ModelConfig modelConfig = findModelConfig(model);
        if (modelConfig == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal inputCost = modelConfig.getInputPrice()
                .multiply(BigDecimal.valueOf(inputTokens))
                .divide(BigDecimal.valueOf(1000), 6, BigDecimal.ROUND_HALF_UP);

        BigDecimal outputCost = modelConfig.getOutputPrice()
                .multiply(BigDecimal.valueOf(outputTokens))
                .divide(BigDecimal.valueOf(1000), 6, BigDecimal.ROUND_HALF_UP);

        return inputCost.add(outputCost);
    }

    private LlmConfig.ModelConfig findModelConfig(String model) {
        for (LlmConfig.ProviderConfig provider : llmConfig.getProviders().values()) {
            for (LlmConfig.ModelConfig modelConfig : provider.getModels()) {
                if (modelConfig.getName().equals(model)) {
                    return modelConfig;
                }
            }
        }
        return null;
    }

    private String getProviderByModel(String model) {
        for (Map.Entry<String, LlmConfig.ProviderConfig> entry : llmConfig.getProviders().entrySet()) {
            for (LlmConfig.ModelConfig modelConfig : entry.getValue().getModels()) {
                if (modelConfig.getName().equals(model)) {
                    return entry.getKey().toUpperCase();
                }
            }
        }
        return "OPENAI";
    }

    // Fallback method
    public ChatResponseDTO chatFallback(ChatRequestDTO request, Exception e) {
        log.error("LLM服务熔断降级: {}", e.getMessage());
        return ChatResponseDTO.builder()
                .sessionId(request.getSessionId())
                .messageId(UUID.randomUUID().toString())
                .error("服务暂时不可用，请稍后重试")
                .timestamp(LocalDateTime.now())
                .build();
    }
}
