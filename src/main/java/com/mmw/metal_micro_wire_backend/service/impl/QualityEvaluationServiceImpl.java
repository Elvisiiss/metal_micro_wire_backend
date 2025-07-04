package com.mmw.metal_micro_wire_backend.service.impl;

import com.mmw.metal_micro_wire_backend.dto.ml.ModelPredictionRequest;
import com.mmw.metal_micro_wire_backend.dto.ml.ModelPredictionResponse;
import com.mmw.metal_micro_wire_backend.dto.ml.PredictionData;
import com.mmw.metal_micro_wire_backend.entity.WireMaterial;
import com.mmw.metal_micro_wire_backend.repository.WireMaterialRepository;
import com.mmw.metal_micro_wire_backend.service.MachineLearningService;
import com.mmw.metal_micro_wire_backend.service.QualityEvaluationService;
import com.mmw.metal_micro_wire_backend.service.RuleEngineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.mmw.metal_micro_wire_backend.dto.quality.CompletedEvaluationPageRequest;
import com.mmw.metal_micro_wire_backend.dto.quality.PendingReviewPageRequest;
import com.mmw.metal_micro_wire_backend.dto.quality.QualityEvaluationPageResponse;
import com.mmw.metal_micro_wire_backend.dto.wirematerial.WireMaterialResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;

