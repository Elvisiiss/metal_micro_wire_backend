package com.mmw.metal_micro_wire_backend.service;

/**
 * 邮件服务接口
 */
public interface EmailService {
    
    /**
     * 发送验证码邮件
     * @param to 收件人邮箱
     * @param code 验证码
     * @param type 邮件类型（注册、登录、重置密码）
     */
    void sendVerificationCode(String to, String code, String type);
    
    /**
     * 生成6位数验证码
     */
    String generateVerificationCode();
} 