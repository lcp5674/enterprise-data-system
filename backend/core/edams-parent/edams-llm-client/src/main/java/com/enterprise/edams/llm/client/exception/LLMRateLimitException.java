package com.enterprise.edams.llm.client.exception;

public class LLMRateLimitException extends LLMException {

    private final long retryAfterMs;

    public LLMRateLimitException(String provider, long retryAfterMs) {
        super(provider, "LLM请求频率超限，Provider: " + provider + ", 请在 " + retryAfterMs + "ms 后重试");
        this.retryAfterMs = retryAfterMs;
    }

    public LLMRateLimitException(String provider, long retryAfterMs, Throwable cause) {
        super(provider, "LLM请求频率超限，Provider: " + provider + ", 请在 " + retryAfterMs + "ms 后重试", cause);
        this.retryAfterMs = retryAfterMs;
    }

    public long getRetryAfterMs() {
        return retryAfterMs;
    }
}
