package com.mmw.metal_micro_wire_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@Entity
@Table(name = "user_root")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Root {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 用户名
     */
    @Column(unique = true, nullable = false)
    String userName;

    /**
     * 密码(BCrypt加密)
     */
    @Column(nullable = false)
    String password;
    
}
