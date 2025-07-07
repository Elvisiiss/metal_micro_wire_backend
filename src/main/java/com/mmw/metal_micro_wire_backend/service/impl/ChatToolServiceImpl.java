package com.mmw.metal_micro_wire_backend.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmw.metal_micro_wire_backend.dto.BaseResponse;
import com.mmw.metal_micro_wire_backend.dto.chat.ChatToolCall;
import com.mmw.metal_micro_wire_backend.dto.device.DevicePageRequest;
import com.mmw.metal_micro_wire_backend.dto.device.DevicePageResponse;
import com.mmw.metal_micro_wire_backend.entity.Device;
import com.mmw.metal_micro_wire_backend.dto.overview.OverallStatisticsResponse;
import com.mmw.metal_micro_wire_backend.dto.overview.ScenarioStatisticsResponse;
import com.mmw.metal_micro_wire_backend.dto.overview.YearlyStatisticsResponse;
import com.mmw.metal_micro_wire_backend.dto.traceability.QualityIssueResponse;
import com.mmw.metal_micro_wire_backend.dto.traceability.QualityStatisticsResponse;
import com.mmw.metal_micro_wire_backend.dto.traceability.TraceabilityAnalysisResponse;
import com.mmw.metal_micro_wire_backend.dto.traceability.TraceabilityQueryRequest;
import com.mmw.metal_micro_wire_backend.dto.wirematerial.WireMaterialPageRequest;
import com.mmw.metal_micro_wire_backend.dto.wirematerial.WireMaterialPageResponse;
import com.mmw.metal_micro_wire_backend.dto.wirematerial.WireMaterialResponse;
import com.mmw.metal_micro_wire_backend.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * AI工具调用服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatToolServiceImpl implements ChatToolService {
    
    private final DeviceService deviceService;
    private final WireMaterialManageService wireMaterialManageService;
    private final TraceabilityService traceabilityService;
    private final OverViewService overViewService;
    private final ObjectMapper objectMapper;
    
    @Override
    public List<ChatToolCall.Tool> getAvailableTools() {
        List<ChatToolCall.Tool> tools = new ArrayList<>();
        
        // 1. 设备管理工具
        tools.add(createDeviceListTool());
        tools.add(createDeviceInfoTool());
        
        // 2. 线材管理工具
        tools.add(createWireMaterialListTool());
        tools.add(createWireMaterialInfoTool());
        
        // 3. 质量溯源工具
        tools.add(createQualityAnalysisTool());
        tools.add(createQualityIssuesTool());
        tools.add(createManufacturerRankingTool());
        
        // 4. 统计概览工具
        tools.add(createOverallStatisticsTool());
        tools.add(createYearlyStatisticsTool());
        tools.add(createScenarioStatisticsTool());
        
        return tools;
    }
    
    @Override
    public ChatToolCall.ToolCallResult executeToolCall(ChatToolCall.ToolCallRequest toolCall, Long userId) {
        ChatToolCall.ToolCallResult result = new ChatToolCall.ToolCallResult();
        result.setToolCallId(toolCall.getId());
        
        try {
            String functionName = toolCall.getFunction().getName();
            String arguments = toolCall.getFunction().getArguments();
            JsonNode argsNode = objectMapper.readTree(arguments);
            
            String response = switch (functionName) {
                case "get_device_list" -> executeGetDeviceList(argsNode);
                case "get_device_info" -> executeGetDeviceInfo(argsNode);
                case "get_wire_material_list" -> executeGetWireMaterialList(argsNode);
                case "get_wire_material_info" -> executeGetWireMaterialInfo(argsNode);
                case "analyze_quality_issues" -> executeAnalyzeQualityIssues(argsNode);
                case "get_quality_issues" -> executeGetQualityIssues(argsNode);
                case "get_manufacturer_ranking" -> executeGetManufacturerRanking(argsNode);
                case "get_overall_statistics" -> executeGetOverallStatistics();
                case "get_yearly_statistics" -> executeGetYearlyStatistics();
                case "get_scenario_statistics" -> executeGetScenarioStatistics(argsNode);
                default -> "未知的工具调用：" + functionName;
            };
            
            result.setResult(response);
            result.setSuccess(true);
            
        } catch (Exception e) {
            log.error("工具调用执行失败：{}", toolCall.getFunction().getName(), e);
            result.setResult("工具调用执行失败");
            result.setError(e.getMessage());
            result.setSuccess(false);
        }
        
        return result;
    }
    
    // ==================== 工具定义方法 ====================
    
    private ChatToolCall.Tool createDeviceListTool() {
        ChatToolCall.Tool tool = new ChatToolCall.Tool();
        ChatToolCall.Function function = new ChatToolCall.Function();
        function.setName("get_device_list");
        function.setDescription("获取设备列表，支持分页和状态筛选");
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("type", "object");
        Map<String, Object> properties = new HashMap<>();
        
        Map<String, Object> pageParam = new HashMap<>();
        pageParam.put("type", "integer");
        pageParam.put("description", "页码，从1开始");
        pageParam.put("default", 1);
        properties.put("page", pageParam);
        
        Map<String, Object> sizeParam = new HashMap<>();
        sizeParam.put("type", "integer");
        sizeParam.put("description", "每页大小");
        sizeParam.put("default", 10);
        properties.put("size", sizeParam);
        
        Map<String, Object> statusParam = new HashMap<>();
        statusParam.put("type", "string");
        statusParam.put("description", "设备状态筛选，ON表示开启，OFF表示关闭");
        statusParam.put("enum", Arrays.asList("ON", "OFF"));
        properties.put("status", statusParam);
        
        parameters.put("properties", properties);
        function.setParameters(parameters);
        tool.setFunction(function);
        
        return tool;
    }
    
    private ChatToolCall.Tool createDeviceInfoTool() {
        ChatToolCall.Tool tool = new ChatToolCall.Tool();
        ChatToolCall.Function function = new ChatToolCall.Function();
        function.setName("get_device_info");
        function.setDescription("根据设备ID获取设备详细信息");
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("type", "object");
        Map<String, Object> properties = new HashMap<>();
        
        Map<String, Object> deviceIdParam = new HashMap<>();
        deviceIdParam.put("type", "string");
        deviceIdParam.put("description", "设备ID");
        properties.put("deviceId", deviceIdParam);
        
        parameters.put("properties", properties);
        parameters.put("required", Arrays.asList("deviceId"));
        function.setParameters(parameters);
        tool.setFunction(function);
        
        return tool;
    }
    
    private ChatToolCall.Tool createWireMaterialListTool() {
        ChatToolCall.Tool tool = new ChatToolCall.Tool();
        ChatToolCall.Function function = new ChatToolCall.Function();
        function.setName("get_wire_material_list");
        function.setDescription("获取金属微丝检测数据列表，支持分页和多种筛选条件");
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("type", "object");
        Map<String, Object> properties = new HashMap<>();
        
        Map<String, Object> pageParam = new HashMap<>();
        pageParam.put("type", "integer");
        pageParam.put("description", "页码，从1开始");
        pageParam.put("default", 1);
        properties.put("page", pageParam);
        
        Map<String, Object> sizeParam = new HashMap<>();
        sizeParam.put("type", "integer");
        sizeParam.put("description", "每页大小");
        sizeParam.put("default", 10);
        properties.put("size", sizeParam);
        
        Map<String, Object> batchNumberParam = new HashMap<>();
        batchNumberParam.put("type", "string");
        batchNumberParam.put("description", "批次号筛选");
        properties.put("batchNumber", batchNumberParam);
        
        Map<String, Object> manufacturerParam = new HashMap<>();
        manufacturerParam.put("type", "string");
        manufacturerParam.put("description", "生产商筛选");
        properties.put("manufacturer", manufacturerParam);
        
        Map<String, Object> scenarioCodeParam = new HashMap<>();
        scenarioCodeParam.put("type", "string");
        scenarioCodeParam.put("description", "应用场景编号筛选");
        properties.put("scenarioCode", scenarioCodeParam);
        
        parameters.put("properties", properties);
        function.setParameters(parameters);
        tool.setFunction(function);
        
        return tool;
    }
    
    private ChatToolCall.Tool createWireMaterialInfoTool() {
        ChatToolCall.Tool tool = new ChatToolCall.Tool();
        ChatToolCall.Function function = new ChatToolCall.Function();
        function.setName("get_wire_material_info");
        function.setDescription("根据批次号获取金属微丝的详细检测信息");
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("type", "object");
        Map<String, Object> properties = new HashMap<>();
        
        Map<String, Object> batchNumberParam = new HashMap<>();
        batchNumberParam.put("type", "string");
        batchNumberParam.put("description", "批次号");
        properties.put("batchNumber", batchNumberParam);
        
        parameters.put("properties", properties);
        parameters.put("required", Arrays.asList("batchNumber"));
        function.setParameters(parameters);
        tool.setFunction(function);
        
        return tool;
    }
    
    private ChatToolCall.Tool createQualityAnalysisTool() {
        ChatToolCall.Tool tool = new ChatToolCall.Tool();
        ChatToolCall.Function function = new ChatToolCall.Function();
        function.setName("analyze_quality_issues");
        function.setDescription("执行质量问题溯源分析，分析指定条件下的质量问题");
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("type", "object");
        Map<String, Object> properties = new HashMap<>();
        
        Map<String, Object> dimensionParam = new HashMap<>();
        dimensionParam.put("type", "string");
        dimensionParam.put("description", "分析维度：manufacturer（生产商）、responsiblePerson（负责人）、processType（工艺类型）、productionMachine（生产机器）、scenarioCode（应用场景）");
        dimensionParam.put("enum", Arrays.asList("manufacturer", "responsiblePerson", "processType", "productionMachine", "scenarioCode"));
        properties.put("dimension", dimensionParam);
        
        Map<String, Object> dimensionValueParam = new HashMap<>();
        dimensionValueParam.put("type", "string");
        dimensionValueParam.put("description", "维度值，如具体的生产商名称");
        properties.put("dimensionValue", dimensionValueParam);
        
        Map<String, Object> startTimeParam = new HashMap<>();
        startTimeParam.put("type", "string");
        startTimeParam.put("description", "开始时间，格式：yyyy-MM-dd HH:mm:ss");
        properties.put("startTime", startTimeParam);
        
        Map<String, Object> endTimeParam = new HashMap<>();
        endTimeParam.put("type", "string");
        endTimeParam.put("description", "结束时间，格式：yyyy-MM-dd HH:mm:ss");
        properties.put("endTime", endTimeParam);
        
        parameters.put("properties", properties);
        parameters.put("required", Arrays.asList("dimension", "dimensionValue"));
        function.setParameters(parameters);
        tool.setFunction(function);
        
        return tool;
    }
    
    private ChatToolCall.Tool createQualityIssuesTool() {
        ChatToolCall.Tool tool = new ChatToolCall.Tool();
        ChatToolCall.Function function = new ChatToolCall.Function();
        function.setName("get_quality_issues");
        function.setDescription("获取质量问题列表，识别不合格的线材批次");
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("type", "object");
        Map<String, Object> properties = new HashMap<>();
        
        Map<String, Object> dimensionParam = new HashMap<>();
        dimensionParam.put("type", "string");
        dimensionParam.put("description", "查询维度");
        properties.put("dimension", dimensionParam);
        
        Map<String, Object> dimensionValueParam = new HashMap<>();
        dimensionValueParam.put("type", "string");
        dimensionValueParam.put("description", "维度值");
        properties.put("dimensionValue", dimensionValueParam);
        
        parameters.put("properties", properties);
        function.setParameters(parameters);
        tool.setFunction(function);
        
        return tool;
    }
    
    private ChatToolCall.Tool createManufacturerRankingTool() {
        ChatToolCall.Tool tool = new ChatToolCall.Tool();
        ChatToolCall.Function function = new ChatToolCall.Function();
        function.setName("get_manufacturer_ranking");
        function.setDescription("获取生产商质量排名，分析各生产商的质量表现");
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("type", "object");
        Map<String, Object> properties = new HashMap<>();
        
        Map<String, Object> startTimeParam = new HashMap<>();
        startTimeParam.put("type", "string");
        startTimeParam.put("description", "开始时间，格式：yyyy-MM-dd HH:mm:ss");
        properties.put("startTime", startTimeParam);
        
        Map<String, Object> endTimeParam = new HashMap<>();
        endTimeParam.put("type", "string");
        endTimeParam.put("description", "结束时间，格式：yyyy-MM-dd HH:mm:ss");
        properties.put("endTime", endTimeParam);
        
        Map<String, Object> scenarioCodeParam = new HashMap<>();
        scenarioCodeParam.put("type", "string");
        scenarioCodeParam.put("description", "应用场景编号筛选");
        properties.put("scenarioCode", scenarioCodeParam);
        
        parameters.put("properties", properties);
        function.setParameters(parameters);
        tool.setFunction(function);
        
        return tool;
    }
    
    private ChatToolCall.Tool createOverallStatisticsTool() {
        ChatToolCall.Tool tool = new ChatToolCall.Tool();
        ChatToolCall.Function function = new ChatToolCall.Function();
        function.setName("get_overall_statistics");
        function.setDescription("获取系统总体统计数据，包括总检测数量、合格率、设备数量等");
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("type", "object");
        parameters.put("properties", new HashMap<>());
        function.setParameters(parameters);
        tool.setFunction(function);
        
        return tool;
    }
    
    private ChatToolCall.Tool createYearlyStatisticsTool() {
        ChatToolCall.Tool tool = new ChatToolCall.Tool();
        ChatToolCall.Function function = new ChatToolCall.Function();
        function.setName("get_yearly_statistics");
        function.setDescription("获取最近12个月的年度检测数据统计");
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("type", "object");
        parameters.put("properties", new HashMap<>());
        function.setParameters(parameters);
        tool.setFunction(function);
        
        return tool;
    }
    
    private ChatToolCall.Tool createScenarioStatisticsTool() {
        ChatToolCall.Tool tool = new ChatToolCall.Tool();
        ChatToolCall.Function function = new ChatToolCall.Function();
        function.setName("get_scenario_statistics");
        function.setDescription("根据时间范围获取应用场景统计数据");
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("type", "object");
        Map<String, Object> properties = new HashMap<>();
        
        Map<String, Object> howParam = new HashMap<>();
        howParam.put("type", "string");
        howParam.put("description", "时间范围");
        howParam.put("enum", Arrays.asList("this_month", "last_month", "this_year", "last_year", "all"));
        properties.put("how", howParam);
        
        parameters.put("properties", properties);
        parameters.put("required", Arrays.asList("how"));
        function.setParameters(parameters);
        tool.setFunction(function);
        
        return tool;
    }
    
    // ==================== 工具执行方法 ====================
    
    private String executeGetDeviceList(JsonNode args) {
        try {
            DevicePageRequest request = new DevicePageRequest();
            if (args.has("page")) {
                request.setPage(args.get("page").asInt());
            }
            if (args.has("size")) {
                request.setSize(args.get("size").asInt());
            }
            if (args.has("status")) {
                String statusStr = args.get("status").asText();
                try {
                    request.setStatus(Device.DeviceStatus.valueOf(statusStr));
                } catch (IllegalArgumentException e) {
                    return "无效的设备状态：" + statusStr + "，只允许 ON 或 OFF";
                }
            }
            
            BaseResponse<DevicePageResponse> response = deviceService.getDeviceList(request);
            if ("success".equals(response.getCode())) {
                return objectMapper.writeValueAsString(response.getData());
            } else {
                return "获取设备列表失败：" + response.getMsg();
            }
        } catch (Exception e) {
            return "获取设备列表出错：" + e.getMessage();
        }
    }
    
    private String executeGetDeviceInfo(JsonNode args) {
        try {
            String deviceId = args.get("deviceId").asText();
            BaseResponse response = deviceService.getDeviceById(deviceId);
            if ("success".equals(response.getCode())) {
                return objectMapper.writeValueAsString(response.getData());
            } else {
                return "获取设备信息失败：" + response.getMsg();
            }
        } catch (Exception e) {
            return "获取设备信息出错：" + e.getMessage();
        }
    }
    
    private String executeGetWireMaterialList(JsonNode args) {
        try {
            WireMaterialPageRequest request = new WireMaterialPageRequest();
            if (args.has("page")) {
                request.setPage(args.get("page").asInt());
            }
            if (args.has("size")) {
                request.setSize(args.get("size").asInt());
            }
            if (args.has("batchNumber")) {
                request.setBatchNumberKeyword(args.get("batchNumber").asText());
            }
            if (args.has("manufacturer")) {
                request.setManufacturerKeyword(args.get("manufacturer").asText());
            }
            if (args.has("scenarioCode")) {
                request.setScenarioCode(args.get("scenarioCode").asText());
            }
            
            BaseResponse<WireMaterialPageResponse> response = wireMaterialManageService.getWireMaterialList(request);
            if ("success".equals(response.getCode())) {
                return objectMapper.writeValueAsString(response.getData());
            } else {
                return "获取线材列表失败：" + response.getMsg();
            }
        } catch (Exception e) {
            return "获取线材列表出错：" + e.getMessage();
        }
    }
    
    private String executeGetWireMaterialInfo(JsonNode args) {
        try {
            String batchNumber = args.get("batchNumber").asText();
            BaseResponse<WireMaterialResponse> response = wireMaterialManageService.getWireMaterialByBatchNumber(batchNumber);
            if ("success".equals(response.getCode())) {
                return objectMapper.writeValueAsString(response.getData());
            } else {
                return "获取线材信息失败：" + response.getMsg();
            }
        } catch (Exception e) {
            return "获取线材信息出错：" + e.getMessage();
        }
    }
    
    private String executeAnalyzeQualityIssues(JsonNode args) {
        try {
            TraceabilityQueryRequest request = new TraceabilityQueryRequest();
            
            // 转换维度枚举
            String dimensionStr = args.get("dimension").asText();
            TraceabilityQueryRequest.QueryDimension dimension;
            switch (dimensionStr) {
                case "manufacturer":
                    dimension = TraceabilityQueryRequest.QueryDimension.MANUFACTURER;
                    break;
                case "responsiblePerson":
                    dimension = TraceabilityQueryRequest.QueryDimension.RESPONSIBLE_PERSON;
                    break;
                case "processType":
                    dimension = TraceabilityQueryRequest.QueryDimension.PROCESS_TYPE;
                    break;
                case "productionMachine":
                    dimension = TraceabilityQueryRequest.QueryDimension.PRODUCTION_MACHINE;
                    break;
                default:
                    return "无效的分析维度：" + dimensionStr;
            }
            request.setDimension(dimension);
            request.setDimensionValue(args.get("dimensionValue").asText());
            
            if (args.has("startTime")) {
                String startTimeStr = args.get("startTime").asText();
                try {
                    LocalDateTime startTime = LocalDateTime.parse(startTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    request.setStartTime(startTime);
                } catch (Exception e) {
                    return "开始时间格式错误，请使用 yyyy-MM-dd HH:mm:ss 格式";
                }
            }
            if (args.has("endTime")) {
                String endTimeStr = args.get("endTime").asText();
                try {
                    LocalDateTime endTime = LocalDateTime.parse(endTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    request.setEndTime(endTime);
                } catch (Exception e) {
                    return "结束时间格式错误，请使用 yyyy-MM-dd HH:mm:ss 格式";
                }
            }
            
            BaseResponse<TraceabilityAnalysisResponse> response = traceabilityService.performTraceabilityAnalysis(request);
            if ("success".equals(response.getCode())) {
                return objectMapper.writeValueAsString(response.getData());
            } else {
                return "质量溯源分析失败：" + response.getMsg();
            }
        } catch (Exception e) {
            return "质量溯源分析出错：" + e.getMessage();
        }
    }
    
    private String executeGetQualityIssues(JsonNode args) {
        try {
            TraceabilityQueryRequest request = new TraceabilityQueryRequest();
            if (args.has("dimension")) {
                String dimensionStr = args.get("dimension").asText();
                TraceabilityQueryRequest.QueryDimension dimension;
                switch (dimensionStr) {
                    case "manufacturer":
                        dimension = TraceabilityQueryRequest.QueryDimension.MANUFACTURER;
                        break;
                    case "responsiblePerson":
                        dimension = TraceabilityQueryRequest.QueryDimension.RESPONSIBLE_PERSON;
                        break;
                    case "processType":
                        dimension = TraceabilityQueryRequest.QueryDimension.PROCESS_TYPE;
                        break;
                    case "productionMachine":
                        dimension = TraceabilityQueryRequest.QueryDimension.PRODUCTION_MACHINE;
                        break;
                    default:
                        return "无效的查询维度：" + dimensionStr;
                }
                request.setDimension(dimension);
            }
            if (args.has("dimensionValue")) {
                request.setDimensionValue(args.get("dimensionValue").asText());
            }
            
            BaseResponse<List<QualityIssueResponse>> response = traceabilityService.identifyQualityIssues(request);
            if ("success".equals(response.getCode())) {
                return objectMapper.writeValueAsString(response.getData());
            } else {
                return "获取质量问题失败：" + response.getMsg();
            }
        } catch (Exception e) {
            return "获取质量问题出错：" + e.getMessage();
        }
    }
    
    private String executeGetManufacturerRanking(JsonNode args) {
        try {
            String startTime = args.has("startTime") ? args.get("startTime").asText() : null;
            String endTime = args.has("endTime") ? args.get("endTime").asText() : null;
            String scenarioCode = args.has("scenarioCode") ? args.get("scenarioCode").asText() : null;
            
            BaseResponse<List<QualityStatisticsResponse>> response = traceabilityService.getManufacturerRanking(startTime, endTime, scenarioCode);
            if ("success".equals(response.getCode())) {
                return objectMapper.writeValueAsString(response.getData());
            } else {
                return "获取生产商排名失败：" + response.getMsg();
            }
        } catch (Exception e) {
            return "获取生产商排名出错：" + e.getMessage();
        }
    }
    
    private String executeGetOverallStatistics() {
        try {
            BaseResponse<OverallStatisticsResponse> response = overViewService.getOverallStatistics();
            if ("success".equals(response.getCode())) {
                return objectMapper.writeValueAsString(response.getData());
            } else {
                return "获取总体统计失败：" + response.getMsg();
            }
        } catch (Exception e) {
            return "获取总体统计出错：" + e.getMessage();
        }
    }
    
    private String executeGetYearlyStatistics() {
        try {
            BaseResponse<YearlyStatisticsResponse> response = overViewService.getYearlyStatistics();
            if ("success".equals(response.getCode())) {
                return objectMapper.writeValueAsString(response.getData());
            } else {
                return "获取年度统计失败：" + response.getMsg();
            }
        } catch (Exception e) {
            return "获取年度统计出错：" + e.getMessage();
        }
    }
    
    private String executeGetScenarioStatistics(JsonNode args) {
        try {
            String how = args.get("how").asText();
            BaseResponse<ScenarioStatisticsResponse> response = overViewService.getScenarioStatistics(how);
            if ("success".equals(response.getCode())) {
                return objectMapper.writeValueAsString(response.getData());
            } else {
                return "获取场景统计失败：" + response.getMsg();
            }
        } catch (Exception e) {
            return "获取场景统计出错：" + e.getMessage();
        }
    }
} 