package com.mmw.metal_micro_wire_backend.service;

import com.mmw.metal_micro_wire_backend.entity.WireMaterial;

/**
 * 规则引擎服务接口
 */
public interface RuleEngineService {
    
    /**
     * 评估线材是否符合应用场景标准
     * @param wireMaterial 线材数据
     * @return 评估后的线材数据（已更新evaluationResult和evaluationMessage）
     */
    WireMaterial evaluateWireMaterial(WireMaterial wireMaterial);
    
    /**
     * 重新评估指定应用场景下的所有线材数据
     * @param scenarioCode 应用场景编号
     * @return 重新评估的数据条数
     */
    int reEvaluateByScenario(String scenarioCode);
} 