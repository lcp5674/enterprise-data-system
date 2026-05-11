package com.enterprise.edams.llm.client.exception;

public class LLMTimeoutException extends LLMException {

    private final long timeoutMs;

    public LLMTimeoutException(String provider, long timeoutMs) {
        super(provider, "LLM请求超时，Provider: " + provider + ", 超时时间: " + timeoutMs + "ms");
        this.timeoutMs = timeoutMs;
    }

    public LLMTimeoutException(String provider, long timeoutMs, Throwable cause) {
        super(provider, "LLM请求超时，Provider: " + provider + ", 超时时间: " + timeoutMs + "ms", cause);
        this.timeoutMs = timeoutMs;
    }

    public long getTimeoutMs() {
        return timeoutMs;
    }
}
