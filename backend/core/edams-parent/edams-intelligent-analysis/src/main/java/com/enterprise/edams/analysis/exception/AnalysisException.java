package com.enterprise.edams.analysis.exception;

public class AnalysisException extends RuntimeException {

    private String errorCode;

    public AnalysisException(String message) {
        super(message);
    }

    public AnalysisException(String message, Throwable cause) {
        super(message, cause);
    }

    public AnalysisException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public AnalysisException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
