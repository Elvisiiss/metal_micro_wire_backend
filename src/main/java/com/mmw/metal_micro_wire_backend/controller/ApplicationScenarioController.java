package com.mmw.metal_micro_wire_backend.controller;

import com.mmw.metal_micro_wire_backend.dto.BaseResponse;
import com.mmw.metal_micro_wire_backend.dto.scenario.*;
import com.mmw.metal_micro_wire_backend.service.ApplicationScenarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import java.util.List;

/**
 * 应用场景管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/scenario")
@RequiredArgsConstructor
@Validated
public class ApplicationScenarioController {
    
    private final ApplicationScenarioService applicationScenarioService;
    
    /**
     * 分页查询应用场景列表
     * 权限：已认证用户
     */
    @GetMapping("/list")
    public ResponseEntity<BaseResponse<ApplicationScenarioPageResponse>> getScenarioList(
            @Valid ApplicationScenarioPageRequest request,
            HttpServletRequest httpRequest) {
        
        Long userId = (Long) httpRequest.getAttribute("userId");
        log.info("用户{}查询应用场景列表，页码：{}，每页大小：{}，线材类型筛选：{}，名称关键词：{}", 
                userId, request.getPage(), request.getSize(), request.getWireType(), request.getScenarioNameKeyword());
        
        BaseResponse<ApplicationScenarioPageResponse> response = applicationScenarioService.getScenarioList(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 根据应用场景编号查询应用场景信息
     * 权限：已认证用户
     */
    @GetMapping("/{scenarioCode}")
    public ResponseEntity<BaseResponse<ApplicationScenarioResponse>> getScenarioByCode(
            @PathVariable @Pattern(regexp = "^\\d{2}$", message = "应用场景编号必须是两位数字") String scenarioCode,
            HttpServletRequest httpRequest) {
        
        Long userId = (Long) httpRequest.getAttribute("userId");
        log.info("用户{}查询应用场景信息，场景编号：{}", userId, scenarioCode);
        
        BaseResponse<ApplicationScenarioResponse> response = applicationScenarioService.getScenarioByCode(scenarioCode);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 根据线材类型获取所有应用场景（不分页）
     * 权限：已认证用户
     */
    @GetMapping("/wire-type/{wireType}")
    public ResponseEntity<BaseResponse<List<ApplicationScenarioResponse>>> getScenariosByWireType(
            @PathVariable @Pattern(regexp = "^(Cu|Al|Ni|Ti|Zn)$", message = "线材类型必须是Cu、Al、Ni、Ti、Zn中的一种") String wireType,
            HttpServletRequest httpRequest) {
        
        Long userId = (Long) httpRequest.getAttribute("userId");
        log.info("用户{}根据线材类型查询应用场景，线材类型：{}", userId, wireType);
        
        BaseResponse<List<ApplicationScenarioResponse>> response = applicationScenarioService.getScenariosByWireType(wireType);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 创建应用场景
     * 权限：仅管理员用户（roleId=1）
     */
    @PostMapping
    public ResponseEntity<BaseResponse<ApplicationScenarioResponse>> createScenario(
            @Valid @RequestBody CreateApplicationScenarioRequest request,
            HttpServletRequest httpRequest) {
        
        // 权限检查：仅管理员
        Integer roleId = (Integer) httpRequest.getAttribute("roleId");
        Long userId = (Long) httpRequest.getAttribute("userId");
        
        if (roleId == null || roleId != 1) {
            log.warn("用户{}尝试创建应用场景但权限不足，roleId：{}", userId, roleId);
            return ResponseEntity.ok(BaseResponse.error("权限不足，仅管理员可创建应用场景"));
        }
        
        log.info("管理员用户{}创建应用场景，场景编号：{}，场景名称：{}", userId, request.getScenarioCode(), request.getScenarioName());
        
        BaseResponse<ApplicationScenarioResponse> response = applicationScenarioService.createScenario(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 更新应用场景
     * 权限：仅管理员用户（roleId=1）
     */
    @PutMapping("/{scenarioCode}")
    public ResponseEntity<BaseResponse<ApplicationScenarioResponse>> updateScenario(
            @PathVariable @Pattern(regexp = "^\\d{2}$", message = "应用场景编号必须是两位数字") String scenarioCode,
            @Valid @RequestBody UpdateApplicationScenarioRequest request,
            HttpServletRequest httpRequest) {
        
        // 权限检查：仅管理员
        Integer roleId = (Integer) httpRequest.getAttribute("roleId");
        Long userId = (Long) httpRequest.getAttribute("userId");
        
        if (roleId == null || roleId != 1) {
            log.warn("用户{}尝试更新应用场景但权限不足，roleId：{}", userId, roleId);
            return ResponseEntity.ok(BaseResponse.error("权限不足，仅管理员可更新应用场景"));
        }
        
        log.info("管理员用户{}更新应用场景，场景编号：{}，场景名称：{}", userId, scenarioCode, request.getScenarioName());
        
        BaseResponse<ApplicationScenarioResponse> response = applicationScenarioService.updateScenario(scenarioCode, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 删除应用场景
     * 权限：仅管理员用户（roleId=1）
     */
    @DeleteMapping("/{scenarioCode}")
    public ResponseEntity<BaseResponse<Void>> deleteScenario(
            @PathVariable @Pattern(regexp = "^\\d{2}$", message = "应用场景编号必须是两位数字") String scenarioCode,
            HttpServletRequest httpRequest) {
        
        // 权限检查：仅管理员
        Integer roleId = (Integer) httpRequest.getAttribute("roleId");
        Long userId = (Long) httpRequest.getAttribute("userId");
        
        if (roleId == null || roleId != 1) {
            log.warn("用户{}尝试删除应用场景但权限不足，roleId：{}", userId, roleId);
            return ResponseEntity.ok(BaseResponse.error("权限不足，仅管理员可删除应用场景"));
        }
        
        log.info("管理员用户{}删除应用场景，场景编号：{}", userId, scenarioCode);
        
        BaseResponse<Void> response = applicationScenarioService.deleteScenario(scenarioCode);
        return ResponseEntity.ok(response);
    }
} 