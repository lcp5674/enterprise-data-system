package com.enterprise.edams.analysis.entity;

public enum ModelType {
    OLLAMA("Ollama", "Ollama原生API"),
    LOCALAI("LocalAI", "LocalAI API"),
    OPENAI_COMPATIBLE("OpenAI Compatible", "OpenAI兼容接口");

    private final String displayName;
    private final String description;

    ModelType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
