package com.mmw.metal_micro_wire_backend.service;

import com.mmw.metal_micro_wire_backend.entity.Device;
import com.mmw.metal_micro_wire_backend.entity.Question;
import com.mmw.metal_micro_wire_backend.entity.WireMaterial;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * IoT数据服务接口
 */
public interface IoTDataService {
    
    /**
     * 保存线材检测数据
     */
    WireMaterial saveWireMaterial(WireMaterial wireMaterial);
    
    /**
     * 保存设备状态数据
     */
    Device saveDevice(Device device);
    
    /**
     * 保存问题数据
     */
    Question saveQuestion(Question question);
    
    /**
     * 从JSON消息解析并保存线材数据
     */
    WireMaterial parseAndSaveWireMaterial(JsonNode messageNode);
    
    /**
     * 从JSON消息解析并保存设备数据
     */
    Device parseAndSaveDevice(JsonNode messageNode);
    
    /**
     * 从JSON消息解析并保存问题数据
     */
    Question parseAndSaveQuestion(JsonNode messageNode);
} 