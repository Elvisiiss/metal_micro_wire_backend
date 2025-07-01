package com.mmw.metal_micro_wire_backend.dto.device;

import com.mmw.metal_micro_wire_backend.entity.Device;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 创建设备请求DTO
 */
@Data
public class CreateDeviceRequest {
    
    /**
     * 设备ID
     */
    @NotBlank(message = "设备ID不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "设备ID只能包含字母、数字、下划线和连字符")
    private String deviceId;
    
    /**
     * 设备初始状态，默认为OFF
     */
    private Device.DeviceStatus status = Device.DeviceStatus.OFF;
} 