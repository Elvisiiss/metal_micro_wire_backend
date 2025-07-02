package com.mmw.metal_micro_wire_backend.dto.device;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 测试设备连接请求DTO
 */
@Data
public class TestConnectionRequest {
    
    /**
     * 设备ID
     */
    @NotBlank(message = "设备ID不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "设备ID只能包含字母、数字、下划线和连字符")
    private String deviceId;
} 