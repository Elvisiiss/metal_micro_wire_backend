package com.mmw.metal_micro_wire_backend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Root用户登录请求
 */
@Data
public class RootLoginRequest {
    
    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    private String userName;
    
    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;
    
    /**
     * 记住我 (可选)
     */
    private Boolean remember = false;
} 