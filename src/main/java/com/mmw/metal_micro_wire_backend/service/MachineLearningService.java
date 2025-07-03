package com.mmw.metal_micro_wire_backend.service;

import com.mmw.metal_micro_wire_backend.dto.ml.BatchPredictionRequest;
import com.mmw.metal_micro_wire_backend.dto.ml.BatchPredictionResponse;
import com.mmw.metal_micro_wire_backend.dto.ml.ModelPredictionRequest;
import com.mmw.metal_micro_wire_backend.dto.ml.ModelPredictionResponse;
import com.mmw.metal_micro_wire_backend.entity.WireMaterial;

/**
 * 机器学习服务接口
 */
public interface MachineLearningService {
    
    /**
     * 单个预测
     *
     * @param request 预测请求
     * @return 预测结果
     */
    ModelPredictionResponse predict(ModelPredictionRequest request);
    
    /**
     * 批量预测
     *
     * @param request 批量预测请求
     * @return 批量预测结果
     */
    BatchPredictionResponse predictBatch(BatchPredictionRequest request);
    
    /**
     * 从线材实体创建预测请求
     *
     * @param wireMaterial 线材实体
     * @return 预测请求
     */
    ModelPredictionRequest createPredictionRequest(WireMaterial wireMaterial);
    
    /**
     * 检查模型服务健康状态
     *
     * @return 是否健康
     */
    boolean checkHealth();
} 