package com.mmw.metal_micro_wire_backend.dto.device;

import com.mmw.metal_micro_wire_backend.entity.Device;
import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * 设备分页查询请求DTO
 */
@Data
public class DevicePageRequest {
    
    /**
     * 页码，从0开始
     */
    @Min(value = 0, message = "页码不能小于0")
    private int page = 0;
    
    /**
     * 每页大小
     */
    @Min(value = 1, message = "每页大小不能小于1")
    @Max(value = 100, message = "每页大小不能超过100")
    private int size = 10;
    
    /**
     * 设备状态筛选（可选）
     */
    private Device.DeviceStatus status;
    
    /**
     * 排序字段，默认按创建时间倒序
     */
    private String sortBy = "createTime";
    
    /**
     * 排序方向，默认降序
     */
    private String sortDirection = "desc";
} 