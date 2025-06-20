package com.mmw.metal_micro_wire_backend.controller;

import com.mmw.metal_micro_wire_backend.dto.BaseResponse;
import com.mmw.metal_micro_wire_backend.dto.auth.*;
import com.mmw.metal_micro_wire_backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.mmw.metal_micro_wire_backend.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * 认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {
    
    private final AuthService authService;
    private final JwtUtil jwtUtil;
    
    /**
     * 发送注册验证码
     */
    @PostMapping("/register/send-code")
    public ResponseEntity<SendCodeResponse> sendRegisterCode(@Valid @RequestBody SendCodeRequest request) {
        log.info("发送注册验证码请求，邮箱：{}", request.getE_mail());
        SendCodeResponse response = authService.sendRegisterCode(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 用户注册
     */
    @PostMapping("/register/verify-code")
    public ResponseEntity<BaseResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("用户注册请求，邮箱：{}，用户名：{}", request.getE_mail(), request.getUser_name());
        BaseResponse<Void> response = authService.register(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 发送重置密码验证码
     */
    @PostMapping("/reset-password/send-code")
    public ResponseEntity<SendCodeResponse> sendResetPasswordCode(@Valid @RequestBody SendCodeRequest request) {
        log.info("发送重置密码验证码请求，邮箱：{}", request.getE_mail());
        SendCodeResponse response = authService.sendResetPasswordCode(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 重置密码
     */
    @PostMapping("/reset-password/verify-code")
    public ResponseEntity<BaseResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("重置密码请求，邮箱：{}", request.getE_mail());
        BaseResponse<Void> response = authService.resetPassword(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 账号密码登录
     */
    @PostMapping("/login/password")
    public ResponseEntity<AuthResponse> loginWithPassword(@Valid @RequestBody LoginRequest request) {
        log.info("账号密码登录请求，账号：{}", request.getAccount());
        AuthResponse response = authService.loginWithPassword(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 发送登录验证码
     */
    @PostMapping("/login/send-code")
    public ResponseEntity<SendCodeResponse> sendLoginCode(@Valid @RequestBody SendCodeRequest request) {
        log.info("发送登录验证码请求，邮箱：{}", request.getE_mail());
        SendCodeResponse response = authService.sendLoginCode(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 验证码登录
     */
    @PostMapping("/login/verify-code")
    public ResponseEntity<AuthResponse> loginWithCode(@Valid @RequestBody VerifyCodeRequest request) {
        log.info("验证码登录请求，邮箱：{}", request.getE_mail());
        AuthResponse response = authService.loginWithCode(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 用户登出
     * 需要Token认证
     */
    @GetMapping("/logout")
    public ResponseEntity<BaseResponse<Void>> logout(HttpServletRequest request) {
        // 从请求中获取token
        String token = getTokenFromRequest(request);
        log.info("用户登出请求");
        BaseResponse<Void> response = authService.logout(token);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 从请求中获取token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        // 从Authorization头获取
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        
        // 从参数获取
        String token = request.getParameter("token");
        if (token != null && !token.trim().isEmpty()) {
            return token;
        }
        
        // 从header直接获取token
        return request.getHeader("token");
    }
    
    /**
     * Root用户登录
     */
    @PostMapping("/root/login")
    public ResponseEntity<AuthResponse> rootLogin(@Valid @RequestBody RootLoginRequest request) {
        log.info("Root用户登录请求，用户名：{}", request.getUserName());
        AuthResponse response = authService.rootLogin(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/token")
    public Integer returnToken(@RequestParam String token){
        if(!jwtUtil.validateToken(token)){return -1;}
        if(jwtUtil.isTokenExpired(token)){return -1;}
        else return jwtUtil.getRoleIdFromToken(token);
    }

} 