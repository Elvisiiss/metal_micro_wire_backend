package com.mmw.metal_micro_wire_backend.dto.ml;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量预测请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchPredictionRequest {
    
    /**
     * 预测样本列表
     */
    private List<ModelPredictionRequest> samples;
} 