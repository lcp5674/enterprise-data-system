package com.enterprise.edams.analysis.llm;

import com.enterprise.edams.analysis.entity.LocalModelConfig;
import com.enterprise.edams.analysis.entity.ModelType;
import com.enterprise.edams.analysis.llm.impl.LocalAIConnector;
import com.enterprise.edams.analysis.llm.impl.OllamaConnector;
import com.enterprise.edams.analysis.llm.impl.OpenAICompatibleConnector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LLMConnectorFactory {

    private final OllamaConnector ollamaConnector;
    private final LocalAIConnector localAIConnector;
    private final OpenAICompatibleConnector openAICompatibleConnector;

    public LLMConnector getConnector(LocalModelConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Model config cannot be null");
        }

        LLMConnector connector;

        switch (config.getModelType()) {
            case OLLAMA:
                connector = ollamaConnector;
                ((OllamaConnector) connector).setBaseUrl(config.getBaseUrl());
                break;
            case LOCALAI:
                connector = localAIConnector;
                ((LocalAIConnector) connector).setBaseUrl(config.getBaseUrl());
                break;
            case OPENAI_COMPATIBLE:
                connector = openAICompatibleConnector;
                ((OpenAICompatibleConnector) connector).setBaseUrl(config.getBaseUrl());
                ((OpenAICompatibleConnector) connector).setApiKey(config.getApiKey());
                break;
            default:
                throw new IllegalArgumentException("Unsupported model type: " + config.getModelType());
        }

        log.debug("Created LLM connector for model type: {}, model: {}",
                config.getModelType(), config.getModelName());

        return connector;
    }

    public LLMConnector getConnectorByType(ModelType type, String baseUrl) {
        LLMConnector connector;

        switch (type) {
            case OLLAMA:
                connector = ollamaConnector;
                ((OllamaConnector) connector).setBaseUrl(baseUrl);
                break;
            case LOCALAI:
                connector = localAIConnector;
                ((LocalAIConnector) connector).setBaseUrl(baseUrl);
                break;
            case OPENAI_COMPATIBLE:
                connector = openAICompatibleConnector;
                ((OpenAICompatibleConnector) connector).setBaseUrl(baseUrl);
                break;
            default:
                throw new IllegalArgumentException("Unsupported model type: " + type);
        }

        return connector;
    }
}
