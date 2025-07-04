package com.mmw.metal_micro_wire_backend.service;

import com.mmw.metal_micro_wire_backend.dto.BaseResponse;
import com.mmw.metal_micro_wire_backend.dto.scenario.*;

import java.util.List;

/**
 * 应用场景服务接口
 */
public interface ApplicationScenarioService {
    
    /**
     * 分页查询应用场景列表
     * @param request 查询请求
     * @return 分页响应
     */
    BaseResponse<ApplicationScenarioPageResponse> getScenarioList(ApplicationScenarioPageRequest request);
    
    /**
     * 根据应用场景编号获取应用场景信息
     * @param scenarioCode 应用场景编号
     * @return 应用场景信息
     */
    BaseResponse<ApplicationScenarioResponse> getScenarioByCode(String scenarioCode);
    
    /**
     * 创建应用场景
     * @param request 创建请求
     * @return 操作结果
     */
    BaseResponse<ApplicationScenarioResponse> createScenario(CreateApplicationScenarioRequest request);
    
    /**
     * 更新应用场景
     * @param scenarioCode 应用场景编号
     * @param request 更新请求
     * @return 操作结果
     */
    BaseResponse<ApplicationScenarioResponse> updateScenario(String scenarioCode, UpdateApplicationScenarioRequest request);
    
    /**
     * 删除应用场景
     * @param scenarioCode 应用场景编号
     * @return 操作结果
     */
    BaseResponse<Void> deleteScenario(String scenarioCode);
    
    /**
     * 根据线材类型获取所有应用场景（不分页）
     * @param wireType 线材类型
     * @return 应用场景列表
     */
    BaseResponse<List<ApplicationScenarioResponse>> getScenariosByWireType(String wireType);
    
    /**
     * 重新评估指定应用场景下的所有线材数据（规则引擎）
     * @param scenarioCode 应用场景编号
     * @return 重新评估结果
     */
    BaseResponse<String> reEvaluateWireMaterials(String scenarioCode);
} 