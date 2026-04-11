package com.enterprise.edams.datasource.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class DatasourceExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(DatasourceException.class)
    public ResponseEntity<Map<String, Object>> handleDatasourceException(DatasourceException ex) {
        log.error("数据源业务异常: {}", ex.getMessage(), ex);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("code", ex.getErrorCode() != null ? ex.getErrorCode() : "DATASOURCE_ERROR");
        result.put("message", ex.getMessage());
        result.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        log.error("参数校验异常", ex);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("code", "VALIDATION_ERROR");
        result.put("timestamp", LocalDateTime.now());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        result.put("errors", errors);
        result.put("message", "参数校验失败");
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    /**
     * 处理通用异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception ex) {
        log.error("系统异常", ex);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("code", "SYSTEM_ERROR");
        result.put("message", "系统异常: " + ex.getMessage());
        result.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }
}
