package com.mmw.metal_micro_wire_backend.dto.device;


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
     * 设备代码（两位数字，用于匹配批次号中的机器号）
     */
    @Pattern(regexp = "^\\d{2}$", message = "设备代码必须是两位数字")
    private String deviceCode;
} 