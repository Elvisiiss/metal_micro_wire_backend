package com.mmw.metal_micro_wire_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 设备实体类 - 存储设备状态信息
 */
@Entity
@Table(name = "devices")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Device {
    
    /**
     * 设备状态枚举
     */
    public enum DeviceStatus {
        ON("ON"),
        OFF("OFF");
        
        private final String value;
        
        DeviceStatus(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
    
    /**
     * 设备ID - 主键
     */
    @Id
    @Column(name = "device_id", nullable = false)
    private String deviceId;
    
    /**
     * 设备状态 (ON/OFF)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private DeviceStatus status = DeviceStatus.OFF;
    
    /**
     * 创建时间
     */
    @Column(name = "create_time", nullable = false)
    @Builder.Default
    private LocalDateTime createTime = LocalDateTime.now();
    
    /**
     * 更新时间
     */
    @Column(name = "update_time", nullable = false)
    @Builder.Default
    private LocalDateTime updateTime = LocalDateTime.now();
    
    @PrePersist
    public void prePersist() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
        if (updateTime == null) {
            updateTime = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }
} 