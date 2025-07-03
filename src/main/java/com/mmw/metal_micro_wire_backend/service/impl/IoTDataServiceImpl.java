package com.mmw.metal_micro_wire_backend.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.mmw.metal_micro_wire_backend.entity.Device;
import com.mmw.metal_micro_wire_backend.entity.Question;
import com.mmw.metal_micro_wire_backend.entity.WireMaterial;
import com.mmw.metal_micro_wire_backend.config.HuaweiIotConfig;
import com.mmw.metal_micro_wire_backend.repository.DeviceRepository;
import com.mmw.metal_micro_wire_backend.repository.QuestionRepository;
import com.mmw.metal_micro_wire_backend.repository.WireMaterialRepository;
import com.mmw.metal_micro_wire_backend.service.IoTDataService;
import com.mmw.metal_micro_wire_backend.service.QualityEvaluationService;
import com.mmw.metal_micro_wire_backend.util.EncodingUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * IoT数据服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IoTDataServiceImpl implements IoTDataService {
    
    private final WireMaterialRepository wireMaterialRepository;
    private final DeviceRepository deviceRepository;
    private final QuestionRepository questionRepository;
    private final HuaweiIotConfig huaweiIotConfig;
    private final QualityEvaluationService qualityEvaluationService;
    
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");
    
    @Override
    @Transactional
    public WireMaterial saveWireMaterial(WireMaterial wireMaterial) {
        try {
            WireMaterial saved = wireMaterialRepository.save(wireMaterial);
            if (huaweiIotConfig.getMessage().isEnableDetailedLogging()) {
                log.info("成功保存线材数据，批次号: {}, 设备ID: {}", saved.getBatchNumber(), saved.getDeviceId());
            }
            return saved;
        } catch (Exception e) {
            log.error("保存线材数据失败", e);
            throw e;
        }
    }
    
    @Override
    @Transactional
    public Device saveDevice(Device device) {
        try {
            Device saved = deviceRepository.save(device);
            if (huaweiIotConfig.getMessage().isEnableDetailedLogging()) {
            log.info("成功保存设备状态，设备ID: {}, 状态: {}", saved.getDeviceId(), saved.getStatus());
            }
            return saved;
        } catch (Exception e) {
            log.error("保存设备状态失败", e);
            throw e;
        }
    }
    
    @Override
    @Transactional
    public Question saveQuestion(Question question) {
        try {
            Question saved = questionRepository.save(question);
            if (huaweiIotConfig.getMessage().isEnableDetailedLogging()) {
                log.info("成功保存问题数据，ID: {}, 设备ID: {}", saved.getId(), saved.getDeviceId());
            }
            return saved;
        } catch (Exception e) {
            log.error("保存问题数据失败", e);
            throw e;
        }
    }
    
    @Override
    @Transactional
    public WireMaterial parseAndSaveWireMaterial(JsonNode messageNode) {
        try {
            // 提取设备信息
            String deviceId = extractDeviceId(messageNode);
            LocalDateTime eventTime = extractEventTime(messageNode);
            
            // 提取线材属性数据
            JsonNode properties = extractProperties(messageNode);
            if (properties == null) {
                throw new IllegalArgumentException("未找到属性数据");
            }
            
            // 获取批次号和原始生产信息
            String batchNumber = getTextValue(properties, "Batch");
            String sourceOriginRaw = getTextValue(properties, "SourceOrigin");
            
            if (batchNumber == null || batchNumber.trim().isEmpty()) {
                throw new IllegalArgumentException("批次号不能为空");
            }
            
            // 解析批次号中的关键信息
            String scenarioCode = null;
            String deviceCode = null;
            
            if (batchNumber.length() >= 21) {
                // 批次号格式：Cu0120250629010010001
                // 3-4位：应用场景编号
                // 13-14位：检测机器号
                try {
                    scenarioCode = batchNumber.substring(2, 4);
                    deviceCode = batchNumber.substring(12, 14);
                    
                    if (huaweiIotConfig.getMessage().isEnableDetailedLogging()) {
                        log.info("解析批次号：{}，应用场景：{}，设备代码：{}", batchNumber, scenarioCode, deviceCode);
                    }
                } catch (Exception e) {
                    log.warn("解析批次号失败：{}，将保存原始数据", batchNumber, e);
                }
            } else {
                log.warn("批次号长度不足21位：{}，无法解析场景和设备代码", batchNumber);
            }
            
            // 解析生产信息
            String[] sourceInfo = EncodingUtil.parseSourceOrigin(sourceOriginRaw);
            
            WireMaterial wireMaterial = WireMaterial.builder()
                    .batchNumber(batchNumber)
                    .deviceId(deviceId)
                    .eventTime(eventTime)
                    .scenarioCode(scenarioCode)
                    .deviceCode(deviceCode)
                    .diameter(parseDecimalValue(properties, "DIR_s"))
                    .resistance(parseDecimalValue(properties, "RES_s"))
                    .extensibility(parseDecimalValue(properties, "EXT_s"))
                    .weight(parseDecimalValue(properties, "WEI_s"))
                    .sourceOriginRaw(sourceOriginRaw)
                    .manufacturer(sourceInfo[0])
                    .responsiblePerson(sourceInfo[1])
                    .processType(sourceInfo[2])
                    .productionMachine(sourceInfo[3])
                    .contactEmail(sourceInfo[4])
                    .build();
            
            // 使用综合质量评估服务进行评估（包括规则引擎和机器学习模型）
            wireMaterial = qualityEvaluationService.evaluateWireMaterial(wireMaterial);
            
            return saveWireMaterial(wireMaterial);
            
        } catch (Exception e) {
            throw new RuntimeException("解析并保存线材数据失败", e);
        }
    }
    
    @Override
    @Transactional
    public Device parseAndSaveDevice(JsonNode messageNode) {
        try {
            // 提取设备信息
            String deviceId = extractDeviceId(messageNode);
            
            // 提取设备状态
            JsonNode properties = extractProperties(messageNode);
            if (properties == null) {
                throw new IllegalArgumentException("未找到属性数据");
            }
            
            String status = getTextValue(properties, "STATUS");
            if (status == null) {
                throw new IllegalArgumentException("未找到STATUS字段");
            }
            
            // 验证并转换状态值
            Device.DeviceStatus deviceStatus;
            try {
                deviceStatus = Device.DeviceStatus.valueOf(status);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("无效的设备状态: " + status + "，只允许 ON 或 OFF");
            }
            
            // 查找现有设备
            Device device = deviceRepository.findById(deviceId)
                    .orElseThrow(() -> new IllegalArgumentException("设备不存在: " + deviceId));
            
            // 更新设备状态
            device.setStatus(deviceStatus);
            
            return saveDevice(device);
            
        } catch (Exception e) {
            throw new RuntimeException("解析并保存设备数据失败", e);
        }
    }
    
    @Override
    @Transactional
    public Question parseAndSaveQuestion(JsonNode messageNode) {
        try {
            // 提取设备信息
            String deviceId = extractDeviceId(messageNode);
            LocalDateTime eventTime = extractEventTime(messageNode);
            
            // 提取问题内容
            JsonNode properties = extractProperties(messageNode);
            if (properties == null) {
                throw new IllegalArgumentException("未找到属性数据");
            }
            
            String questionContent = EncodingUtil.decodeGbkHexToUtf8(getTextValue(properties, "AI"));
            if (questionContent == null) {
                throw new IllegalArgumentException("未找到AI字段");
            }
            
            Question question = Question.builder()
                    .deviceId(deviceId)
                    .eventTime(eventTime)
                    .questionContent(questionContent)
                    .responseStatus(0) // 默认未处理
                    .build();
            
            return saveQuestion(question);
            
        } catch (Exception e) {
            throw new RuntimeException("解析并保存问题数据失败", e);
        }
    }
    
    /**
     * 提取设备ID
     */
    private String extractDeviceId(JsonNode messageNode) {
        JsonNode notifyData = messageNode.get("notify_data");
        if (notifyData != null) {
            JsonNode header = notifyData.get("header");
            if (header != null) {
                return getTextValue(header, "device_id");
            }
        }
        throw new IllegalArgumentException("未找到设备ID");
    }
    
    /**
     * 提取事件时间（统一转为东八区Asia/Shanghai时间）
     */
    private LocalDateTime extractEventTime(JsonNode messageNode) {
        try {
            String eventTimeStr = getTextValue(messageNode, "event_time");
            if (eventTimeStr != null) {
                // 先按UTC解析，再转为东八区
                LocalDateTime utcTime = LocalDateTime.parse(eventTimeStr, ISO_FORMATTER);
                ZonedDateTime zonedUtc = utcTime.atZone(ZoneId.of("UTC"));
                ZonedDateTime zonedShanghai = zonedUtc.withZoneSameInstant(ZoneId.of("Asia/Shanghai"));
                return zonedShanghai.toLocalDateTime();
            }
        } catch (Exception e) {
            log.warn("解析事件时间失败，使用当前东八区时间", e);
        }
        // 返回当前东八区时间
        return LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
    }
    
    /**
     * 提取属性数据
     */
    private JsonNode extractProperties(JsonNode messageNode) {
        JsonNode notifyData = messageNode.get("notify_data");
        if (notifyData != null) {
            JsonNode body = notifyData.get("body");
            if (body != null) {
                JsonNode services = body.get("services");
                if (services != null && services.isArray() && services.size() > 0) {
                    JsonNode firstService = services.get(0);
                    return firstService.get("properties");
                }
            }
        }
        return null;
    }
    
    /**
     * 安全获取文本值
     */
    private String getTextValue(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.get(fieldName);
        return fieldNode != null && !fieldNode.isNull() ? fieldNode.asText() : null;
    }
    
    /**
     * 解析BigDecimal值
     */
    private BigDecimal parseDecimalValue(JsonNode node, String fieldName) {
        String textValue = getTextValue(node, fieldName);
        if (textValue != null) {
            try {
                return new BigDecimal(textValue);
            } catch (NumberFormatException e) {
                log.warn("解析数值字段 {} 失败: {}", fieldName, textValue);
            }
        }
        return null;
    }
    

}