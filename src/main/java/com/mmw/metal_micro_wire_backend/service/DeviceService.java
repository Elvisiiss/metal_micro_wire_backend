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
     * 
     * 说明：此方法向设备发送控制命令消息，由于硬件限制无法获得实时响应，
     * 只能确认消息已送达。设备状态的实际更新通过AMQP消息监听异步完成。
     * 
     * @param request 控制请求
     * @return 操作结果（消息发送状态）
     */
    BaseResponse<Void> controlDevice(DeviceControlRequest request);
    
    /**
     * 根据设备ID获取设备信息
     * @param deviceId 设备ID
     * @return 设备信息
     */
    BaseResponse<DeviceResponse> getDeviceById(String deviceId);
} 