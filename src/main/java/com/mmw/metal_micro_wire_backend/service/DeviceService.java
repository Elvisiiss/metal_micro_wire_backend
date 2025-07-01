package com.mmw.metal_micro_wire_backend.service;

import com.mmw.metal_micro_wire_backend.dto.BaseResponse;
import com.mmw.metal_micro_wire_backend.dto.device.*;

/**
 * 设备服务接口
 */
public interface DeviceService {
    
    /**
     * 分页查询设备列表
     * @param request 查询请求
     * @return 分页响应
     */
    BaseResponse<DevicePageResponse> getDeviceList(DevicePageRequest request);
    
    /**
     * 创建设备
     * @param request 创建请求
     * @return 操作结果
     */
    BaseResponse<DeviceResponse> createDevice(CreateDeviceRequest request);
    
    /**
     * 删除设备
     * @param deviceId 设备ID
     * @return 操作结果
     */
    BaseResponse<Void> deleteDevice(String deviceId);
    
    /**
     * 控制设备（启停）
     * @param request 控制请求
     * @return 操作结果
     */
    BaseResponse<Void> controlDevice(DeviceControlRequest request);
    
    /**
     * 根据设备ID获取设备信息
     * @param deviceId 设备ID
     * @return 设备信息
     */
    BaseResponse<DeviceResponse> getDeviceById(String deviceId);
} 