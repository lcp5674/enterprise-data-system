package com.enterprise.edams.chatbot.service;

import com.enterprise.edams.chatbot.dto.ChatResponseDTO;
import reactor.core.publisher.Flux;

/**
 * LLM服务Feign客户端接口
 *
 * @author Chatbot Team
 * @version 1.0.0
 */
@org.springframework.cloud.openfeign.FeignClient(
        name = "edams-llm",
        url = "${chatbot.llm.service-url:http://localhost:8088}"
)
public interface LlmServiceClient {

    /**
     * 聊天
     *
     * @param userId 用户ID
     * @param message 消息内容
     * @param history 历史消息
     * @param maxHistorySize 最大历史大小
     * @return 聊天响应
     */
    @org.springframework.web.bind.annotation.PostMapping("/api/v1/llm/chat")
    ChatResponseDTO chat(
            @org.springframework.web.bind.annotation.RequestParam("userId") String userId,
            @org.springframework.web.bind.annotation.RequestParam("message") String message,
            @org.springframework.web.bind.annotation.RequestParam(value = "history", required = false) String history,
            @org.springframework.web.bind.annotation.RequestParam(value = "maxHistorySize", defaultValue = "10") int maxHistorySize
    );

    /**
     * 流式聊天
     *
     * @param userId 用户ID
     * @param message 消息内容
     * @param history 历史消息
     * @param maxHistorySize 最大历史大小
     * @return 流式响应
     */
    @org.springframework.web.bind.annotation.PostMapping(value = "/api/v1/llm/chat/stream", produces = org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<ChatResponseDTO> chatStream(
            @org.springframework.web.bind.annotation.RequestParam("userId") String userId,
            @org.springframework.web.bind.annotation.RequestParam("message") String message,
            @org.springframework.web.bind.annotation.RequestParam(value = "history", required = false) String history,
            @org.springframework.web.bind.annotation.RequestParam(value = "maxHistorySize", defaultValue = "10") int maxHistorySize
    );
}
