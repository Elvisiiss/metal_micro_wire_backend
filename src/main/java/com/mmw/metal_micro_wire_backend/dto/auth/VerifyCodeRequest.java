package com.mmw.metal_micro_wire_backend.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 验证码验证请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyCodeRequest {
    
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
     * 邮箱验证码
     */
    @NotBlank(message = "验证码不能为空")
    private String mail_code;
} 