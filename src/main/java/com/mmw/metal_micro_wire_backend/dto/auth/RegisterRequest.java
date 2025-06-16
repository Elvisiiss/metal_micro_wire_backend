package com.mmw.metal_micro_wire_backend.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 用户注册请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    
    /**
     * 消息描述
     */
    @NotBlank(message = "消息不能为空")
    private String msg;
    
    /**
     * 邮箱地址
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String e_mail;
    
    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 1, max = 20, message = "用户名长度应在1-20字符之间")
    @Pattern(regexp = "^[^@]*$", message = "用户名不能包含@符号")
    private String user_name;
    
    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码长度至少6位")
    private String passwd;
    
    /**
     * 邮箱验证码
     */
    @NotBlank(message = "验证码不能为空")
    private String mail_code;
} 