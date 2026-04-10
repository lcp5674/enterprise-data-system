package com.enterprise.edams.llm.service;

import com.enterprise.edams.llm.dto.ChatRequestDTO;
import com.enterprise.edams.llm.dto.ChatResponseDTO;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * LLM聊天服务接口
 *
 * @author LLM Team
 * @version 1.0.0
 */
public interface LlmChatService {

    /**
     * 同步聊天
     *
     * @param request 聊天请求
     * @return 聊天响应
     */
    ChatResponseDTO chat(ChatRequestDTO request);

    /**
     * 流式聊天
     *
     * @param request 聊天请求
     * @return 流式响应
     */
    Flux<ChatResponseDTO> chatStream(ChatRequestDTO request);

    /**
     * 批量聊天
     *
     * @param requests 请求列表
     * @return 响应列表
     */
    java.util.List<ChatResponseDTO> batchChat(java.util.List<ChatRequestDTO> requests);

    /**
     * 获取支持的模型列表
     *
     * @param provider 提供商代码
     * @return 模型列表
     */
    java.util.List<com.enterprise.edams.llm.dto.ProviderDTO.ModelDTO> getAvailableModels(String provider);

    /**
     * 获取提供商列表
     *
     * @return 提供商列表
     */
    java.util.List<com.enterprise.edams.llm.dto.ProviderDTO> getProviders();

    /**
     * 根据策略选择模型
     *
     * @param strategy 策略
     * @param requirements 需求
     * @return 选择的模型
     */
    String selectModel(String strategy, Map<String, Object> requirements);
}
