package com.mmw.metal_micro_wire_backend.exception;

import com.mmw.metal_micro_wire_backend.dto.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 处理请求参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        log.warn("请求参数验证失败：{}", errors);
        
        BaseResponse<Map<String, String>> response = BaseResponse.<Map<String, String>>builder()
                .msg("请求参数验证失败")
                .code("Error")
                .data(errors)
                .build();
        
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<BaseResponse<Map<String, String>>> handleBindException(BindException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("数据绑定失败：{}", errors);

        BaseResponse<Map<String, String>> response = BaseResponse.<Map<String, String>>builder()
                .msg("数据绑定失败")
                .code("Error")
                .data(errors)
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 处理参数约束验证异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<BaseResponse<Void>> handleConstraintViolationException(ConstraintViolationException ex) {
        // 提取第一个约束违反的错误信息
        String errorMessage = "参数验证失败";
        if (!ex.getConstraintViolations().isEmpty()) {
            ConstraintViolation<?> violation = ex.getConstraintViolations().iterator().next();
            errorMessage = violation.getMessage();
        }

        log.warn("参数约束验证失败：{}", errorMessage);

        BaseResponse<Void> response = BaseResponse.<Void>builder()
                .msg(errorMessage)
                .code("Error")
                .build();

        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<BaseResponse<Void>> handleRuntimeException(RuntimeException ex) {
        log.error("运行时异常：", ex);
        
        BaseResponse<Void> response = BaseResponse.<Void>builder()
                .msg("系统内部错误")
                .code("Error")
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Void>> handleGenericException(Exception ex) {
        log.error("未知异常：", ex);
        
        BaseResponse<Void> response = BaseResponse.<Void>builder()
                .msg("系统异常")
                .code("Error")
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
} 