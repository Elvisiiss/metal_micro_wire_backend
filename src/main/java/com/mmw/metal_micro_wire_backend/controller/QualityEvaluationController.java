package com.mmw.metal_micro_wire_backend.controller;

import com.mmw.metal_micro_wire_backend.dto.BaseResponse;
import com.mmw.metal_micro_wire_backend.dto.ml.BatchPredictionRequest;
import com.mmw.metal_micro_wire_backend.dto.ml.BatchPredictionResponse;
import com.mmw.metal_micro_wire_backend.dto.ml.ModelPredictionRequest;
import com.mmw.metal_micro_wire_backend.dto.ml.ModelPredictionResponse;
import com.mmw.metal_micro_wire_backend.dto.ml.PredictionData;
import com.mmw.metal_micro_wire_backend.entity.WireMaterial;
import com.mmw.metal_micro_wire_backend.service.MachineLearningService;
import com.mmw.metal_micro_wire_backend.service.QualityEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 质量评估控制器
 * 提供机器学习模型和质量评估的相关API
 */
@RestController
@RequestMapping("/api/quality")
@RequiredArgsConstructor
@Slf4j
public class QualityEvaluationController {
    
    private final MachineLearningService machineLearningService;
    private final QualityEvaluationService qualityEvaluationService;
    
    /**
     * 机器学习模型单个预测
     */
    @PostMapping("/predict")
    public BaseResponse<PredictionData> predict(@RequestBody ModelPredictionRequest request) {
        log.info("接收到机器学习模型预测请求，场景代码：{}", request.getScenarioCode());
        ModelPredictionResponse response = machineLearningService.predict(request);
        if (response.getSuccess()) {
            return BaseResponse.success("预测成功", response.getData());
        } else {
            return BaseResponse.error("预测失败：" + response.getError());
        }
    }
    
    /**
     * 机器学习模型批量预测
     */
    @PostMapping("/predict/batch")
    public BaseResponse<List<PredictionData>> predictBatch(@RequestBody BatchPredictionRequest request) {
        log.info("接收到机器学习模型批量预测请求，样本数量：{}", request.getSamples().size());
        BatchPredictionResponse response = machineLearningService.predictBatch(request);
        if (response.getSuccess()) {
            return BaseResponse.success("批量预测成功", response.getData());
        } else {
            return BaseResponse.error("批量预测失败：" + response.getError());
        }
    }
    
    /**
     * 检查机器学习模型健康状态
     */
    @GetMapping("/health")
    public BaseResponse<Boolean> checkHealth() {
        boolean healthy = machineLearningService.checkHealth();
        return healthy ? 
            BaseResponse.success("机器学习模型服务正常", healthy) : 
            BaseResponse.error("机器学习模型服务异常");
    }
    
    /**
     * 重新评估指定场景的所有线材
     */
    @PostMapping("/scenario/{scenarioCode}/re-evaluate")
    public BaseResponse<String> reEvaluateScenario(@PathVariable String scenarioCode) {
        log.info("开始重新评估应用场景 {} 的线材质量", scenarioCode);
        try {
            int count = qualityEvaluationService.reEvaluateByScenario(scenarioCode);
            String message = String.format("应用场景 %s 下的线材数据重新评估完成，共处理 %d 条数据", scenarioCode, count);
            return BaseResponse.success(message);
        } catch (Exception e) {
            log.error("重新评估失败", e);
            return BaseResponse.error("重新评估失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取需要人工审核的线材列表
     */
    @GetMapping("/pending-review")
    public BaseResponse<List<WireMaterial>> getPendingReviewMaterials() {
        try {
            List<WireMaterial> materials = qualityEvaluationService.getPendingReviewMaterials();
            return BaseResponse.success("获取待审核线材成功", materials);
        } catch (Exception e) {
            log.error("获取待审核线材失败", e);
            return BaseResponse.error("获取待审核线材失败：" + e.getMessage());
        }
    }
    
    /**
     * 人工审核确认最终结果
     */
    @PostMapping("/confirm-result")
    public BaseResponse<String> confirmFinalResult(
            @RequestParam String batchNumber,
            @RequestParam WireMaterial.FinalEvaluationResult finalResult,
            @RequestParam(required = false) String reviewRemark) {
        
        log.info("收到人工审核确认请求，批次号：{}，最终结果：{}", batchNumber, finalResult);
        try {
            boolean success = qualityEvaluationService.confirmFinalResult(
                batchNumber, finalResult, reviewRemark != null ? reviewRemark : "");
            
            if (success) {
                return BaseResponse.success("人工审核确认成功");
            } else {
                return BaseResponse.error("人工审核确认失败");
            }
        } catch (Exception e) {
            log.error("人工审核确认失败", e);
            return BaseResponse.error("人工审核确认失败：" + e.getMessage());
        }
    }
} 