/**
 * 质量评估服务实现类
 * 整合规则引擎和机器学习模型的评估结果
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QualityEvaluationServiceImpl implements QualityEvaluationService {
    
    private final RuleEngineService ruleEngineService;
    private final MachineLearningService machineLearningService;
    private final WireMaterialRepository wireMaterialRepository;
    
    /**
     * 模型置信度阈值，低于此值需要人工审核
     */
    @Value("${ml.model.confidence.threshold:0.8}")
    private BigDecimal confidenceThreshold;
    
    @Override
    @Transactional
    public WireMaterial evaluateWireMaterial(WireMaterial wireMaterial) {
        log.info("开始综合评估线材质量，批次号：{}", wireMaterial.getBatchNumber());
        
        try {
            // 1. 规则引擎评估
            WireMaterial ruleEvaluated = ruleEngineService.evaluateWireMaterial(wireMaterial);
            WireMaterial.EvaluationResult ruleResult = ruleEvaluated.getEvaluationResult();
            log.info("规则引擎评估结果：{}", ruleResult.getDescription());
            
            // 2. 机器学习模型评估
            ModelPredictionRequest mlRequest = machineLearningService.createPredictionRequest(wireMaterial);
            ModelPredictionResponse mlResponse = machineLearningService.predict(mlRequest);
            
            WireMaterial.EvaluationResult modelResult = WireMaterial.EvaluationResult.UNKNOWN;
            BigDecimal modelConfidence = BigDecimal.ZERO;
            
            if (mlResponse.getSuccess() && mlResponse.getData() != null) {
                PredictionData predictionData = mlResponse.getData();
                modelResult = "合格".equals(predictionData.getPrediction()) ? 
                    WireMaterial.EvaluationResult.PASS : WireMaterial.EvaluationResult.FAIL;
                modelConfidence = predictionData.getConfidence();
                
                // 更新模型评估结果
                wireMaterial.setModelEvaluationResult(modelResult);
                wireMaterial.setModelConfidence(modelConfidence);
                
                log.info("机器学习模型评估结果：{}，置信度：{}", 
                    modelResult.getDescription(), modelConfidence);
            } else {
                log.warn("机器学习模型评估失败：{}", mlResponse.getError());
            }
            
            // 3. 确定最终评估结果
            WireMaterial.FinalEvaluationResult finalResult = determineFinalResult(
                ruleResult, modelResult, modelConfidence);
            wireMaterial.setFinalEvaluationResult(finalResult);
            
            log.info("最终评估结果：{}", finalResult.getDescription());
            
            // 4. 保存评估结果
            return wireMaterialRepository.save(wireMaterial);
            
        } catch (Exception e) {
            log.error("综合评估失败，批次号：{}", wireMaterial.getBatchNumber(), e);
            // 评估失败时，保持原有的规则引擎评估结果，设置最终结果为待审核
            wireMaterial.setFinalEvaluationResult(WireMaterial.FinalEvaluationResult.PENDING_REVIEW);
            return wireMaterialRepository.save(wireMaterial);
        }
    }
    
    @Override
    @Transactional
    public List<WireMaterial> evaluateBatch(List<WireMaterial> wireMaterials) {
        log.info("开始批量评估线材质量，数量：{}", wireMaterials.size());
        
        return wireMaterials.stream()
            .map(this::evaluateWireMaterial)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public int reEvaluateByScenario(String scenarioCode) {
        log.info("重新评估应用场景 {} 下的所有线材", scenarioCode);
        
        List<WireMaterial> materials = wireMaterialRepository.findByScenarioCode(scenarioCode);
        List<WireMaterial> evaluated = evaluateBatch(materials);
        
        log.info("重新评估完成，应用场景：{}，处理数量：{}", scenarioCode, evaluated.size());
        return evaluated.size();
    }
    
    @Override
    public List<WireMaterial> getPendingReviewMaterials() {
        // 返回未评估和待人工审核的线材
        List<WireMaterial.FinalEvaluationResult> pendingStates = Arrays.asList(
            WireMaterial.FinalEvaluationResult.UNKNOWN,
            WireMaterial.FinalEvaluationResult.PENDING_REVIEW
        );
        return wireMaterialRepository.findByFinalEvaluationResultIn(pendingStates);
    }
    
    @Override
    public List<WireMaterial> getCompletedMaterials() {
        // 返回已完成评估的线材（合格和不合格），供人工重新审核
        List<WireMaterial.FinalEvaluationResult> completedStates = Arrays.asList(
            WireMaterial.FinalEvaluationResult.PASS,
            WireMaterial.FinalEvaluationResult.FAIL
        );
        return wireMaterialRepository.findByFinalEvaluationResultIn(completedStates);
    }
    
    @Override
    public QualityEvaluationPageResponse getPendingReviewMaterials(PendingReviewPageRequest request) {
        // 构建排序
        Sort.Direction direction = "desc".equalsIgnoreCase(request.getSortDirection()) 
            ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        
        // 查询待审核状态的线材
        List<WireMaterial.FinalEvaluationResult> pendingStates = Arrays.asList(
            WireMaterial.FinalEvaluationResult.UNKNOWN,
            WireMaterial.FinalEvaluationResult.PENDING_REVIEW
        );
        
        Page<WireMaterial> wireMaterialPage = wireMaterialRepository.findByFinalEvaluationResultIn(pendingStates, pageable);
        
        // 转换为响应DTO
        Page<WireMaterialResponse> responsePage = wireMaterialPage.map(WireMaterialResponse::fromEntity);
        return QualityEvaluationPageResponse.fromPage(responsePage);
    }
    
    @Override
    public QualityEvaluationPageResponse getCompletedMaterials(CompletedEvaluationPageRequest request) {
        // 构建排序
        Sort.Direction direction = "desc".equalsIgnoreCase(request.getSortDirection()) 
            ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        
        // 构建查询条件
        Specification<WireMaterial> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // 已完成评估状态筛选
            List<WireMaterial.FinalEvaluationResult> completedStates = Arrays.asList(
                WireMaterial.FinalEvaluationResult.PASS,
                WireMaterial.FinalEvaluationResult.FAIL
            );
            predicates.add(root.get("finalEvaluationResult").in(completedStates));
            
            // 应用场景编号筛选（可选）
            if (StringUtils.hasText(request.getScenarioCode())) {
                predicates.add(criteriaBuilder.equal(root.get("scenarioCode"), request.getScenarioCode()));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        
        Page<WireMaterial> wireMaterialPage = wireMaterialRepository.findAll(spec, pageable);
        
        // 转换为响应DTO
        Page<WireMaterialResponse> responsePage = wireMaterialPage.map(WireMaterialResponse::fromEntity);
        return QualityEvaluationPageResponse.fromPage(responsePage);
    }
    
    @Override
    @Transactional
    public boolean confirmFinalResult(String batchNumber, 
                                     WireMaterial.FinalEvaluationResult finalResult, 
                                     String reviewRemark,
                                     String reviewerName,
                                     String reviewerEmail) {

        
        try {
            WireMaterial wireMaterial = wireMaterialRepository.findById(batchNumber)
                .orElseThrow(() -> new RuntimeException("线材不存在，批次号：" + batchNumber));
            
            WireMaterial.FinalEvaluationResult originalResult = wireMaterial.getFinalEvaluationResult();
            wireMaterial.setFinalEvaluationResult(finalResult);
            
            // 更新评估详情，添加审核备注和审核人员信息
            String originalMessage = wireMaterial.getEvaluationMessage();
            String reviewType = (originalResult == WireMaterial.FinalEvaluationResult.PASS || 
                                originalResult == WireMaterial.FinalEvaluationResult.FAIL) ? 
                                "人工重新审核" : "人工审核";
            String reviewerInfo = (reviewerName != null && !reviewerName.isEmpty()) ? 
                                 reviewerName + "(" + reviewerEmail + ")" : reviewerEmail;
            String updatedMessage = originalMessage + " | " + reviewType + "：" + 
                                  (reviewRemark != null ? reviewRemark : "无备注") + 
                                  " [审核人：" + reviewerInfo + "]";
            wireMaterial.setEvaluationMessage(updatedMessage);
            
            wireMaterialRepository.save(wireMaterial);
            
            log.info("{}完成，批次号：{}，原结果：{}，新结果：{}", 
                reviewType, batchNumber, originalResult.getDescription(), finalResult.getDescription());
            
            return true;
        } catch (Exception e) {
            log.error("人工审核确认失败，批次号：{}", batchNumber, e);
            return false;
        }
    }
    
    /**
     * 根据规则引擎和模型评估结果确定最终评估结果
     */
    private WireMaterial.FinalEvaluationResult determineFinalResult(
            WireMaterial.EvaluationResult ruleResult, 
            WireMaterial.EvaluationResult modelResult, 
            BigDecimal modelConfidence) {
        
        // 模型评估失败或未评估
        if (modelResult == WireMaterial.EvaluationResult.UNKNOWN) {
            log.info("模型评估失败，依赖规则引擎结果，需人工审核");
            return WireMaterial.FinalEvaluationResult.PENDING_REVIEW;
        }
        
        // 置信度过低，需要人工审核
        if (modelConfidence.compareTo(confidenceThreshold) < 0) {
            log.info("模型置信度过低（{}），需人工审核", modelConfidence);
            return WireMaterial.FinalEvaluationResult.PENDING_REVIEW;
        }
        
        // 规则引擎和模型结果不一致，需要人工审核
        if (ruleResult != modelResult) {
            log.info("规则引擎（{}）和模型（{}）评估结果不一致，需人工审核", 
                ruleResult.getDescription(), modelResult.getDescription());
            return WireMaterial.FinalEvaluationResult.PENDING_REVIEW;
        }
        
        // 结果一致且置信度高，直接确定最终结果
        log.info("规则引擎和模型评估结果一致且置信度高，自动确定最终结果：{}", 
            modelResult.getDescription());
        
        return modelResult == WireMaterial.EvaluationResult.PASS ? 
            WireMaterial.FinalEvaluationResult.PASS : 
            WireMaterial.FinalEvaluationResult.FAIL;
    }
} 