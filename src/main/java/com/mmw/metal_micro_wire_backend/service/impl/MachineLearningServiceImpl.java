package com.mmw.metal_micro_wire_backend.service.impl;

import com.mmw.metal_micro_wire_backend.dto.ml.*;
import com.mmw.metal_micro_wire_backend.entity.WireMaterial;
import com.mmw.metal_micro_wire_backend.service.MachineLearningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 机器学习服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MachineLearningServiceImpl implements MachineLearningService {
    
    private final RestTemplate restTemplate;
    
    @Value("${ml.model.api.url:http://localhost:5000}")
    private String mlApiUrl;
    
    @Value("${ml.model.api.timeout:30000}")
    private int timeout;
    
    @Override
    public ModelPredictionResponse predict(ModelPredictionRequest request) {
        try {
            log.info("调用机器学习模型进行单个预测，场景代码：{}", request.getScenarioCode());
            
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("scenario_code", request.getScenarioCode());
            requestBody.put("conductivity", request.getConductivity());
            requestBody.put("extensibility", request.getExtensibility());
            requestBody.put("diameter", request.getDiameter());
            
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            // 调用API
            String url = mlApiUrl + "/predict";
            ResponseEntity<ModelPredictionResponse> response = restTemplate.exchange(
                url, 
                HttpMethod.POST, 
                entity, 
                ModelPredictionResponse.class
            );
            
            ModelPredictionResponse result = response.getBody();
            if (result != null && Boolean.TRUE.equals(result.getSuccess())) {
                log.info("预测成功，结果：{}", result.getData().getPrediction());
                return result;
            } else {
                log.error("预测失败，错误信息：{}", result != null ? result.getError() : "响应为空");
                return ModelPredictionResponse.error("模型预测失败");
            }
            
        } catch (Exception e) {
            log.error("调用机器学习模型预测失败", e);
            return ModelPredictionResponse.error("调用机器学习模型失败：" + e.getMessage());
        }
    }
    
    @Override
    public BatchPredictionResponse predictBatch(BatchPredictionRequest request) {
        try {
            log.info("调用机器学习模型进行批量预测，样本数量：{}", request.getSamples().size());
            
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("samples", request.getSamples());
            
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            // 调用API
            String url = mlApiUrl + "/predict/batch";
            ResponseEntity<BatchPredictionResponse> response = restTemplate.exchange(
                url, 
                HttpMethod.POST, 
                entity, 
                BatchPredictionResponse.class
            );
            
            BatchPredictionResponse result = response.getBody();
            if (result != null && Boolean.TRUE.equals(result.getSuccess())) {
                log.info("批量预测成功，预测数量：{}", result.getCount());
                return result;
            } else {
                log.error("批量预测失败，错误信息：{}", result != null ? result.getError() : "响应为空");
                return BatchPredictionResponse.error("批量预测失败");
            }
            
        } catch (Exception e) {
            log.error("调用机器学习模型批量预测失败", e);
            return BatchPredictionResponse.error("调用机器学习模型失败：" + e.getMessage());
        }
    }
    
    @Override
    public ModelPredictionRequest createPredictionRequest(WireMaterial wireMaterial) {
        return ModelPredictionRequest.builder()
            .scenarioCode(wireMaterial.getScenarioCode())
            .conductivity(wireMaterial.getResistance())
            .extensibility(wireMaterial.getExtensibility())
            .diameter(wireMaterial.getDiameter())
            .build();
    }
    
    @Override
    public boolean checkHealth() {
        try {
            String url = mlApiUrl + "/health";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            log.error("检查机器学习模型健康状态失败", e);
            return false;
        }
    }
} 