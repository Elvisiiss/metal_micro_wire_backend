package com.mmw.metal_micro_wire_backend.dto.scenario;

import com.mmw.metal_micro_wire_backend.entity.ApplicationScenario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 应用场景响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationScenarioResponse {
    
    /**
     * 应用场景编号
     */
    private String scenarioCode;
    
    /**
     * 应用场景名称
     */
    private String scenarioName;
    
    /**
     * 线材类型
     */
    private String wireType;
    
    /**
     * 电导率标准下限
     */
    private BigDecimal conductivityMin;
    
    /**
     * 电导率标准上限
     */
    private BigDecimal conductivityMax;
    
    /**
     * 延展率标准下限（%）
     */
    private BigDecimal extensibilityMin;
    
    /**
     * 延展率标准上限（%）
     */
    private BigDecimal extensibilityMax;
    
    /**
     * 重量标准下限（g）
     */
    private BigDecimal weightMin;
    
    /**
     * 重量标准上限（g）
     */
    private BigDecimal weightMax;
    
    /**
     * 直径标准下限（mm）
     */
    private BigDecimal diameterMin;
    
    /**
     * 直径标准上限（mm）
     */
    private BigDecimal diameterMax;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 从应用场景实体转换为响应DTO
     */
    public static ApplicationScenarioResponse fromEntity(ApplicationScenario entity) {
        return ApplicationScenarioResponse.builder()
                .scenarioCode(entity.getScenarioCode())
                .scenarioName(entity.getScenarioName())
                .wireType(entity.getWireType())
                .conductivityMin(entity.getConductivityMin())
                .conductivityMax(entity.getConductivityMax())
                .extensibilityMin(entity.getExtensibilityMin())
                .extensibilityMax(entity.getExtensibilityMax())
                .weightMin(entity.getWeightMin())
                .weightMax(entity.getWeightMax())
                .diameterMin(entity.getDiameterMin())
                .diameterMax(entity.getDiameterMax())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }
} 