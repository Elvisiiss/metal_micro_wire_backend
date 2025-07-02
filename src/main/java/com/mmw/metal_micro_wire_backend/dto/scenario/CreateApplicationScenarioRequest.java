package com.mmw.metal_micro_wire_backend.dto.scenario;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * 创建应用场景请求DTO
 */
@Data
public class CreateApplicationScenarioRequest {
    
    /**
     * 应用场景编号（固定两位数字字符串）
     */
    @NotBlank(message = "应用场景编号不能为空")
    @Pattern(regexp = "^\\d{2}$", message = "应用场景编号必须是两位数字")
    private String scenarioCode;
    
    /**
     * 应用场景名称
     */
    @NotBlank(message = "应用场景名称不能为空")
    @Size(max = 100, message = "应用场景名称长度不能超过100个字符")
    private String scenarioName;
    
    /**
     * 线材类型（Cu、Al、Ni、Ti、Zn）
     */
    @NotBlank(message = "线材类型不能为空")
    @Pattern(regexp = "^(Cu|Al|Ni|Ti|Zn)$", message = "线材类型必须是Cu、Al、Ni、Ti、Zn中的一种")
    private String wireType;
    
    /**
     * 电导率标准下限
     */
    @Positive(message = "电导率标准下限必须大于0")
    private BigDecimal conductivityMin;
    
    /**
     * 电导率标准上限
     */
    @Positive(message = "电导率标准上限必须大于0")
    private BigDecimal conductivityMax;
    
    /**
     * 延展率标准下限（%）
     */
    @Positive(message = "延展率标准下限必须大于0")
    private BigDecimal extensibilityMin;
    
    /**
     * 延展率标准上限（%）
     */
    @Positive(message = "延展率标准上限必须大于0")
    private BigDecimal extensibilityMax;
    
    /**
     * 重量标准下限（g）
     */
    @Positive(message = "重量标准下限必须大于0")
    private BigDecimal weightMin;
    
    /**
     * 重量标准上限（g）
     */
    @Positive(message = "重量标准上限必须大于0")
    private BigDecimal weightMax;
    
    /**
     * 直径标准下限（mm）
     */
    @Positive(message = "直径标准下限必须大于0")
    private BigDecimal diameterMin;
    
    /**
     * 直径标准上限（mm）
     */
    @Positive(message = "直径标准上限必须大于0")
    private BigDecimal diameterMax;
} 