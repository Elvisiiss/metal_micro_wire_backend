package com.mmw.metal_micro_wire_backend.service.impl;

import com.mmw.metal_micro_wire_backend.entity.ApplicationScenario;
import com.mmw.metal_micro_wire_backend.entity.WireMaterial;
import com.mmw.metal_micro_wire_backend.repository.ApplicationScenarioRepository;
import com.mmw.metal_micro_wire_backend.repository.WireMaterialRepository;
import com.mmw.metal_micro_wire_backend.service.RuleEngineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 规则引擎服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RuleEngineServiceImpl implements RuleEngineService {
    
    private final ApplicationScenarioRepository applicationScenarioRepository;
    private final WireMaterialRepository wireMaterialRepository;
    
    @Override
    public WireMaterial evaluateWireMaterial(WireMaterial wireMaterial) {
        try {
            if (wireMaterial.getScenarioCode() == null) {
                log.warn("线材批次号：{} 未能解析出应用场景编号，跳过评估", wireMaterial.getBatchNumber());
                wireMaterial.setEvaluationResult(WireMaterial.EvaluationResult.UNKNOWN);
                wireMaterial.setEvaluationMessage("未能解析应用场景编号");
                return wireMaterial;
            }
            
            // 获取应用场景标准
            Optional<ApplicationScenario> scenarioOpt = applicationScenarioRepository.findById(wireMaterial.getScenarioCode());
            if (scenarioOpt.isEmpty()) {
                log.warn("未找到应用场景：{}，批次号：{}", wireMaterial.getScenarioCode(), wireMaterial.getBatchNumber());
                wireMaterial.setEvaluationResult(WireMaterial.EvaluationResult.UNKNOWN);
                wireMaterial.setEvaluationMessage("应用场景不存在：" + wireMaterial.getScenarioCode());
                return wireMaterial;
            }
            
            ApplicationScenario scenario = scenarioOpt.get();
            List<String> failures = new ArrayList<>();
            
            // 检查直径
            if (wireMaterial.getDiameter() != null) {
                if (!isInRange(wireMaterial.getDiameter(), scenario.getDiameterMin(), scenario.getDiameterMax())) {
                    failures.add("直径超出标准范围");
                }
            }
            
            // 检查电导率（这里使用resistance字段，实际应该是电导率）
            if (wireMaterial.getResistance() != null) {
                if (!isInRange(wireMaterial.getResistance(), scenario.getConductivityMin(), scenario.getConductivityMax())) {
                    failures.add("电导率超出标准范围");
                }
            }
            
            // 检查延展率
            if (wireMaterial.getExtensibility() != null) {
                if (!isInRange(wireMaterial.getExtensibility(), scenario.getExtensibilityMin(), scenario.getExtensibilityMax())) {
                    failures.add("延展率超出标准范围");
                }
            }
            
            // 检查重量
            if (wireMaterial.getWeight() != null) {
                if (!isInRange(wireMaterial.getWeight(), scenario.getWeightMin(), scenario.getWeightMax())) {
                    failures.add("重量超出标准范围");
                }
            }
            
            // 设置评估结果
            if (failures.isEmpty()) {
                wireMaterial.setEvaluationResult(WireMaterial.EvaluationResult.PASS);
                wireMaterial.setEvaluationMessage("所有指标均符合标准");
            } else {
                wireMaterial.setEvaluationResult(WireMaterial.EvaluationResult.FAIL);
                wireMaterial.setEvaluationMessage(String.join("；", failures));
            }
            
            log.debug("完成线材评估，批次号：{}，结果：{}，详情：{}", 
                wireMaterial.getBatchNumber(), 
                wireMaterial.getEvaluationResult(),
                wireMaterial.getEvaluationMessage());
            
        } catch (Exception e) {
            log.error("评估线材数据失败，批次号：{}", wireMaterial.getBatchNumber(), e);
            wireMaterial.setEvaluationResult(WireMaterial.EvaluationResult.UNKNOWN);
            wireMaterial.setEvaluationMessage("评估过程发生错误：" + e.getMessage());
        }
        
        return wireMaterial;
    }
    
    @Override
    @Transactional
    public int reEvaluateByScenario(String scenarioCode) {
        try {
            // 获取应用场景下的所有线材数据
            List<WireMaterial> wireMaterials = wireMaterialRepository.findByScenarioCode(scenarioCode);
            
            if (wireMaterials.isEmpty()) {
                log.info("应用场景 {} 下没有线材数据需要重新评估", scenarioCode);
                return 0;
            }
            
            int evaluatedCount = 0;
            
            for (WireMaterial wireMaterial : wireMaterials) {
                try {
                    evaluateWireMaterial(wireMaterial);
                    wireMaterialRepository.save(wireMaterial);
                    evaluatedCount++;
                } catch (Exception e) {
                    log.error("重新评估线材数据失败，批次号：{}", wireMaterial.getBatchNumber(), e);
                }
            }
            
            log.info("完成应用场景 {} 的线材数据重新评估，共处理 {} 条数据", scenarioCode, evaluatedCount);
            return evaluatedCount;
            
        } catch (Exception e) {
            log.error("重新评估应用场景 {} 的线材数据失败", scenarioCode, e);
            throw new RuntimeException("重新评估失败：" + e.getMessage());
        }
    }
    
    /**
     * 检查数值是否在指定范围内
     */
    private boolean isInRange(BigDecimal value, BigDecimal min, BigDecimal max) {
        if (value == null) {
            return true; // 空值不参与评估
        }
        
        boolean inRange = true;
        
        if (min != null && value.compareTo(min) < 0) {
            inRange = false;
        }
        
        if (max != null && value.compareTo(max) > 0) {
            inRange = false;
        }
        
        return inRange;
    }
} 