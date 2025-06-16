package com.mmw.metal_micro_wire_backend.service;

/**
 * Token服务接口
 */
public interface TokenService {
    
    /**
     * 生成并保存Token到Redis（以用户ID为key）
     * @param userId 用户ID
     * @param email 用户邮箱
     * @param userName 用户名
     * @param roleId 角色ID
     * @param remember 是否记住登录
     * @return Token字符串
     */
    String generateAndSaveToken(Long userId, String email, String userName, Integer roleId, Boolean remember);
    
    /**
     * 验证Token是否有效
     * @param token Token字符串
     * @return 是否有效
     */
    boolean validateToken(String token);
    
    /**
     * 从Token中获取用户ID
     * @param token Token字符串
     * @return 用户ID
     */
    Long getUserIdFromToken(String token);
    
    /**
     * 从Token中获取用户邮箱
     * @param token Token字符串
     * @return 用户邮箱
     */
    String getEmailFromToken(String token);
    
    /**
     * 从Token中获取用户名
     * @param token Token字符串
     * @return 用户名
     */
    String getUserNameFromToken(String token);
    
    /**
     * 从Token中获取角色ID
     * @param token Token字符串
     * @return 角色ID
     */
    Integer getRoleIdFromToken(String token);
    
    /**
     * 删除用户的Token（用于登出）
     * @param userId 用户ID
     */
    void deleteUserToken(Long userId);
    
    /**
     * 根据Token删除（用于登出时从Token中获取用户ID）
     * @param token Token字符串
     */
    void deleteTokenByToken(String token);
    
    /**
     * 检查Token是否是用户当前有效的Token
     * @param token Token字符串
     * @return 是否是当前有效Token
     */
    boolean isCurrentValidToken(String token);
} 