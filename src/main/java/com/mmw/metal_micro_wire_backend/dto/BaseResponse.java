package com.mmw.metal_micro_wire_backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用响应类
 */
@Data
@Builder
@NoArgsConstructor 
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseResponse<T> {
    
    /**
     * 响应消息
     */
    private String msg;
    
    /**
     * 响应状态码 success/Error
     */
    private String code;
    
    /**
     * 响应数据
     */
    private T data;
    
    // 成功响应的静态方法
    public static <T> BaseResponse<T> success(String msg) {
        return BaseResponse.<T>builder()
                .msg(msg)
                .code("success")
                .build();
    }
    
    public static <T> BaseResponse<T> success(String msg, T data) {
        return BaseResponse.<T>builder()
                .msg(msg)
                .code("success")
                .data(data)
                .build();
    }
    
    // 失败响应的静态方法
    public static <T> BaseResponse<T> error(String msg) {
        return BaseResponse.<T>builder()
                .msg(msg)
                .code("Error")
                .build();
    }
} 