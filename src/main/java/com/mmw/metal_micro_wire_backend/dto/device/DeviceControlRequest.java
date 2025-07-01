package com.mmw.metal_micro_wire_backend.dto.device;

import com.mmw.metal_micro_wire_backend.entity.Device;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 设备控制请求DTO
 */
@Data
public class DeviceControlRequest {
    
    /**
     * 设备ID
     */
    @NotBlank(message = "设备ID不能为空")
    private String deviceId;
    
    /**
     * 目标状态
     */
    @NotNull(message = "设备状态不能为空")
    private Device.DeviceStatus targetStatus;
} 