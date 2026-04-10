package com.enterprise.edams.common.exception;

import lombok.Getter;

/**
 * 业务异常
 *
 * @author Architecture Team
 * @version 1.0.0
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.toString());
        this.code = getCodeFromErrorCode(errorCode);
    }

    public BusinessException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    private static int getCodeFromErrorCode(ErrorCode errorCode) {
        try {
            return (int) errorCode.getClass().getField(errorCode.name()).get(null);
        } catch (Exception e) {
            return 1000;
        }
    }
}
