package com.mmw.metal_micro_wire_backend.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户资料响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String userName;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 角色ID (0: 普通用户, 1: 管理员)
     */
    private Integer roleId;
    
    /**
     * 用户状态 (0: 正常, 1: 禁用)
     */
    private Integer status;
    
    /**
     * 头像URL
     */
    private String avatarUrl;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 获取角色名称
     */
    public String getRoleName() {
        return switch (roleId) {
            case 0 -> "普通用户";
            case 1 -> "管理员";
            default -> "未知角色";
        };
    }
    
    /**
     * 获取状态名称
     */
    public String getStatusName() {
        return switch (status) {
            case 0 -> "正常";
            case 1 -> "禁用";
            default -> "未知状态";
        };
    }
}
