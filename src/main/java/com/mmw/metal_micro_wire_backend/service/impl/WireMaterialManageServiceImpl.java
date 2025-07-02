package com.mmw.metal_micro_wire_backend.service.impl;

import com.mmw.metal_micro_wire_backend.dto.BaseResponse;
import com.mmw.metal_micro_wire_backend.dto.wirematerial.UpdateWireMaterialRequest;
import com.mmw.metal_micro_wire_backend.dto.wirematerial.WireMaterialPageRequest;
import com.mmw.metal_micro_wire_backend.dto.wirematerial.WireMaterialPageResponse;
import com.mmw.metal_micro_wire_backend.dto.wirematerial.WireMaterialResponse;
import com.mmw.metal_micro_wire_backend.entity.WireMaterial;
import com.mmw.metal_micro_wire_backend.repository.WireMaterialRepository;
import com.mmw.metal_micro_wire_backend.service.WireMaterialManageService;
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

/**
 * 线材管理服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WireMaterialManageServiceImpl implements WireMaterialManageService {
    
    private final WireMaterialRepository wireMaterialRepository;
    
    @Override
    public BaseResponse<WireMaterialPageResponse> getWireMaterialList(WireMaterialPageRequest request) {
        try {
            // 构建排序
            Sort.Direction direction = "desc".equalsIgnoreCase(request.getSortDirection()) 
                ? Sort.Direction.DESC : Sort.Direction.ASC;
            Sort sort = Sort.by(direction, request.getSortBy());
            Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
            
            // 构建查询条件
            Specification<WireMaterial> spec = (root, query, criteriaBuilder) -> {
                List<Predicate> predicates = new ArrayList<>();
                
                // 批次号关键词搜索
                if (StringUtils.hasText(request.getBatchNumberKeyword())) {
                    predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("batchNumber")), 
                        "%" + request.getBatchNumberKeyword().toLowerCase() + "%"
                    ));
                }
                
                // 设备ID关键词搜索
                if (StringUtils.hasText(request.getDeviceIdKeyword())) {
                    predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("deviceId")), 
                        "%" + request.getDeviceIdKeyword().toLowerCase() + "%"
                    ));
                }
                
                // 生产商关键词搜索
                if (StringUtils.hasText(request.getManufacturerKeyword())) {
                    predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("manufacturer")), 
                        "%" + request.getManufacturerKeyword().toLowerCase() + "%"
                    ));
                }
                
                // 负责人关键词搜索
                if (StringUtils.hasText(request.getResponsiblePersonKeyword())) {
                    predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("responsiblePerson")), 
                        "%" + request.getResponsiblePersonKeyword().toLowerCase() + "%"
                    ));
                }
                
                // 工艺类型关键词搜索
                if (StringUtils.hasText(request.getProcessTypeKeyword())) {
                    predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("processType")), 
                        "%" + request.getProcessTypeKeyword().toLowerCase() + "%"
                    ));
                }
                
                // 生产机器关键词搜索
                if (StringUtils.hasText(request.getProductionMachineKeyword())) {
                    predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("productionMachine")), 
                        "%" + request.getProductionMachineKeyword().toLowerCase() + "%"
                    ));
                }
                
                // 应用场景编号筛选
                if (StringUtils.hasText(request.getScenarioCode())) {
                    predicates.add(criteriaBuilder.equal(root.get("scenarioCode"), request.getScenarioCode()));
                }
                
                // 设备代码筛选
                if (StringUtils.hasText(request.getDeviceCode())) {
                    predicates.add(criteriaBuilder.equal(root.get("deviceCode"), request.getDeviceCode()));
                }
                
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            };
            
            // 查询数据
            Page<WireMaterial> wireMaterialPage = wireMaterialRepository.findAll(spec, pageable);
            
            // 转换为响应DTO
            Page<WireMaterialResponse> responsePage = wireMaterialPage.map(WireMaterialResponse::fromEntity);
            WireMaterialPageResponse pageResponse = WireMaterialPageResponse.fromPage(responsePage);
            
            return BaseResponse.success(pageResponse);
        } catch (Exception e) {
            log.error("查询线材列表失败", e);
            return BaseResponse.error("查询线材列表失败：" + e.getMessage());
        }
    }
    
    @Override
    public BaseResponse<WireMaterialResponse> getWireMaterialByBatchNumber(String batchNumber) {
        try {
            Optional<WireMaterial> wireMaterialOpt = wireMaterialRepository.findById(batchNumber);
            if (wireMaterialOpt.isEmpty()) {
                return BaseResponse.error("线材记录不存在：" + batchNumber);
            }
            
            return BaseResponse.success(WireMaterialResponse.fromEntity(wireMaterialOpt.get()));
        } catch (Exception e) {
            log.error("查询线材信息失败，批次号：{}", batchNumber, e);
            return BaseResponse.error("查询线材信息失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public BaseResponse<WireMaterialResponse> updateWireMaterial(String batchNumber, UpdateWireMaterialRequest request) {
        try {
            Optional<WireMaterial> wireMaterialOpt = wireMaterialRepository.findById(batchNumber);
            if (wireMaterialOpt.isEmpty()) {
                return BaseResponse.error("线材记录不存在：" + batchNumber);
            }
            
            WireMaterial wireMaterial = wireMaterialOpt.get();
            
            // 更新可编辑字段
            if (request.getDiameter() != null) {
                wireMaterial.setDiameter(request.getDiameter());
            }
            if (request.getResistance() != null) {
                wireMaterial.setResistance(request.getResistance());
            }
            if (request.getExtensibility() != null) {
                wireMaterial.setExtensibility(request.getExtensibility());
            }
            if (request.getWeight() != null) {
                wireMaterial.setWeight(request.getWeight());
            }
            if (StringUtils.hasText(request.getManufacturer())) {
                wireMaterial.setManufacturer(request.getManufacturer());
            }
            if (StringUtils.hasText(request.getResponsiblePerson())) {
                wireMaterial.setResponsiblePerson(request.getResponsiblePerson());
            }
            if (StringUtils.hasText(request.getProcessType())) {
                wireMaterial.setProcessType(request.getProcessType());
            }
            if (StringUtils.hasText(request.getProductionMachine())) {
                wireMaterial.setProductionMachine(request.getProductionMachine());
            }
            if (StringUtils.hasText(request.getContactEmail())) {
                wireMaterial.setContactEmail(request.getContactEmail());
            }
            
            WireMaterial savedWireMaterial = wireMaterialRepository.save(wireMaterial);
            log.info("线材信息更新成功，批次号：{}", batchNumber);
            
            return BaseResponse.success(WireMaterialResponse.fromEntity(savedWireMaterial));
        } catch (Exception e) {
            log.error("更新线材信息失败，批次号：{}", batchNumber, e);
            return BaseResponse.error("更新线材信息失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public BaseResponse<Void> deleteWireMaterial(String batchNumber) {
        try {
            if (!wireMaterialRepository.existsById(batchNumber)) {
                return BaseResponse.error("线材记录不存在：" + batchNumber);
            }
            
            wireMaterialRepository.deleteById(batchNumber);
            log.info("线材记录删除成功，批次号：{}", batchNumber);
            
            return BaseResponse.success(null);
        } catch (Exception e) {
            log.error("删除线材记录失败，批次号：{}", batchNumber, e);
            return BaseResponse.error("删除线材记录失败：" + e.getMessage());
        }
    }
} 