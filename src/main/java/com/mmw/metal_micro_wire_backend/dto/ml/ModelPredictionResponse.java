package com.mmw.metal_micro_wire_backend.dto.ml;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 机器学习模型预测响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModelPredictionResponse {
    
    /**
     * 预测数据
     */
    private PredictionData data;
    
    /**
     * 是否成功
     */
    private Boolean success;
    
    /**
     * 错误信息
     */
    private String error;
    
    /**
     * 时间戳
     */
    private String timestamp;
    
    /**
     * 创建成功响应
     */
    public static ModelPredictionResponse success(PredictionData data) {
        ModelPredictionResponse response = new ModelPredictionResponse();
        response.setSuccess(true);
        response.setData(data);
        return response;
    }
    
    /**
     * 创建失败响应
     */
    public static ModelPredictionResponse error(String error) {
        ModelPredictionResponse response = new ModelPredictionResponse();
        response.setSuccess(false);
        response.setError(error);
        return response;
    }
} 