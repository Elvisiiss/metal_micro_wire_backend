package com.mmw.metal_micro_wire_backend.service.impl;

import com.mmw.metal_micro_wire_backend.config.JwtConfig;
import com.mmw.metal_micro_wire_backend.service.RedisService;
import com.mmw.metal_micro_wire_backend.service.TokenService;
import com.mmw.metal_micro_wire_backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Token服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {
    
    private final JwtUtil jwtUtil;
    private final RedisService redisService;
    private final JwtConfig jwtConfig;
    
    private static final String TOKEN_PREFIX = "token:";
    
    @Override
    public String generateAndSaveToken(Long userId, String email, String userName, Integer roleId, Boolean remember, UserType userType) {
        // 生成JWT Token
        String token = jwtUtil.generateToken(userId, email, userName, roleId, remember, userType);
        
        // 计算过期时间（小时转换为秒）
        int expirationHours = (remember != null && remember) ? 
            jwtConfig.getExpirationRemember() : jwtConfig.getExpirationNormal();
        long expirationSeconds = expirationHours * 60L * 60L;
        
        // 删除用户之前的Token（如果存在）
        deleteUserToken(userId, userType);
        
        // 将Token保存到Redis，key为token:用户类型:用户ID，value为Token
        String redisKey = TOKEN_PREFIX + userType.getPrefix() + ":" + userId;
        redisService.setWithExpiration(redisKey, token, expirationSeconds, TimeUnit.SECONDS);
        
        log.info("Token已生成并保存到Redis，用户类型：{}，用户ID：{}，过期时间：{}小时，旧Token已失效", 
                userType.name(), userId, expirationHours);
        return token;
    }
    
    @Override
    public boolean validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        
        // 首先验证JWT Token本身的有效性
        if (!jwtUtil.validateToken(token)) {
            log.warn("JWT Token验证失败");
            return false;
        }
        
        // 然后检查Token是否是用户当前有效的Token
        if (!isCurrentValidToken(token)) {
            log.warn("Token不是用户当前有效的Token或已过期");
            return false;
        }
        
        return true;
    }
    
    @Override
    public Long getUserIdFromToken(String token) {
        if (!validateToken(token)) {
            return null;
        }
        return jwtUtil.getUserIdFromToken(token);
    }
    
    @Override
    public String getEmailFromToken(String token) {
        if (!validateToken(token)) {
            return null;
        }
        return jwtUtil.getEmailFromToken(token);
    }
    
    @Override
    public String getUserNameFromToken(String token) {
        if (!validateToken(token)) {
            return null;
        }
        return jwtUtil.getUserNameFromToken(token);
    }
    
    @Override
    public Integer getRoleIdFromToken(String token) {
        if (!validateToken(token)) {
            return null;
        }
        return jwtUtil.getRoleIdFromToken(token);
    }
    
    @Override
    public UserType getUserTypeFromToken(String token) {
        if (!validateToken(token)) {
            return null;
        }
        return jwtUtil.getUserTypeFromToken(token);
    }
    
    @Override
    public void deleteUserToken(Long userId, UserType userType) {
        if (userId != null && userType != null) {
            String redisKey = TOKEN_PREFIX + userType.getPrefix() + ":" + userId;
            redisService.delete(redisKey);
            log.info("用户Token已从Redis中删除，用户类型：{}，用户ID：{}", userType.name(), userId);
        }
    }
    
    @Override
    public void deleteTokenByToken(String token) {
        if (token != null && !token.trim().isEmpty()) {
            Long userId = jwtUtil.getUserIdFromToken(token);
            UserType userType = jwtUtil.getUserTypeFromToken(token);
            if (userId != null && userType != null) {
                deleteUserToken(userId, userType);
            }
        }
    }
    
    @Override
    public boolean isCurrentValidToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        
        // 从Token中获取用户ID和用户类型
        Long userId = jwtUtil.getUserIdFromToken(token);
        UserType userType = jwtUtil.getUserTypeFromToken(token);
        if (userId == null || userType == null) {
            return false;
        }
        
        // 检查Redis中存储的Token是否与当前Token一致
        String redisKey = TOKEN_PREFIX + userType.getPrefix() + ":" + userId;
        String storedToken = redisService.get(redisKey);
        
        if (storedToken == null) {
            log.debug("用户Token在Redis中不存在，用户类型：{}，用户ID：{}", userType.name(), userId);
            return false;
        }
        
        // 验证存储的Token值是否与当前Token一致
        boolean isValid = storedToken.equals(token);
        if (!isValid) {
            log.warn("Token不匹配，用户类型：{}，用户ID：{}，可能是旧Token或被替换", userType.name(), userId);
        }
        
        return isValid;
    }
} 