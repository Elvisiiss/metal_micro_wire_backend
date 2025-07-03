package com.mmw.metal_micro_wire_backend.dto.ml;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量预测响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchPredictionResponse {
    
    /**
     * 预测数据列表
     */
    private List<PredictionData> data;
    
    /**
     * 是否成功
     */
    private Boolean success;
    
    /**
     * 错误信息
     */
    private String error;
    
    /**
     * 预测数量
     */
    private Integer count;
    
    /**
     * 时间戳
     */
    private String timestamp;
    
    /**
     * 创建成功响应
     */
    public static BatchPredictionResponse success(List<PredictionData> data, Integer count) {
        BatchPredictionResponse response = new BatchPredictionResponse();
        response.setSuccess(true);
        response.setData(data);
        response.setCount(count);
        return response;
    }
    
    /**
     * 创建失败响应
     */
    public static BatchPredictionResponse error(String error) {
        BatchPredictionResponse response = new BatchPredictionResponse();
        response.setSuccess(false);
        response.setError(error);
        return response;
    }
} 