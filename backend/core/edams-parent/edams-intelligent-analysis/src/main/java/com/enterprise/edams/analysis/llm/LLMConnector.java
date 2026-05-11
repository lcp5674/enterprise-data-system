package com.enterprise.edams.analysis.llm;

import java.util.List;

public interface LLMConnector {

    LLMResponse generate(LLMRequest request);

    boolean testConnection();

    List<String> listModels();

    String getConnectorType();
}
