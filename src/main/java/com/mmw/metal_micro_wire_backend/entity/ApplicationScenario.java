package com.mmw.metal_micro_wire_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 应用场景实体类 - 存储线材应用场景的标准规范
 */
@Entity
@Table(name = "application_scenarios")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationScenario {
    
    /**
     * 应用场景编号 - 主键（固定两位数字字符串）
     */
    @Id
    @Column(name = "scenario_code", nullable = false, length = 2)
    private String scenarioCode;
    
    /**
     * 应用场景名称
     */
    @Column(name = "scenario_name", nullable = false)
    private String scenarioName;
    
    /**
     * 线材类型（Cu、Al、Ni、Ti、Zn）
     */
    @Column(name = "wire_type", nullable = false, length = 2)
    private String wireType;
    
    /**
     * 电导率标准下限
     */
    @Column(name = "conductivity_min", precision = 10, scale = 4)
    private BigDecimal conductivityMin;
    
    /**
     * 电导率标准上限
     */
    @Column(name = "conductivity_max", precision = 10, scale = 4)
    private BigDecimal conductivityMax;
    
    /**
     * 延展率标准下限（%）
     */
    @Column(name = "extensibility_min", precision = 10, scale = 4)
    private BigDecimal extensibilityMin;
    
    /**
     * 延展率标准上限（%）
     */
    @Column(name = "extensibility_max", precision = 10, scale = 4)
    private BigDecimal extensibilityMax;
    
    /**
     * 重量标准下限（g）
     */
    @Column(name = "weight_min", precision = 10, scale = 4)
    private BigDecimal weightMin;
    
    /**
     * 重量标准上限（g）
     */
    @Column(name = "weight_max", precision = 10, scale = 4)
    private BigDecimal weightMax;
    
    /**
     * 直径标准下限（mm）
     */
    @Column(name = "diameter_min", precision = 10, scale = 4)
    private BigDecimal diameterMin;
    
    /**
     * 直径标准上限（mm）
     */
    @Column(name = "diameter_max", precision = 10, scale = 4)
    private BigDecimal diameterMax;
    
    /**
     * 创建时间
     */
    @Column(name = "create_time", nullable = false)
    @Builder.Default
    private LocalDateTime createTime = LocalDateTime.now();
    
    /**
     * 更新时间
     */
    @Column(name = "update_time", nullable = false)
    @Builder.Default
    private LocalDateTime updateTime = LocalDateTime.now();
    
    @PrePersist
    public void prePersist() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
        if (updateTime == null) {
            updateTime = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }
} 