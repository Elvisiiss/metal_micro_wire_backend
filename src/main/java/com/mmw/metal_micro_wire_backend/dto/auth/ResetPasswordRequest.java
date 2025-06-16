package com.mmw.metal_micro_wire_backend.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 重置密码请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {
    
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
     * 新密码
     */
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, message = "密码长度至少6位")
    private String new_passwd;
    
    /**
     * 邮箱验证码
     */
    @NotBlank(message = "验证码不能为空")
    private String mail_code;
} 