package com.mmw.metal_micro_wire_backend.dto.wirematerial;

import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * 线材分页查询请求DTO
 */
@Data
public class WireMaterialPageRequest {
    
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
     * 批次号关键词搜索（可选）
     */
    private String batchNumberKeyword;
    
    /**
     * 设备ID关键词搜索（可选）
     */
    private String deviceIdKeyword;
    
    /**
     * 生产商关键词搜索（可选）
     */
    private String manufacturerKeyword;
    
    /**
     * 负责人关键词搜索（可选）
     */
    private String responsiblePersonKeyword;
    
    /**
     * 工艺类型关键词搜索（可选）
     */
    private String processTypeKeyword;
    
    /**
     * 生产机器关键词搜索（可选）
     */
    private String productionMachineKeyword;
    
    /**
     * 应用场景编号筛选（可选）
     */
    private String scenarioCode;
    
    /**
     * 设备代码筛选（可选）
     */
    private String deviceCode;
    
    /**
     * 排序字段，默认按创建时间倒序
     */
    private String sortBy = "createTime";
    
    /**
     * 排序方向，默认降序
     */
    private String sortDirection = "desc";
} 