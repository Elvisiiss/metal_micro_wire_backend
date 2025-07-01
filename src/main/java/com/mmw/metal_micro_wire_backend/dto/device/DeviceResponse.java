package com.mmw.metal_micro_wire_backend.dto.device;

import com.mmw.metal_micro_wire_backend.entity.Device;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 设备响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceResponse {
    
    /**
     * 设备ID
     */
    private String deviceId;
    
    /**
     * 设备状态
     */
    private Device.DeviceStatus status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 从设备实体转换为响应DTO
     */
    public static DeviceResponse fromEntity(Device device) {
        return DeviceResponse.builder()
                .deviceId(device.getDeviceId())
                .status(device.getStatus())
                .createTime(device.getCreateTime())
                .updateTime(device.getUpdateTime())
                .build();
    }
} 