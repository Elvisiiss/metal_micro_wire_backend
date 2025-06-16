package com.mmw.metal_micro_wire_backend.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

/**
 * 用户登录请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    
    /**
     * 消息描述
     */
    @NotBlank(message = "消息不能为空")
    private String msg;
    
    /**
     * 账户（用户名或邮箱）
     */
    @NotBlank(message = "账户不能为空")
    private String account;
    
    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String passwd;
    
    /**
     * 记住我（可选）
     */
    private Boolean remember;
} 