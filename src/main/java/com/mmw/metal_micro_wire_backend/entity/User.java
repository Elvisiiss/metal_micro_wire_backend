package com.mmw.metal_micro_wire_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 用户名
     */
    @Column(unique = true, nullable = false)
    private String userName;
    
    /**
     * 邮箱
     */
    @Column(unique = true, nullable = false)
    private String email;
    
    /**
     * 密码（BCrypt加密）
     */
    @Column(nullable = false)
    private String password;
    
    /**
     * 角色ID (0: 普通用户, 1: 管理员)
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer roleId = 0;
    
    /**
     * 用户状态 (0: 正常, 1: 禁用)
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer status = 0;
    
    /**
     * 创建时间
     */
    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createTime = LocalDateTime.now();
    
    /**
     * 更新时间
     */
    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime updateTime = LocalDateTime.now();
    
    @PreUpdate
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }
} 