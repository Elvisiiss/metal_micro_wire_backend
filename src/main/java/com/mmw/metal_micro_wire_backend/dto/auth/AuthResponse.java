package com.mmw.metal_micro_wire_backend.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 认证响应DTO（用于登录成功返回用户信息）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {
    
    /**
     * 响应消息
     */
    private String msg;
    
    /**
     * 响应状态码 success/Error
     */
    private String code;
    
    /**
     * 用户邮箱
     */
    private String e_mail;
    
    /**
     * 用户名
     */
    private String user_name;
    
    /**
     * 角色ID
     */
    private Integer role_id;
    
    /**
     * 认证Token
     */
    private String token;
    
    // 成功登录的静态方法
    public static AuthResponse success(String msg, String email, String userName, Integer roleId, String token) {
        return AuthResponse.builder()
                .msg(msg)
                .code("success")
                .e_mail(email)
                .user_name(userName)
                .role_id(roleId)
                .token(token)
                .build();
    }
    
    // 失败响应的静态方法
    public static AuthResponse error(String msg) {
        return AuthResponse.builder()
                .msg(msg)
                .code("Error")
                .build();
    }
} 