package com.enterprise.edams.llm.client.exception;

public class LLMAvailabilityException extends LLMException {

    public LLMAvailabilityException(String provider) {
        super(provider, "LLM Provider不可用: " + provider);
    }

    public LLMAvailabilityException(String provider, Throwable cause) {
        super(provider, "LLM Provider不可用: " + provider, cause);
    }
}
