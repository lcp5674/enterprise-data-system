package com.enterprise.dataplatform.iot.controller;

import com.enterprise.dataplatform.iot.dto.ApiResponse;
import com.enterprise.dataplatform.iot.service.DeviceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler Unit Tests")
class GlobalExceptionHandlerTest {

    @InjectMocks
    private TestGlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new TestGlobalExceptionHandler();
    }

    @Test
    @DisplayName("Should handle IllegalArgumentException")
    void testHandleIllegalArgumentException() {
        IllegalArgumentException ex = new IllegalArgumentException("Device not found");

        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleIllegalArgumentException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().getCode());
        assertEquals("Device not found", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Should handle IllegalStateException")
    void testHandleIllegalStateException() {
        IllegalStateException ex = new IllegalStateException("Invalid state");

        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleIllegalStateException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(409, response.getBody().getCode());
    }

    @Test
    @DisplayName("Should handle NullPointerException")
    void testHandleNullPointerException() {
        NullPointerException ex = new NullPointerException("Null value encountered");

        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleNullPointerException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(500, response.getBody().getCode());
    }

    @Test
    @DisplayName("Should handle generic Exception")
    void testHandleGenericException() {
        Exception ex = new RuntimeException("Unknown error");

        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(500, response.getBody().getCode());
        assertEquals("Unknown error", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Should handle MethodArgumentNotValidException")
    void testHandleValidationException() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("device", "deviceName", "Device name is required");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(Collections.singletonList(fieldError));

        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleValidationException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().getCode());
        assertNotNull(response.getBody().getData());
    }

    @Test
    @DisplayName("Should handle BindException")
    void testHandleBindException() {
        BindException ex = mock(BindException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("device", "deviceId", "Device ID is required");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(Collections.singletonList(fieldError));

        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleBindException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().getCode());
    }

    @RestControllerAdvice
    static class TestGlobalExceptionHandler {
        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(
                IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, ex.getMessage()));
        }

        @ExceptionHandler(IllegalStateException.class)
        public ResponseEntity<ApiResponse<Object>> handleIllegalStateException(
                IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(409, ex.getMessage()));
        }

        @ExceptionHandler(NullPointerException.class)
        public ResponseEntity<ApiResponse<Object>> handleNullPointerException(
                NullPointerException ex) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(500, ex.getMessage()));
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiResponse<Object>> handleValidationException(
                MethodArgumentNotValidException ex) {
            Map<String, String> errors = new java.util.HashMap<>();
            ex.getBindingResult().getAllErrors().forEach(error -> {
                String fieldName = ((FieldError) error).getField();
                String errorMessage = error.getDefaultMessage();
                errors.put(fieldName, errorMessage);
            });
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Validation failed", errors));
        }

        @ExceptionHandler(BindException.class)
        public ResponseEntity<ApiResponse<Object>> handleBindException(
                BindException ex) {
            Map<String, String> errors = new java.util.HashMap<>();
            ex.getBindingResult().getAllErrors().forEach(error -> {
                String fieldName = ((FieldError) error).getField();
                String errorMessage = error.getDefaultMessage();
                errors.put(fieldName, errorMessage);
            });
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Binding failed", errors));
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiResponse<Object>> handleGenericException(
                Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(500, ex.getMessage()));
        }
    }
}
