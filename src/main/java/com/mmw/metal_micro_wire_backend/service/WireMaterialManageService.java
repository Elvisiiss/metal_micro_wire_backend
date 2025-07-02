package com.mmw.metal_micro_wire_backend.service;

import com.mmw.metal_micro_wire_backend.dto.BaseResponse;
import com.mmw.metal_micro_wire_backend.dto.wirematerial.UpdateWireMaterialRequest;
import com.mmw.metal_micro_wire_backend.dto.wirematerial.WireMaterialPageRequest;
import com.mmw.metal_micro_wire_backend.dto.wirematerial.WireMaterialPageResponse;
import com.mmw.metal_micro_wire_backend.dto.wirematerial.WireMaterialResponse;

/**
 * 线材管理服务接口
 */
public interface WireMaterialManageService {
    
    /**
     * 分页查询线材列表
     * @param request 查询请求
     * @return 分页响应
     */
    BaseResponse<WireMaterialPageResponse> getWireMaterialList(WireMaterialPageRequest request);
    
    /**
     * 根据批次号获取线材信息
     * @param batchNumber 批次号
     * @return 线材信息
     */
    BaseResponse<WireMaterialResponse> getWireMaterialByBatchNumber(String batchNumber);
    
    /**
     * 更新线材信息
     * @param batchNumber 批次号
     * @param request 更新请求
     * @return 操作结果
     */
    BaseResponse<WireMaterialResponse> updateWireMaterial(String batchNumber, UpdateWireMaterialRequest request);
    
    /**
     * 删除线材记录
     * @param batchNumber 批次号
     * @return 操作结果
     */
    BaseResponse<Void> deleteWireMaterial(String batchNumber);
} 