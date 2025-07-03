package com.mmw.metal_micro_wire_backend.dto.ml;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 预测概率DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionProbability {
    
    /**
     * 不合格概率
     */
    @JsonProperty("不合格")
    private BigDecimal unqualified;
    
    /**
     * 合格概率
     */
    @JsonProperty("合格")
    private BigDecimal qualified;
} 