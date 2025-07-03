package com.mmw.metal_micro_wire_backend.dto.ml;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 机器学习模型预测请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelPredictionRequest {
    
    /**
     * 应用场景代码
     */
    private String scenarioCode;
    
    /**
     * 电导率
     */
    private BigDecimal conductivity;
    
    /**
     * 延展率
     */
    private BigDecimal extensibility;
    
    /**
     * 直径
     */
    private BigDecimal diameter;
} 