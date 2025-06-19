package com.mmw.metal_micro_wire_backend.dto.auth;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Data;

/**
 * 用户管理请求DTO
 */
@Data
public class UserManageRequest {
    
    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    @Min(value = 1, message = "用户ID必须大于0")
    private Long userId;
    
    /**
     * 角色ID (0: 普通用户, 1: 管理员)
     */
    @Min(value = 0, message = "角色ID必须为0或1")
    @Max(value = 1, message = "角色ID必须为0或1")
    private Integer roleId;
    
    /**
     * 用户状态 (0: 正常, 1: 禁用)
     */
    @Min(value = 0, message = "用户状态必须为0或1")
    @Max(value = 1, message = "用户状态必须为0或1")
    private Integer status;
} 