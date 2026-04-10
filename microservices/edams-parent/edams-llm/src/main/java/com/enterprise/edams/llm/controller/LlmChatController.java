package com.enterprise.edams.llm.controller;

import com.enterprise.edams.common.result.Result;
import com.enterprise.edams.llm.dto.ChatRequestDTO;
import com.enterprise.edams.llm.dto.ChatResponseDTO;
import com.enterprise.edams.llm.dto.ProviderDTO;
import com.enterprise.edams.llm.service.LlmChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import jakarta.validation.Valid;
import java.util.List;

/**
 * LLM聊天接口
 *
 * @author LLM Team
 * @version 1.0.0
 */
@Slf4j
@Tag(name = "LLM聊天", description = "大语言模型聊天接口")
@RestController
@RequestMapping("/api/v1/llm")
@RequiredArgsConstructor
public class LlmChatController {

    private final LlmChatService llmChatService;

    @Operation(summary = "聊天", description = "发送消息并获取LLM回复")
    @PostMapping("/chat")
    public Result<ChatResponseDTO> chat(@Valid @RequestBody ChatRequestDTO request) {
        log.info("收到聊天请求: userId={}", request.getUserId());
        ChatResponseDTO response = llmChatService.chat(request);
        return Result.success(response);
    }

    @Operation(summary = "流式聊天", description = "发送消息并获取流式LLM回复")
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatResponseDTO> chatStream(@Valid @RequestBody ChatRequestDTO request) {
        log.info("收到流式聊天请求: userId={}", request.getUserId());
        request.setStream(true);
        return llmChatService.chatStream(request);
    }

    @Operation(summary = "批量聊天", description = "批量发送消息并获取LLM回复")
    @PostMapping("/chat/batch")
    public Result<List<ChatResponseDTO>> batchChat(@RequestBody List<ChatRequestDTO> requests) {
        log.info("收到批量聊天请求: count={}", requests.size());
        List<ChatResponseDTO> responses = llmChatService.batchChat(requests);
        return Result.success(responses);
    }

    @Operation(summary = "获取提供商列表", description = "获取所有可用的LLM提供商")
    @GetMapping("/providers")
    public Result<List<ProviderDTO>> getProviders() {
        List<ProviderDTO> providers = llmChatService.getProviders();
        return Result.success(providers);
    }

    @Operation(summary = "获取模型列表", description = "获取指定提供商的可用模型列表")
    @GetMapping("/providers/{provider}/models")
    public Result<List<ProviderDTO.ModelDTO>> getModels(
            @Parameter(description = "提供商代码") @PathVariable String provider) {
        List<ProviderDTO.ModelDTO> models = llmChatService.getAvailableModels(provider);
        return Result.success(models);
    }

    @Operation(summary = "选择模型", description = "根据策略选择最佳模型")
    @GetMapping("/models/select")
    public Result<String> selectModel(
            @Parameter(description = "选择策略: AUTO, COST_OPTIMIZED, LATENCY_OPTIMIZED, QUALITY_FIRST")
            @RequestParam(defaultValue = "AUTO") String strategy) {
        String model = llmChatService.selectModel(strategy, java.util.Map.of());
        return Result.success(model);
    }
}
