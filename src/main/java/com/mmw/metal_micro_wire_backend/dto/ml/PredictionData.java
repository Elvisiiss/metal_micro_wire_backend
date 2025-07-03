package com.mmw.metal_micro_wire_backend.dto.ml;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 预测数据DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionData {
    
    /**
     * 预测结果
     */
    private String prediction;
    
    /**
     * 预测概率
     */
    private PredictionProbability probability;
    
    /**
     * 置信度
     */
    private BigDecimal confidence;
    
    /**
     * 输入数据
     */
    private ModelPredictionRequest input;
    
    /**
     * 时间戳
     */
    private String timestamp;
} 