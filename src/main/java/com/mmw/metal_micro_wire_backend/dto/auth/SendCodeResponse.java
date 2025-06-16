package com.mmw.metal_micro_wire_backend.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 发送验证码响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SendCodeResponse {
    
    /**
     * 响应消息
     */
    private String msg;
    
    /**
     * 响应状态码 success/Error
     */
    private String code;
    
    /**
     * 邮箱地址
     */
    private String e_mail;
    
    // 成功响应的静态方法
    public static SendCodeResponse success(String msg, String email) {
        return SendCodeResponse.builder()
                .msg(msg)
                .code("success")
                .e_mail(email)
                .build();
    }
    
    // 失败响应的静态方法
    public static SendCodeResponse error(String msg, String email) {
        return SendCodeResponse.builder()
                .msg(msg)
                .code("Error")
                .e_mail(email)
                .build();
    }
} 