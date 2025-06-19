package com.mmw.metal_micro_wire_backend.util;

import com.mmw.metal_micro_wire_backend.config.JwtConfig;
import com.mmw.metal_micro_wire_backend.service.TokenService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {
    
    private final JwtConfig jwtConfig;
    
    /**
     * 生成JWT Token
     * @param userId 用户ID
     * @param email 用户邮箱
     * @param userName 用户名
     * @param roleId 角色ID
     * @param remember 是否记住登录
     * @param userType 用户类型
     * @return JWT Token
     */
    public String generateToken(Long userId, String email, String userName, Integer roleId, Boolean remember, TokenService.UserType userType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("email", email);
        claims.put("userName", userName);
        claims.put("roleId", roleId);
        claims.put("userType", userType.name()); // 存储用户类型
        
        // 根据是否记住登录设置不同的过期时间
        int expirationHours = (remember != null && remember) ? 
            jwtConfig.getExpirationRemember() : jwtConfig.getExpirationNormal();
        
        Date expiration = new Date(System.currentTimeMillis() + expirationHours * 60 * 60 * 1000L);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(expiration)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * 验证Token
     * @param token JWT Token
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT token验证失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 从Token中获取Claims
     * @param token JWT Token
     * @return Claims
     */
    public Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException | IllegalArgumentException e) {
            log.error("从token中获取claims失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 从Token中获取用户ID
     * @param token JWT Token
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims != null) {
            return claims.get("userId", Long.class);
        }
        return null;
    }
    
    /**
     * 从Token中获取用户邮箱
     * @param token JWT Token
     * @return 用户邮箱
     */
    public String getEmailFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims != null) {
            return claims.getSubject();
        }
        return null;
    }
    
    /**
     * 从Token中获取用户名
     * @param token JWT Token
     * @return 用户名
     */
    public String getUserNameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims != null) {
            return claims.get("userName", String.class);
        }
        return null;
    }
    
    /**
     * 从Token中获取角色ID
     * @param token JWT Token
     * @return 角色ID
     */
    public Integer getRoleIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims != null) {
            return claims.get("roleId", Integer.class);
        }
        return null;
    }
    
    /**
     * 从Token中获取用户类型
     * @param token JWT Token
     * @return 用户类型
     */
    public TokenService.UserType getUserTypeFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims != null) {
            String userTypeStr = claims.get("userType", String.class);
            if (userTypeStr != null) {
                try {
                    return TokenService.UserType.valueOf(userTypeStr);
                } catch (IllegalArgumentException e) {
                    log.warn("无效的用户类型: {}", userTypeStr);
                }
            }
        }
        return null;
    }
    
    /**
     * 检查Token是否过期
     * @param token JWT Token
     * @return 是否过期
     */
    public boolean isTokenExpired(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims != null) {
            return claims.getExpiration().before(new Date());
        }
        return true;
    }
    
    /**
     * 获取签名密钥
     * @return 签名密钥
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
} 