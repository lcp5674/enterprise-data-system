package com.enterprise.edams.llm.client;

import com.enterprise.edams.llm.client.model.ChatRequest;
import com.enterprise.edams.llm.client.model.LLMResponse;
import reactor.core.publisher.Flux;

import java.util.List;

public interface LLMClient {

    LLMResponse chat(ChatRequest request);

    Flux<String> streamChat(ChatRequest request);

    List<String> getSupportedModels();

    boolean healthCheck();

    String getProvider();
}
