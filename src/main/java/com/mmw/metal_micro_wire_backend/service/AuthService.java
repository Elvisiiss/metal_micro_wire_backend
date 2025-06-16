package com.mmw.metal_micro_wire_backend.service;

import com.mmw.metal_micro_wire_backend.dto.BaseResponse;
import com.mmw.metal_micro_wire_backend.dto.auth.*;

/**
 * 认证服务接口
 */
public interface AuthService {
    
    /**
     * 发送注册验证码
     */
    SendCodeResponse sendRegisterCode(SendCodeRequest request);
    
    /**
     * 用户注册
     */
    BaseResponse<Void> register(RegisterRequest request);
    
    /**
     * 发送重置密码验证码
     */
    SendCodeResponse sendResetPasswordCode(SendCodeRequest request);
    
    /**
     * 重置密码
     */
    BaseResponse<Void> resetPassword(ResetPasswordRequest request);
    
    /**
     * 账号密码登录
     */
    AuthResponse loginWithPassword(LoginRequest request);
    
    /**
     * 发送登录验证码
     */
    SendCodeResponse sendLoginCode(SendCodeRequest request);
    
    /**
     * 验证码登录
     */
    AuthResponse loginWithCode(VerifyCodeRequest request);
    
    /**
     * 用户登出
     * @param token 用户的token
     * @return 登出结果
     */
    BaseResponse<Void> logout(String token);
} 