package com.mmw.metal_micro_wire_backend.service.impl;

import com.mmw.metal_micro_wire_backend.dto.BaseResponse;
import com.mmw.metal_micro_wire_backend.dto.auth.*;
import com.mmw.metal_micro_wire_backend.entity.User;
import com.mmw.metal_micro_wire_backend.entity.Root;
import com.mmw.metal_micro_wire_backend.repository.UserRepository;
import com.mmw.metal_micro_wire_backend.repository.RootRepository;
import com.mmw.metal_micro_wire_backend.service.AuthService;
import com.mmw.metal_micro_wire_backend.service.EmailService;
import com.mmw.metal_micro_wire_backend.service.RedisService;
import com.mmw.metal_micro_wire_backend.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 认证服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private final UserRepository userRepository;
    private final RootRepository rootRepository;
    private final EmailService emailService;
    private final RedisService redisService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    
    @Override
    public SendCodeResponse sendRegisterCode(SendCodeRequest request) {
        String email = request.getE_mail();
        
        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(email)) {
            return SendCodeResponse.error("邮箱已存在", email);
        }
        
        // 检查是否已发送验证码（防止频繁发送）  
        if (redisService.isCodeSent(email, "register")) {
            long cooldownSeconds = redisService.getCodeSendCooldown(email, "register");
            return SendCodeResponse.error("验证码已发送，请" + cooldownSeconds + "秒后再试", email);
        }
        
        try {
            // 生成验证码
            String code = emailService.generateVerificationCode();
            
            // 发送邮件
            emailService.sendVerificationCode(email, code, "register");
            
            // 存储到Redis
            redisService.saveVerificationCode(email, code, "register");
            
            return SendCodeResponse.success("成功发送验证码", email);
            
        } catch (Exception e) {
            log.error("发送注册验证码失败，邮箱：{}，错误：{}", email, e.getMessage());
            return SendCodeResponse.error("发送验证码失败", email);
        }
    }
    
    @Override
    public BaseResponse<Void> register(RegisterRequest request) {
        String email = request.getE_mail();
        String userName = request.getUser_name();
        String password = request.getPasswd();
        String code = request.getMail_code();
        
        // 验证用户名不能包含@符号
        if (userName.contains("@")) {
            return BaseResponse.error("用户名不能包含@符号");
        }
        
        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(email)) {
            return BaseResponse.error("邮箱已存在");
        }
        
        // 检查用户名是否已存在
        if (userRepository.existsByUserName(userName)) {
            return BaseResponse.error("用户名已存在");
        }
        
        // 验证验证码
        if (!redisService.verifyCode(email, code, "register")) {
            return BaseResponse.error("验证码错误");
        }
        
        try {
            // 创建用户
            User user = User.builder()
                    .userName(userName)
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .roleId(0)
                    .build();
            
            userRepository.save(user);
            log.info("用户注册成功，邮箱：{}，用户名：{}", email, userName);
            
            return BaseResponse.success("成功创建用户");
            
        } catch (Exception e) {
            log.error("用户注册失败，邮箱：{}，用户名：{}，错误：{}", email, userName, e.getMessage());
            return BaseResponse.error("注册失败");
        }
    }
    
    @Override
    public SendCodeResponse sendResetPasswordCode(SendCodeRequest request) {
        String email = request.getE_mail();
        
        // 检查邮箱是否存在
        if (!userRepository.existsByEmail(email)) {
            return SendCodeResponse.error("邮箱不存在", email);
        }
        
        // 检查是否已发送验证码（防止频繁发送）  
        if (redisService.isCodeSent(email, "reset")) {
            long cooldownSeconds = redisService.getCodeSendCooldown(email, "reset");
            return SendCodeResponse.error("验证码已发送，请" + cooldownSeconds + "秒后再试", email);
        }
        
        try {
            // 生成验证码
            String code = emailService.generateVerificationCode();
            
            // 发送邮件
            emailService.sendVerificationCode(email, code, "reset");
            
            // 存储到Redis
            redisService.saveVerificationCode(email, code, "reset");
            
            return SendCodeResponse.success("成功发送验证码", email);
            
        } catch (Exception e) {
            log.error("发送重置密码验证码失败，邮箱：{}，错误：{}", email, e.getMessage());
            return SendCodeResponse.error("发送验证码失败", email);
        }
    }
    
    @Override
    public BaseResponse<Void> resetPassword(ResetPasswordRequest request) {
        String email = request.getE_mail();
        String newPassword = request.getNew_passwd();
        String code = request.getMail_code();
        
        // 验证验证码
        if (!redisService.verifyCode(email, code, "reset")) {
            return BaseResponse.error("验证码错误");
        }
        
        try {
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                return BaseResponse.error("用户不存在");
            }
            
            User user = userOpt.get();
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            
            log.info("密码重置成功，邮箱：{}", email);
            return BaseResponse.success("成功重置密码");
            
        } catch (Exception e) {
            log.error("重置密码失败，邮箱：{}，错误：{}", email, e.getMessage());
            return BaseResponse.error("重置密码失败");
        }
    }
    
    @Override
    public AuthResponse loginWithPassword(LoginRequest request) {
        String account = request.getAccount();
        String password = request.getPasswd();
        Boolean remember = request.getRemember();
        
        try {
            // 根据账号查找用户（可能是邮箱或用户名）
            Optional<User> userOpt;
            if (account.contains("@")) {
                userOpt = userRepository.findByEmail(account);
            } else {
                userOpt = userRepository.findByUserName(account);
            }
            
            if (userOpt.isEmpty()) {
                return AuthResponse.error("账户或密码错误");
            }
            
            User user = userOpt.get();
            
            // 检查用户状态
            if (user.getStatus() == 1) {
                log.warn("用户登录失败，账户已被禁用，用户名：{}", user.getUserName());
                return AuthResponse.error("账户已被禁用，请联系管理员");
            }
            
            // 验证密码
            if (!passwordEncoder.matches(password, user.getPassword())) {
                return AuthResponse.error("账户或密码错误");
            }
            
            // 生成并保存token到Redis
            String token = tokenService.generateAndSaveToken(
                user.getId(), user.getEmail(), user.getUserName(), user.getRoleId(), remember, TokenService.UserType.NORMAL);
            
            log.info("用户登录成功，邮箱：{}，用户名：{}", user.getEmail(), user.getUserName());
            return AuthResponse.success("成功登录", user.getEmail(), user.getUserName(), user.getRoleId(), token, user.getAvatarUrl());
            
        } catch (Exception e) {
            log.error("登录失败，账号：{}，错误：{}", account, e.getMessage());
            return AuthResponse.error("登录失败");
        }
    }
    
    @Override
    public SendCodeResponse sendLoginCode(SendCodeRequest request) {
        String email = request.getE_mail();
        
        // 检查邮箱是否存在
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return SendCodeResponse.error("邮箱不存在", email);
        }
        
        // 检查用户状态
        User user = userOpt.get();
        if (user.getStatus() == 1) {
            return SendCodeResponse.error("账户已被禁用，请联系管理员", email);
        }
        
        // 检查是否已发送验证码（防止频繁发送）  
        if (redisService.isCodeSent(email, "login")) {
            long cooldownSeconds = redisService.getCodeSendCooldown(email, "login");
            return SendCodeResponse.error("验证码已发送，请" + cooldownSeconds + "秒后再试", email);
        }
        
        try {
            // 生成验证码
            String code = emailService.generateVerificationCode();
            
            // 发送邮件
            emailService.sendVerificationCode(email, code, "login");
            
            // 存储到Redis
            redisService.saveVerificationCode(email, code, "login");
            
            return SendCodeResponse.success("成功发送验证码", email);
            
        } catch (Exception e) {
            log.error("发送登录验证码失败，邮箱：{}，错误：{}", email, e.getMessage());
            return SendCodeResponse.error("发送验证码失败", email);
        }
    }
    
    @Override
    public AuthResponse loginWithCode(VerifyCodeRequest request) {
        String email = request.getE_mail();
        String code = request.getMail_code();
        Boolean remember = request.getRemember();
        
        // 验证验证码
        if (!redisService.verifyCode(email, code, "login")) {
            return AuthResponse.error("登录验证码错误");
        }
        
        try {
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                return AuthResponse.error("用户不存在");
            }
            
            User user = userOpt.get();
            
            // 检查用户状态
            if (user.getStatus() == 1) {
                log.warn("用户验证码登录失败，账户已被禁用，用户名：{}", user.getUserName());
                return AuthResponse.error("账户已被禁用，请联系管理员");
            }
            
            String token = tokenService.generateAndSaveToken(
                user.getId(), user.getEmail(), user.getUserName(), user.getRoleId(), remember, TokenService.UserType.NORMAL);
            
            log.info("用户验证码登录成功，邮箱：{}，用户名：{}", user.getEmail(), user.getUserName());
            return AuthResponse.success("成功登录", user.getEmail(), user.getUserName(), user.getRoleId(), token, user.getAvatarUrl());
            
        } catch (Exception e) {
            log.error("验证码登录失败，邮箱：{}，错误：{}", email, e.getMessage());
            return AuthResponse.error("登录失败");
        }
    }
    
    @Override
    public BaseResponse<Void> logout(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return BaseResponse.error("Token不能为空");
            }
            
            // 从Redis中删除token（根据token获取用户ID后删除）
            tokenService.deleteTokenByToken(token);
            
            log.info("用户登出成功");
            return BaseResponse.success("登出成功");
            
        } catch (Exception e) {
            log.error("用户登出失败，错误：{}", e.getMessage());
            return BaseResponse.error("登出失败");
        }
    }
    
    @Override
    public AuthResponse rootLogin(RootLoginRequest request) {
        String userName = request.getUserName();
        String password = request.getPassword();
        Boolean remember = request.getRemember();
        
        try {
            // 根据用户名查找Root用户
            Optional<Root> rootOpt = rootRepository.findByUserName(userName);
            
            if (rootOpt.isEmpty()) {
                log.warn("Root登录失败，用户名不存在：{}", userName);
                return AuthResponse.error("Root用户名或密码错误");
            }
            
            Root root = rootOpt.get();
            
            // 验证密码
            if (!passwordEncoder.matches(password, root.getPassword())) {
                log.warn("Root登录失败，密码错误，用户名：{}", userName);
                return AuthResponse.error("Root用户名或密码错误");
            }
            
            // 生成并保存token到Redis (Root用户角色ID设为999，表示超级管理员)
            String token = tokenService.generateAndSaveToken(
                root.getId(), "root@system", root.getUserName(), 999, remember, TokenService.UserType.ROOT);
            
            log.info("Root用户登录成功，用户名：{}", root.getUserName());
            return AuthResponse.success("Root登录成功", "root@system", root.getUserName(), 999, token, null);
            
        } catch (Exception e) {
            log.error("Root登录失败，用户名：{}，错误：{}", userName, e.getMessage());
            return AuthResponse.error("Root登录失败");
        }
    }
} 