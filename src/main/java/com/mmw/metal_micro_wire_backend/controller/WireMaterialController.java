package com.mmw.metal_micro_wire_backend.controller;

import com.mmw.metal_micro_wire_backend.dto.BaseResponse;
import com.mmw.metal_micro_wire_backend.dto.wirematerial.UpdateWireMaterialRequest;
import com.mmw.metal_micro_wire_backend.dto.wirematerial.WireMaterialPageRequest;
import com.mmw.metal_micro_wire_backend.dto.wirematerial.WireMaterialPageResponse;
import com.mmw.metal_micro_wire_backend.dto.wirematerial.WireMaterialResponse;
import com.mmw.metal_micro_wire_backend.service.WireMaterialManageService;
import com.mmw.metal_micro_wire_backend.service.TokenService;
import com.mmw.metal_micro_wire_backend.service.QualityEvaluationService;
import com.mmw.metal_micro_wire_backend.entity.WireMaterial;
import com.mmw.metal_micro_wire_backend.repository.WireMaterialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

/**
 * 线材管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/wire-material")
@RequiredArgsConstructor
@Validated
public class WireMaterialController {
    
    private final WireMaterialManageService wireMaterialManageService;
    private final QualityEvaluationService qualityEvaluationService;
    private final WireMaterialRepository wireMaterialRepository;
    
    /**
     * 分页查询线材列表
     * 权限：已认证用户
     */
    @GetMapping("/list")
    public ResponseEntity<BaseResponse<WireMaterialPageResponse>> getWireMaterialList(
            @Valid WireMaterialPageRequest request,
            HttpServletRequest httpRequest) {
        
        String userName = (String) httpRequest.getAttribute("userName");
        Long userId = (Long) httpRequest.getAttribute("userId");
        log.info("用户{}(ID:{})查询线材列表，页码：{}，每页大小：{}", userName, userId, request.getPage(), request.getSize());
        
        BaseResponse<WireMaterialPageResponse> response = wireMaterialManageService.getWireMaterialList(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 根据批次号查询线材信息
     * 权限：无需认证（公开接口）
     */
    @GetMapping("/{batchNumber}")
    public ResponseEntity<BaseResponse<WireMaterialResponse>> getWireMaterialByBatchNumber(
            @PathVariable @NotBlank(message = "批次号不能为空") String batchNumber,
            HttpServletRequest httpRequest) {
        
        log.info("查询线材信息，批次号：{}", batchNumber);
        
        BaseResponse<WireMaterialResponse> response = wireMaterialManageService.getWireMaterialByBatchNumber(batchNumber);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 更新线材信息
     * 权限：管理员（roleId=1）
     */
    @PutMapping("/{batchNumber}")
    public ResponseEntity<BaseResponse<WireMaterialResponse>> updateWireMaterial(
            @PathVariable @NotBlank(message = "批次号不能为空") String batchNumber,
            @Valid @RequestBody UpdateWireMaterialRequest request,
            HttpServletRequest httpRequest) {
        
        // 验证管理员权限（不包括Root用户）
        if (!hasManagerPermission(httpRequest)) {
            return ResponseEntity.ok(BaseResponse.error("权限不足，仅管理员可操作"));
        }
        
        String userName = (String) httpRequest.getAttribute("userName");
        log.info("管理员{}更新线材信息，批次号：{}", userName, batchNumber);
        
        BaseResponse<WireMaterialResponse> response = wireMaterialManageService.updateWireMaterial(batchNumber, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 删除线材记录
     * 权限：管理员（roleId=1）
     */
    @DeleteMapping("/{batchNumber}")
    public ResponseEntity<BaseResponse<Void>> deleteWireMaterial(
            @PathVariable @NotBlank(message = "批次号不能为空") String batchNumber,
            HttpServletRequest httpRequest) {
        
        // 验证管理员权限（不包括Root用户）
        if (!hasManagerPermission(httpRequest)) {
            return ResponseEntity.ok(BaseResponse.error("权限不足，仅管理员可操作"));
        }
        
        String userName = (String) httpRequest.getAttribute("userName");
        log.info("管理员{}删除线材记录，批次号：{}", userName, batchNumber);
        
        BaseResponse<Void> response = wireMaterialManageService.deleteWireMaterial(batchNumber);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 手动评估线材质量（规则引擎+机器学习模型）
     * 权限：管理员（roleId=1）
     */
    @PostMapping("/{batchNumber}/evaluate")
    public ResponseEntity<BaseResponse<WireMaterialResponse>> evaluateWireMaterial(
            @PathVariable @NotBlank(message = "批次号不能为空") String batchNumber,
            HttpServletRequest httpRequest) {
        
        // 验证管理员权限
        if (!hasManagerPermission(httpRequest)) {
            return ResponseEntity.ok(BaseResponse.error("权限不足，仅管理员可操作"));
        }
        
        String userName = (String) httpRequest.getAttribute("userName");
        log.info("管理员{}手动评估线材质量，批次号：{}", userName, batchNumber);
        
        try {
            // 检查线材是否存在
            WireMaterial wireMaterial = wireMaterialRepository.findById(batchNumber)
                .orElse(null);
            
            if (wireMaterial == null) {
                return ResponseEntity.ok(BaseResponse.error("线材记录不存在：" + batchNumber));
            }
            
            // 执行综合质量评估
            WireMaterial evaluated = qualityEvaluationService.evaluateWireMaterial(wireMaterial);
            
            // 转换为响应格式
            WireMaterialResponse response = WireMaterialResponse.fromEntity(evaluated);
            
            return ResponseEntity.ok(BaseResponse.success("线材质量评估完成", response));
            
        } catch (Exception e) {
            log.error("手动评估线材质量失败", e);
            return ResponseEntity.ok(BaseResponse.error("评估失败：" + e.getMessage()));
        }
    }
    
    /**
     * 验证是否有管理员权限（仅限普通用户中的管理员，不包括Root用户）
     * @param request HttpServletRequest
     * @return true: 有权限，false: 无权限
     */
    private boolean hasManagerPermission(HttpServletRequest request) {
        TokenService.UserType userType = (TokenService.UserType) request.getAttribute("userType");
        Integer roleId = (Integer) request.getAttribute("roleId");
        
        // Root用户不具有线材管理权限
        if (userType == TokenService.UserType.ROOT) {
            return false;
        }
        
        // 只有普通用户中的管理员角色（roleId = 1）才有权限
        return roleId != null && roleId == 1;
    }
} 