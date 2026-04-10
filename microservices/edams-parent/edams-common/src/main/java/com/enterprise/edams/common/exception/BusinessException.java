package com.enterprise.edams.common.exception;

import com.enterprise.edams.common.result.ResultCode;
import lombok.Getter;

/**
 * 业务异常
 *
 * @author Architecture Team
 * @version 1.0.0
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 错误信息
     */
    private final String message;

    /**
     * 详细信息
     */
    private final String details;

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
        this.details = null;
    }

    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
        this.message = message;
        this.details = null;
    }

    public BusinessException(ResultCode resultCode, String message, String details) {
        super(message);
        this.code = resultCode.getCode();
        this.message = message;
        this.details = details;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
        this.details = null;
    }

    public BusinessException(Integer code, String message, String details) {
        super(message);
        this.code = code;
        this.message = message;
        this.details = details;
    }
}
