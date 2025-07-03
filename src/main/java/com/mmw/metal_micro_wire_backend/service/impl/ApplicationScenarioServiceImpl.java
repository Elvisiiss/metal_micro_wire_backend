package com.mmw.metal_micro_wire_backend.service.impl;

import com.mmw.metal_micro_wire_backend.dto.BaseResponse;
import com.mmw.metal_micro_wire_backend.dto.scenario.*;
import com.mmw.metal_micro_wire_backend.entity.ApplicationScenario;
import com.mmw.metal_micro_wire_backend.repository.ApplicationScenarioRepository;
import com.mmw.metal_micro_wire_backend.service.ApplicationScenarioService;
import com.mmw.metal_micro_wire_backend.service.RuleEngineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 应用场景服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationScenarioServiceImpl implements ApplicationScenarioService {
    
    private final ApplicationScenarioRepository applicationScenarioRepository;
    private final RuleEngineService ruleEngineService;
    
    @Override
    public BaseResponse<ApplicationScenarioPageResponse> getScenarioList(ApplicationScenarioPageRequest request) {
        try {
            // 构建排序
            Sort.Direction direction = "desc".equalsIgnoreCase(request.getSortDirection()) 
                ? Sort.Direction.DESC : Sort.Direction.ASC;
            Sort sort = Sort.by(direction, request.getSortBy());
            Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
            
            // 构建查询条件
            Specification<ApplicationScenario> spec = (root, query, criteriaBuilder) -> {
                List<Predicate> predicates = new ArrayList<>();
                
                // 线材类型筛选
                if (StringUtils.hasText(request.getWireType())) {
                    predicates.add(criteriaBuilder.equal(root.get("wireType"), request.getWireType()));
                }
                
                // 应用场景名称关键词搜索
                if (StringUtils.hasText(request.getScenarioNameKeyword())) {
                    predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("scenarioName")), 
                        "%" + request.getScenarioNameKeyword().toLowerCase() + "%"
                    ));
                }
                
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            };
            
            // 查询数据
            Page<ApplicationScenario> scenarioPage = applicationScenarioRepository.findAll(spec, pageable);
            
            // 转换为响应DTO
            Page<ApplicationScenarioResponse> responsePage = scenarioPage.map(ApplicationScenarioResponse::fromEntity);
            ApplicationScenarioPageResponse pageResponse = ApplicationScenarioPageResponse.fromPage(responsePage);
            
            return BaseResponse.success(pageResponse);
        } catch (Exception e) {
            log.error("查询应用场景列表失败", e);
            return BaseResponse.error("查询应用场景列表失败：" + e.getMessage());
        }
    }
    
    @Override
    public BaseResponse<ApplicationScenarioResponse> getScenarioByCode(String scenarioCode) {
        try {
            Optional<ApplicationScenario> scenarioOpt = applicationScenarioRepository.findById(scenarioCode);
            if (scenarioOpt.isEmpty()) {
                return BaseResponse.error("应用场景不存在：" + scenarioCode);
            }
            
            return BaseResponse.success(ApplicationScenarioResponse.fromEntity(scenarioOpt.get()));
        } catch (Exception e) {
            log.error("查询应用场景失败，场景编号：{}", scenarioCode, e);
            return BaseResponse.error("查询应用场景失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public BaseResponse<ApplicationScenarioResponse> createScenario(CreateApplicationScenarioRequest request) {
        try {
            // 检查应用场景编号是否已存在
            if (applicationScenarioRepository.existsById(request.getScenarioCode())) {
                return BaseResponse.error("应用场景编号已存在：" + request.getScenarioCode());
            }
            
            // 验证上下限值的合理性
            if (request.getConductivityMin() != null && request.getConductivityMax() != null 
                && request.getConductivityMin().compareTo(request.getConductivityMax()) > 0) {
                return BaseResponse.error("电导率下限不能大于上限");
            }
            
            if (request.getExtensibilityMin() != null && request.getExtensibilityMax() != null 
                && request.getExtensibilityMin().compareTo(request.getExtensibilityMax()) > 0) {
                return BaseResponse.error("延展率下限不能大于上限");
            }
            
            if (request.getWeightMin() != null && request.getWeightMax() != null 
                && request.getWeightMin().compareTo(request.getWeightMax()) > 0) {
                return BaseResponse.error("重量下限不能大于上限");
            }
            
            if (request.getDiameterMin() != null && request.getDiameterMax() != null 
                && request.getDiameterMin().compareTo(request.getDiameterMax()) > 0) {
                return BaseResponse.error("直径下限不能大于上限");
            }
            
            // 创建应用场景
            ApplicationScenario scenario = ApplicationScenario.builder()
                    .scenarioCode(request.getScenarioCode())
                    .scenarioName(request.getScenarioName())
                    .wireType(request.getWireType())
                    .conductivityMin(request.getConductivityMin())
                    .conductivityMax(request.getConductivityMax())
                    .extensibilityMin(request.getExtensibilityMin())
                    .extensibilityMax(request.getExtensibilityMax())
                    .weightMin(request.getWeightMin())
                    .weightMax(request.getWeightMax())
                    .diameterMin(request.getDiameterMin())
                    .diameterMax(request.getDiameterMax())
                    .build();
            
            ApplicationScenario savedScenario = applicationScenarioRepository.save(scenario);
            log.info("创建应用场景成功，场景编号：{}，场景名称：{}", savedScenario.getScenarioCode(), savedScenario.getScenarioName());
            
            return BaseResponse.success(ApplicationScenarioResponse.fromEntity(savedScenario));
        } catch (Exception e) {
            log.error("创建应用场景失败", e);
            return BaseResponse.error("创建应用场景失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public BaseResponse<ApplicationScenarioResponse> updateScenario(String scenarioCode, UpdateApplicationScenarioRequest request) {
        try {
            Optional<ApplicationScenario> scenarioOpt = applicationScenarioRepository.findById(scenarioCode);
            if (scenarioOpt.isEmpty()) {
                return BaseResponse.error("应用场景不存在：" + scenarioCode);
            }
            
            // 验证上下限值的合理性
            if (request.getConductivityMin() != null && request.getConductivityMax() != null 
                && request.getConductivityMin().compareTo(request.getConductivityMax()) > 0) {
                return BaseResponse.error("电导率下限不能大于上限");
            }
            
            if (request.getExtensibilityMin() != null && request.getExtensibilityMax() != null 
                && request.getExtensibilityMin().compareTo(request.getExtensibilityMax()) > 0) {
                return BaseResponse.error("延展率下限不能大于上限");
            }
            
            if (request.getWeightMin() != null && request.getWeightMax() != null 
                && request.getWeightMin().compareTo(request.getWeightMax()) > 0) {
                return BaseResponse.error("重量下限不能大于上限");
            }
            
            if (request.getDiameterMin() != null && request.getDiameterMax() != null 
                && request.getDiameterMin().compareTo(request.getDiameterMax()) > 0) {
                return BaseResponse.error("直径下限不能大于上限");
            }
            
            ApplicationScenario scenario = scenarioOpt.get();
            
            // 更新字段
            scenario.setScenarioName(request.getScenarioName());
            scenario.setWireType(request.getWireType());
            scenario.setConductivityMin(request.getConductivityMin());
            scenario.setConductivityMax(request.getConductivityMax());
            scenario.setExtensibilityMin(request.getExtensibilityMin());
            scenario.setExtensibilityMax(request.getExtensibilityMax());
            scenario.setWeightMin(request.getWeightMin());
            scenario.setWeightMax(request.getWeightMax());
            scenario.setDiameterMin(request.getDiameterMin());
            scenario.setDiameterMax(request.getDiameterMax());
            
            ApplicationScenario savedScenario = applicationScenarioRepository.save(scenario);
            log.info("更新应用场景成功，场景编号：{}，场景名称：{}", savedScenario.getScenarioCode(), savedScenario.getScenarioName());
            
            // 异步触发重新评估该场景下的线材数据
            try {
                int evaluatedCount = ruleEngineService.reEvaluateByScenario(scenarioCode);
                log.info("应用场景更新后自动重新评估完成，场景编号：{}，处理数据条数：{}", scenarioCode, evaluatedCount);
            } catch (Exception e) {
                log.warn("应用场景更新后自动重新评估失败，场景编号：{}，错误：{}", scenarioCode, e.getMessage());
            }
            
            return BaseResponse.success(ApplicationScenarioResponse.fromEntity(savedScenario));
        } catch (Exception e) {
            log.error("更新应用场景失败，场景编号：{}", scenarioCode, e);
            return BaseResponse.error("更新应用场景失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public BaseResponse<Void> deleteScenario(String scenarioCode) {
        try {
            if (!applicationScenarioRepository.existsById(scenarioCode)) {
                return BaseResponse.error("应用场景不存在：" + scenarioCode);
            }
            
            applicationScenarioRepository.deleteById(scenarioCode);
            log.info("删除应用场景成功，场景编号：{}", scenarioCode);
            
            return BaseResponse.success("删除成功");
        } catch (Exception e) {
            log.error("删除应用场景失败，场景编号：{}", scenarioCode, e);
            return BaseResponse.error("删除应用场景失败：" + e.getMessage());
        }
    }
    
    @Override
    public BaseResponse<List<ApplicationScenarioResponse>> getScenariosByWireType(String wireType) {
        try {
            List<ApplicationScenario> scenarios = applicationScenarioRepository.findByWireType(wireType);
            List<ApplicationScenarioResponse> responses = scenarios.stream()
                    .map(ApplicationScenarioResponse::fromEntity)
                    .collect(Collectors.toList());
            
            return BaseResponse.success(responses);
        } catch (Exception e) {
            log.error("根据线材类型查询应用场景失败，线材类型：{}", wireType, e);
            return BaseResponse.error("查询应用场景失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public BaseResponse<String> reEvaluateWireMaterials(String scenarioCode) {
        try {
            // 验证应用场景是否存在
            if (!applicationScenarioRepository.existsById(scenarioCode)) {
                return BaseResponse.error("应用场景不存在：" + scenarioCode);
            }
            
            // 重新评估该场景下的所有线材数据
            int evaluatedCount = ruleEngineService.reEvaluateByScenario(scenarioCode);
            
            String message = String.format("应用场景 %s 下的线材数据重新评估完成，共处理 %d 条数据", scenarioCode, evaluatedCount);
            log.info(message);
            
            return BaseResponse.success(message);
        } catch (Exception e) {
            log.error("重新评估应用场景下的线材数据失败，场景编号：{}", scenarioCode, e);
            return BaseResponse.error("重新评估失败：" + e.getMessage());
        }
    }
} 