package com.enterprise.edams.llm.client.exception;

public class LLMException extends RuntimeException {

    private final String provider;
    private final String errorCode;

    public LLMException(String message) {
        super(message);
        this.provider = null;
        this.errorCode = null;
    }

    public LLMException(String message, Throwable cause) {
        super(message, cause);
        this.provider = null;
        this.errorCode = null;
    }

    public LLMException(String provider, String message) {
        super(message);
        this.provider = provider;
        this.errorCode = null;
    }

    public LLMException(String provider, String message, String errorCode) {
        super(message);
        this.provider = provider;
        this.errorCode = errorCode;
    }

    public LLMException(String provider, String message, Throwable cause) {
        super(message, cause);
        this.provider = provider;
        this.errorCode = null;
    }

    public String getProvider() {
        return provider;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
