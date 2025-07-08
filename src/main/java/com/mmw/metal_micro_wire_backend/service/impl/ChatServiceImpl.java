package com.mmw.metal_micro_wire_backend.service.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmw.metal_micro_wire_backend.config.DeepSeekConfig;
import com.mmw.metal_micro_wire_backend.dto.chat.*;
import com.mmw.metal_micro_wire_backend.service.ChatService;
import com.mmw.metal_micro_wire_backend.service.ChatToolService;
import com.mmw.metal_micro_wire_backend.service.RedisService;
import com.mmw.metal_micro_wire_backend.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 聊天服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
    
    private final DeepSeekConfig deepSeekConfig;
    private final RedisService redisService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ChatToolService chatToolService;
    
    @Override
    public ChatMessageResponse sendMessage(Long userId, TokenService.UserType userType, ChatMessageRequest request) {
        try {
            // 检查服务是否启用
            if (!deepSeekConfig.getApi().isEnabled()) {
                throw new RuntimeException("DeepSeek服务已禁用");
            }
            
            String sessionId = request.getSessionId();
            boolean isNewSession = false;
            
            // 如果没有会话ID，创建新会话
            if (sessionId == null || sessionId.trim().isEmpty()) {
                sessionId = generateSessionId();
                isNewSession = true;
            }
            
            // 获取或创建会话
            ChatSession session = getOrCreateSession(userId, userType, sessionId, request.getTitle(), isNewSession);
            
            // 获取消息历史
            List<ChatMessage> messageHistory = getMessageHistory(userId, sessionId);
            
            // 构建API请求
            String response = callDeepSeekApi(messageHistory, request.getMessage(), userId);
            
            // 保存消息
            saveMessage(userId, sessionId, request.getMessage(), response, messageHistory);
            
            // 更新会话信息
            updateSessionInfo(userId, sessionId, session, response);
            
            // 构建响应
            ChatMessageResponse messageResponse = new ChatMessageResponse();
            messageResponse.setMessageId(generateMessageId());
            messageResponse.setSessionId(sessionId);
            messageResponse.setUserMessage(request.getMessage());
            messageResponse.setAssistantMessage(response);
            messageResponse.setCreateTime(LocalDateTime.now());
            messageResponse.setNewSession(isNewSession);
            messageResponse.setSessionTitle(session.getTitle());
            
            return messageResponse;
            
        } catch (Exception e) {
            log.error("发送聊天消息失败，用户ID：{}，错误：{}", userId, e.getMessage(), e);
            throw new RuntimeException("发送消息失败：" + e.getMessage());
        }
    }
    
    @Override
    public ChatSessionResponse createSession(Long userId, TokenService.UserType userType, CreateChatSessionRequest request) {
        try {
            // 检查用户会话数量限制
            int sessionCount = redisService.getUserChatSessionCount(userId);
            if (sessionCount >= deepSeekConfig.getSession().getMaxSessionsPerUser()) {
                throw new RuntimeException("会话数量已达到上限：" + deepSeekConfig.getSession().getMaxSessionsPerUser());
            }
            
            String sessionId = generateSessionId();
            ChatSession session = new ChatSession();
            session.setSessionId(sessionId);
            session.setTitle(request.getTitle());
            session.setUserId(userId);
            session.setUserType(userType);
            session.setCreateTime(LocalDateTime.now());
            session.setUpdateTime(LocalDateTime.now());
            session.setMessageCount(0);
            session.setLastMessage("");
            
            // 保存会话
            saveSession(userId, session);
            
            // 构建响应
            ChatSessionResponse response = new ChatSessionResponse();
            response.setSessionId(sessionId);
            response.setTitle(request.getTitle());
            response.setUserId(userId);
            response.setCreateTime(LocalDateTime.now());
            response.setUpdateTime(LocalDateTime.now());
            response.setMessageCount(0);
            response.setLastMessage("");
            
            return response;
            
        } catch (Exception e) {
            log.error("创建聊天会话失败，用户ID：{}，错误：{}", userId, e.getMessage(), e);
            throw new RuntimeException("创建会话失败：" + e.getMessage());
        }
    }
    
    @Override
    public ChatSessionListResponse getUserSessions(Long userId, TokenService.UserType userType) {
        try {
            // 清理过期会话
            redisService.cleanupExpiredChatSessions(userId);
            
            Set<String> sessionIds = redisService.getUserChatSessions(userId);
            List<ChatSessionResponse> sessions = new ArrayList<>();
            
            if (sessionIds != null && !sessionIds.isEmpty()) {
                for (String sessionId : sessionIds) {
                    ChatSession session = getSession(userId, sessionId);
                    if (session != null) {
                        ChatSessionResponse response = new ChatSessionResponse();
                        response.setSessionId(session.getSessionId());
                        response.setTitle(session.getTitle());
                        response.setUserId(session.getUserId());
                        response.setCreateTime(session.getCreateTime());
                        response.setUpdateTime(session.getUpdateTime());
                        response.setMessageCount(session.getMessageCount());
                        response.setLastMessage(session.getLastMessage());
                        sessions.add(response);
                    }
                }
            }
            
            // 按更新时间倒序排序
            sessions.sort((a, b) -> b.getUpdateTime().compareTo(a.getUpdateTime()));
            
            ChatSessionListResponse response = new ChatSessionListResponse();
            response.setSessions(sessions);
            response.setTotal(sessions.size());
            
            return response;
            
        } catch (Exception e) {
            log.error("获取用户会话列表失败，用户ID：{}，错误：{}", userId, e.getMessage(), e);
            throw new RuntimeException("获取会话列表失败：" + e.getMessage());
        }
    }
    
    @Override
    public ChatHistoryResponse getChatHistory(Long userId, TokenService.UserType userType, String sessionId) {
        try {
            // 获取会话信息
            ChatSession session = getSession(userId, sessionId);
            if (session == null) {
                throw new RuntimeException("会话不存在");
            }
            
            // 获取消息历史
            List<ChatMessage> messageHistory = getMessageHistory(userId, sessionId);
            
            // 构建响应
            ChatHistoryResponse response = new ChatHistoryResponse();
            response.setSessionId(sessionId);
            response.setTitle(session.getTitle());
            response.setCreateTime(session.getCreateTime());
            response.setMessageCount(messageHistory.size());
            
            // 转换消息格式
            List<ChatHistoryResponse.ChatMessage> messages = messageHistory.stream()
                    .map(msg -> {
                        ChatHistoryResponse.ChatMessage chatMsg = new ChatHistoryResponse.ChatMessage();
                        chatMsg.setMessageId(msg.getMessageId());
                        chatMsg.setRole(msg.getRole());
                        chatMsg.setContent(msg.getContent());
                        chatMsg.setTimestamp(msg.getTimestamp());
                        return chatMsg;
                    })
                    .collect(Collectors.toList());
            
            response.setMessages(messages);
            
            return response;
            
        } catch (Exception e) {
            log.error("获取聊天历史失败，用户ID：{}，会话ID：{}，错误：{}", userId, sessionId, e.getMessage(), e);
            throw new RuntimeException("获取聊天历史失败：" + e.getMessage());
        }
    }
    
    @Override
    public void deleteSession(Long userId, TokenService.UserType userType, String sessionId) {
        try {
            // 验证会话是否存在
            ChatSession session = getSession(userId, sessionId);
            if (session == null) {
                throw new RuntimeException("会话不存在");
            }
            
            // 删除会话
            redisService.deleteChatSession(userId, sessionId);
            
            log.info("聊天会话已删除，用户ID：{}，会话ID：{}", userId, sessionId);
            
        } catch (Exception e) {
            log.error("删除聊天会话失败，用户ID：{}，会话ID：{}，错误：{}", userId, sessionId, e.getMessage(), e);
            throw new RuntimeException("删除会话失败：" + e.getMessage());
        }
    }
    
    @Override
    public void updateSessionTitle(Long userId, TokenService.UserType userType, String sessionId, String title) {
        try {
            // 验证会话是否存在
            ChatSession session = getSession(userId, sessionId);
            if (session == null) {
                throw new RuntimeException("会话不存在");
            }
            
            // 更新标题
            session.setTitle(title);
            session.setUpdateTime(LocalDateTime.now());
            
            // 保存会话
            saveSession(userId, session);
            
            log.info("会话标题已更新，用户ID：{}，会话ID：{}，新标题：{}", userId, sessionId, title);
            
        } catch (Exception e) {
            log.error("更新会话标题失败，用户ID：{}，会话ID：{}，错误：{}", userId, sessionId, e.getMessage(), e);
            throw new RuntimeException("更新会话标题失败：" + e.getMessage());
        }
    }
    
    @Override
    public void cleanupExpiredSessions(Long userId, TokenService.UserType userType) {
        try {
            redisService.cleanupExpiredChatSessions(userId);
            log.info("用户过期会话清理完成，用户ID：{}", userId);
        } catch (Exception e) {
            log.error("清理过期会话失败，用户ID：{}，错误：{}", userId, e.getMessage(), e);
            throw new RuntimeException("清理过期会话失败：" + e.getMessage());
        }
    }
    
    // ==================== 私有方法 ====================
    
    private String callDeepSeekApi(List<ChatMessage> messageHistory, String newMessage, Long userId) throws Exception {
        // 构建消息列表
        List<Map<String, String>> messages = new ArrayList<>();
        
        // 添加系统消息
        messages.add(Map.of("role", "system", "content", buildSystemPrompt()));
        
        // 添加历史消息
        for (ChatMessage msg : messageHistory) {
            messages.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
        }
        
        // 添加新消息
        messages.add(Map.of("role", "user", "content", newMessage));
        
        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", deepSeekConfig.getModel().getDefaultModel());
        requestBody.put("messages", messages);
        requestBody.put("temperature", deepSeekConfig.getModel().getTemperature());
        requestBody.put("max_tokens", deepSeekConfig.getModel().getMaxTokens());
        
        // 添加工具调用支持
        List<ChatToolCall.Tool> tools = chatToolService.getAvailableTools();
        if (!tools.isEmpty()) {
            requestBody.put("tools", tools);
            requestBody.put("tool_choice", "auto");
        }
        
        // 构建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + deepSeekConfig.getApi().getApiKey());
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        
        // 发送请求
        String url = deepSeekConfig.getApi().getBaseUrl() + "/v1/chat/completions";
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
        
        // 解析响应
        JsonNode responseJson = objectMapper.readTree(response.getBody());
        log.debug("DeepSeek API响应：{}", responseJson.toString());

        JsonNode choices = responseJson.get("choices");

        if (choices != null && choices.isArray() && choices.size() > 0) {
            JsonNode firstChoice = choices.get(0);
            JsonNode message = firstChoice.get("message");
            if (message != null) {
                // 检查是否有工具调用
                JsonNode toolCalls = message.get("tool_calls");
                if (toolCalls != null && toolCalls.isArray() && toolCalls.size() > 0) {
                    log.info("检测到标准OpenAI格式的工具调用，数量：{}", toolCalls.size());
                    return handleToolCalls(toolCalls, messageHistory, newMessage, userId);
                }
                
                // 普通文本响应
                JsonNode content = message.get("content");
                if (content != null) {
                    String contentText = content.asText();

                    // 检查是否包含DeepSeek特殊格式的工具调用标记
                    if (containsToolCallMarkers(contentText)) {
                        log.info("检测到DeepSeek特殊格式的工具调用，开始解析和执行");
                        return handleDeepSeekToolCalls(contentText, messageHistory, newMessage, userId);
                    }

                    return contentText;
                }
            }
        }
        
        throw new RuntimeException("DeepSeek API响应格式不正确");
    }
    
    private ChatSession getOrCreateSession(Long userId, TokenService.UserType userType, String sessionId, String title, boolean isNewSession) throws JsonProcessingException {
        if (isNewSession) {
            // 创建新会话
            ChatSession session = new ChatSession();
            session.setSessionId(sessionId);
            session.setTitle(title != null && !title.trim().isEmpty() ? title : "新的对话");
            session.setUserId(userId);
            session.setUserType(userType);
            session.setCreateTime(LocalDateTime.now());
            session.setUpdateTime(LocalDateTime.now());
            session.setMessageCount(0);
            session.setLastMessage("");
            
            // 保存会话
            saveSession(userId, session);
            
            return session;
        } else {
            // 获取现有会话
            ChatSession session = getSession(userId, sessionId);
            if (session == null) {
                throw new RuntimeException("会话不存在");
            }
            return session;
        }
    }
    
    private void saveSession(Long userId, ChatSession session) throws JsonProcessingException {
        String sessionData = objectMapper.writeValueAsString(session);
        redisService.saveChatSession(userId, session.getSessionId(), sessionData, deepSeekConfig.getSession().getExpireHours());
    }
    
    private ChatSession getSession(Long userId, String sessionId) {
        try {
            String sessionData = redisService.getChatSession(userId, sessionId);
            if (sessionData != null) {
                return objectMapper.readValue(sessionData, ChatSession.class);
            }
        } catch (Exception e) {
            log.error("获取会话失败，用户ID：{}，会话ID：{}，错误：{}", userId, sessionId, e.getMessage(), e);
        }
        return null;
    }
    
    private List<ChatMessage> getMessageHistory(Long userId, String sessionId) {
        try {
            String historyData = redisService.getChatMessageHistory(userId, sessionId);
            if (historyData != null) {
                return objectMapper.readValue(historyData, 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, ChatMessage.class));
            }
        } catch (Exception e) {
            log.error("获取消息历史失败，用户ID：{}，会话ID：{}，错误：{}", userId, sessionId, e.getMessage(), e);
        }
        return new ArrayList<>();
    }
    
    private void saveMessage(Long userId, String sessionId, String userMessage, String assistantMessage, List<ChatMessage> messageHistory) throws JsonProcessingException {
        // 添加用户消息
        ChatMessage userMsg = new ChatMessage();
        userMsg.setMessageId(generateMessageId());
        userMsg.setRole("user");
        userMsg.setContent(userMessage);
        userMsg.setTimestamp(LocalDateTime.now());
        messageHistory.add(userMsg);
        
        // 添加助手消息
        ChatMessage assistantMsg = new ChatMessage();
        assistantMsg.setMessageId(generateMessageId());
        assistantMsg.setRole("assistant");
        assistantMsg.setContent(assistantMessage);
        assistantMsg.setTimestamp(LocalDateTime.now());
        messageHistory.add(assistantMsg);
        
        // 限制消息数量
        int maxMessages = deepSeekConfig.getSession().getMaxMessagesPerSession();
        if (messageHistory.size() > maxMessages) {
            messageHistory = messageHistory.subList(messageHistory.size() - maxMessages, messageHistory.size());
        }
        
        // 保存消息历史
        String historyData = objectMapper.writeValueAsString(messageHistory);
        redisService.saveChatMessageHistory(userId, sessionId, historyData, deepSeekConfig.getSession().getExpireHours());
    }
    
    /**
     * 构建系统提示词
     * 从配置文件读取系统提示词，如果配置为空则使用默认值
     */
    private String buildSystemPrompt() {
        String configuredPrompt = deepSeekConfig.getModel().getSystemPrompt();

        // 如果配置为空或null，使用默认的专业系统提示词
        if (configuredPrompt == null || configuredPrompt.trim().isEmpty()) {
            return """
                你是金属微细线材综合检测平台的智能助手，专门为用户提供线材检测、质量分析和设备管理相关的服务。

                ## 你的能力
                1. **设备管理**：查询设备状态、设备列表等信息
                2. **线材检测数据**：查询金属微丝的检测数据、质量参数等
                3. **质量分析**：进行质量问题溯源分析、生产商排名、质量统计等
                4. **数据统计**：提供系统总体统计、年度统计、场景统计等数据分析

                ## 工作原则
                - 专业性：使用准确的技术术语，提供专业的分析建议
                - 实用性：优先提供实际可行的解决方案
                - 数据驱动：基于实际检测数据进行分析和建议
                - 用户友好：将复杂的技术信息转化为易懂的表述

                ## 响应风格
                - 简洁明了，重点突出
                - 提供具体的数据和分析结果
                - 在适当时候主动建议相关的查询或分析
                - 对于质量问题，提供可能的原因和改进建议

                ## 注意事项
                - 当用户询问具体数据时，优先使用工具函数获取实时数据
                - 对于质量问题，要客观分析，避免主观臆断
                - 保护用户隐私和商业机密
                - 如果遇到超出能力范围的问题，诚实说明并建议联系相关技术人员

                请用中文回答用户的问题，并在需要时主动调用相关工具获取数据。
                """;
        }

        return configuredPrompt;
    }
    
    /**
     * 处理工具调用
     */
    private String handleToolCalls(JsonNode toolCalls, List<ChatMessage> messageHistory, String newMessage, Long userId) throws Exception {
        List<Map<String, Object>> toolMessages = new ArrayList<>();
        
        // 执行所有工具调用
        for (JsonNode toolCall : toolCalls) {
            String toolCallId = toolCall.get("id").asText();
            String functionName = toolCall.get("function").get("name").asText();
            String arguments = toolCall.get("function").get("arguments").asText();
            
            // 构建工具调用请求
            ChatToolCall.ToolCallRequest request = new ChatToolCall.ToolCallRequest();
            request.setId(toolCallId);
            request.setType("function");
            
            ChatToolCall.FunctionCall functionCall = new ChatToolCall.FunctionCall();
            functionCall.setName(functionName);
            functionCall.setArguments(arguments);
            request.setFunction(functionCall);
            
            // 执行工具调用
            ChatToolCall.ToolCallResult toolResult = chatToolService.executeToolCall(request, userId);
            
            // 添加工具调用结果到消息
            Map<String, Object> toolMessage = new HashMap<>();
            toolMessage.put("role", "tool");
            toolMessage.put("tool_call_id", toolCallId);
            toolMessage.put("content", toolResult.getResult());
            toolMessages.add(toolMessage);
            
            if (toolResult.isSuccess()) {
                log.info("工具调用成功：{}，结果：{}", functionName, toolResult.getResult());
            } else {
                log.error("工具调用失败：{}，错误：{}", functionName, toolResult.getError());
            }
        }
        
        // 如果有工具调用结果，需要再次调用API获取最终响应
        if (!toolMessages.isEmpty()) {
            return callDeepSeekApiWithToolResults(messageHistory, newMessage, toolCalls, toolMessages, userId);
        }
        
        return "工具调用完成，但没有返回结果";
    }
    
    /**
     * 带工具调用结果的API调用
     */
    private String callDeepSeekApiWithToolResults(List<ChatMessage> messageHistory, String newMessage, JsonNode toolCalls, List<Map<String, Object>> toolMessages, Long userId) throws Exception {
        // 构建消息列表
        List<Map<String, Object>> messages = new ArrayList<>();
        
        // 添加系统消息
        messages.add(Map.of("role", "system", "content", buildSystemPrompt()));
        
        // 添加历史消息
        for (ChatMessage msg : messageHistory) {
            messages.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
        }
        
        // 添加用户消息
        messages.add(Map.of("role", "user", "content", newMessage));
        
        // 添加助手消息（包含工具调用）
        Map<String, Object> assistantMessage = new HashMap<>();
        assistantMessage.put("role", "assistant");
        assistantMessage.put("content", null);
        assistantMessage.put("tool_calls", objectMapper.convertValue(toolCalls, List.class));
        messages.add(assistantMessage);
        
        // 添加工具调用结果
        messages.addAll(toolMessages);
        
        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", deepSeekConfig.getModel().getDefaultModel());
        requestBody.put("messages", messages);
        requestBody.put("temperature", deepSeekConfig.getModel().getTemperature());
        requestBody.put("max_tokens", deepSeekConfig.getModel().getMaxTokens());
        
        // 构建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + deepSeekConfig.getApi().getApiKey());
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        
        // 发送请求
        String url = deepSeekConfig.getApi().getBaseUrl() + "/v1/chat/completions";
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
        
        // 解析响应
        JsonNode responseJson = objectMapper.readTree(response.getBody());
        JsonNode choices = responseJson.get("choices");
        
        if (choices != null && choices.isArray() && choices.size() > 0) {
            JsonNode firstChoice = choices.get(0);
            JsonNode message = firstChoice.get("message");
            if (message != null) {
                // 检查是否有新的工具调用（标准格式）
                JsonNode newToolCalls = message.get("tool_calls");
                if (newToolCalls != null && newToolCalls.isArray() && newToolCalls.size() > 0) {
                    log.info("检测到新的标准格式工具调用，数量：{}", newToolCalls.size());
                    return handleToolCalls(newToolCalls, messageHistory, newMessage, userId);
                }

                // 检查普通文本响应
                JsonNode content = message.get("content");
                if (content != null) {
                    String contentText = content.asText();

                    // 检查是否包含DeepSeek特殊格式的工具调用标记
                    if (containsToolCallMarkers(contentText)) {
                        log.info("在工具调用结果响应中检测到DeepSeek特殊格式的工具调用，开始递归处理");
                        return handleDeepSeekToolCalls(contentText, messageHistory, newMessage, userId, 1);
                    }

                    return contentText;
                }
            }
        }
        
        throw new RuntimeException("DeepSeek API响应格式不正确");
    }

    /**
     * 检查文本是否包含DeepSeek特殊格式的工具调用标记
     */
    private boolean containsToolCallMarkers(String content) {
        if (content == null) {
            return false;
        }

        // 检测各种可能的DeepSeek工具调用格式
        return content.contains("<|tool_calls_begin|>") ||
               content.contains("<｜tool▁calls▁begin｜>") ||
               content.contains("<｜tool▁call▁begin｜>") ||  // 新增：单数形式
               content.contains("tool_calls_begin") ||
               content.contains("tool_call_begin") ||        // 新增：单数形式
               content.contains("function_calls") ||
               content.contains("<｜tool▁sep｜>") ||         // 新增：分隔符格式
               content.contains("tool▁sep") ||               // 新增：分隔符格式
               // 检测JSON数组格式的工具调用
               (content.contains("[") && content.contains("\"name\"") && content.contains("\"arguments\""));
    }

    /**
     * 处理DeepSeek特殊格式的工具调用
     */
    private String handleDeepSeekToolCalls(String content, List<ChatMessage> messageHistory, String newMessage, Long userId) throws Exception {
        return handleDeepSeekToolCalls(content, messageHistory, newMessage, userId, 0);
    }

    /**
     * 处理DeepSeek特殊格式的工具调用（带递归深度控制）
     */
    private String handleDeepSeekToolCalls(String content, List<ChatMessage> messageHistory, String newMessage, Long userId, int recursionDepth) throws Exception {
        // 防止无限递归
        if (recursionDepth > 5) {
            log.warn("工具调用递归深度超过限制，停止处理");
            return "工具调用处理达到最大递归深度，请稍后重试";
        }
        try {
            // 解析DeepSeek特殊格式的工具调用
            List<ChatToolCall.ToolCallRequest> toolCallRequests = parseDeepSeekToolCalls(content);

            if (toolCallRequests.isEmpty()) {
                log.warn("未能解析出有效的工具调用，返回原始内容");
                return content;
            }

            List<Map<String, Object>> toolMessages = new ArrayList<>();

            // 执行所有工具调用
            for (ChatToolCall.ToolCallRequest request : toolCallRequests) {
                log.info("执行工具调用：{}", request.getFunction().getName());

                // 执行工具调用
                ChatToolCall.ToolCallResult toolResult = chatToolService.executeToolCall(request, userId);

                // 添加工具调用结果到消息
                Map<String, Object> toolMessage = new HashMap<>();
                toolMessage.put("role", "tool");
                toolMessage.put("tool_call_id", request.getId());
                toolMessage.put("content", toolResult.getResult());
                toolMessages.add(toolMessage);

                if (toolResult.isSuccess()) {
                    log.info("工具调用成功：{}，结果长度：{}", request.getFunction().getName(), toolResult.getResult().length());
                } else {
                    log.error("工具调用失败：{}，错误：{}", request.getFunction().getName(), toolResult.getError());
                }
            }

            // 构建模拟的tool_calls JsonNode用于后续处理
            List<Map<String, Object>> toolCallsData = new ArrayList<>();
            for (ChatToolCall.ToolCallRequest request : toolCallRequests) {
                Map<String, Object> toolCallData = new HashMap<>();
                toolCallData.put("id", request.getId());
                toolCallData.put("type", "function");
                Map<String, Object> functionData = new HashMap<>();
                functionData.put("name", request.getFunction().getName());
                functionData.put("arguments", request.getFunction().getArguments());
                toolCallData.put("function", functionData);
                toolCallsData.add(toolCallData);
            }

            JsonNode toolCallsNode = objectMapper.valueToTree(toolCallsData);

            // 调用原有的工具结果处理逻辑
            return callDeepSeekApiWithToolResults(messageHistory, newMessage, toolCallsNode, toolMessages, userId);

        } catch (Exception e) {
            log.error("处理DeepSeek工具调用时发生错误", e);
            return "处理工具调用时发生错误：" + e.getMessage();
        }
    }

    /**
     * 解析DeepSeek特殊格式的工具调用
     */
    private List<ChatToolCall.ToolCallRequest> parseDeepSeekToolCalls(String content) {
        List<ChatToolCall.ToolCallRequest> requests = new ArrayList<>();

        try {
            // 尝试多种可能的DeepSeek工具调用格式
            String[] patterns = {
                "<\\|tool_calls_begin\\|>([\\s\\S]*?)<\\|tool_calls_end\\|>",
                "<｜tool▁calls▁begin｜>([\\s\\S]*?)<｜tool▁calls▁end｜>",
                "<｜tool▁call▁begin｜>([\\s\\S]*?)<｜tool▁call▁end｜>",  // 新增：单数形式
                "tool_calls_begin([\\s\\S]*?)tool_calls_end",
                "tool_call_begin([\\s\\S]*?)tool_call_end",              // 新增：单数形式
                "function_calls\\s*:\\s*\\[([\\s\\S]*?)\\]"
            };

            String toolCallsJson = null;

            for (String pattern : patterns) {
                java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
                java.util.regex.Matcher m = p.matcher(content);
                if (m.find()) {
                    String extractedContent = m.group(1).trim();
                    log.info("使用模式 {} 成功提取工具调用内容：{}", pattern, extractedContent);

                    // 验证提取的内容是否为有效JSON
                    if (isValidJson(extractedContent)) {
                        toolCallsJson = extractedContent;
                        log.info("提取的内容是有效JSON，直接使用");
                        break;
                    } else {
                        log.info("提取的内容不是有效JSON，将使用分隔符解析方法处理");
                        // 不设置toolCallsJson，让后续的parseToolCallsWithSeparator处理
                    }
                }
            }

            if (toolCallsJson == null) {
                // 尝试直接查找JSON数组格式
                java.util.regex.Pattern jsonPattern = java.util.regex.Pattern.compile("\\[\\s*\\{[^\\}]*\"name\"[^\\}]*\\}[^\\]]*\\]");
                java.util.regex.Matcher jsonMatcher = jsonPattern.matcher(content);
                if (jsonMatcher.find()) {
                    toolCallsJson = jsonMatcher.group();
                    log.info("使用JSON模式成功提取工具调用：{}", toolCallsJson);
                }
            }

            // 如果没有找到JSON格式，尝试解析分隔符格式
            if (toolCallsJson == null) {
                log.info("未找到有效JSON格式，尝试使用分隔符解析方法");
                toolCallsJson = parseToolCallsWithSeparator(content);
            }

            if (toolCallsJson != null) {
                log.debug("准备解析工具调用JSON：{}", toolCallsJson);

                try {
                    // 解析JSON格式的工具调用
                    JsonNode toolCallsArray = objectMapper.readTree(toolCallsJson);

                    if (toolCallsArray.isArray()) {
                        log.info("成功解析工具调用JSON数组，包含 {} 个工具调用", toolCallsArray.size());

                        for (int i = 0; i < toolCallsArray.size(); i++) {
                            JsonNode toolCallNode = toolCallsArray.get(i);
                            log.debug("处理第 {} 个工具调用：{}", i + 1, toolCallNode);

                            ChatToolCall.ToolCallRequest request = new ChatToolCall.ToolCallRequest();
                            request.setId("tool_call_" + System.currentTimeMillis() + "_" + i);
                            request.setType("function");

                            ChatToolCall.FunctionCall functionCall = new ChatToolCall.FunctionCall();

                            // 获取函数名
                            String functionName = null;
                            if (toolCallNode.has("name")) {
                                functionName = toolCallNode.get("name").asText();
                            } else if (toolCallNode.has("function") && toolCallNode.get("function").has("name")) {
                                functionName = toolCallNode.get("function").get("name").asText();
                            }

                            if (functionName == null) {
                                log.warn("无法获取工具调用的函数名，跳过：{}", toolCallNode);
                                continue;
                            }

                            functionCall.setName(functionName);

                            // 获取参数
                            String arguments = "{}";
                            if (toolCallNode.has("arguments")) {
                                JsonNode argsNode = toolCallNode.get("arguments");
                                arguments = argsNode.isTextual() ? argsNode.asText() : argsNode.toString();
                            } else if (toolCallNode.has("function") && toolCallNode.get("function").has("arguments")) {
                                JsonNode argsNode = toolCallNode.get("function").get("arguments");
                                arguments = argsNode.isTextual() ? argsNode.asText() : argsNode.toString();
                            } else if (toolCallNode.has("parameters")) {
                                JsonNode argsNode = toolCallNode.get("parameters");
                                arguments = argsNode.toString();
                            }

                            // 验证参数是否为有效JSON
                            if (!isValidJson(arguments)) {
                                log.warn("工具调用 {} 的参数不是有效的JSON格式，使用默认空对象：{}", functionName, arguments);
                                arguments = "{}";
                            }

                            functionCall.setArguments(arguments);
                            request.setFunction(functionCall);

                            requests.add(request);
                            log.info("成功解析工具调用：{} with args: {}", functionName, arguments);
                        }
                    } else {
                        log.warn("工具调用JSON不是数组格式：{}", toolCallsArray);
                    }
                } catch (JsonProcessingException e) {
                    log.error("解析工具调用JSON时发生错误，JSON内容：{}", toolCallsJson, e);
                    log.error("JSON解析错误详情：{}", e.getMessage());

                    // 尝试修复常见的JSON格式问题
                    String fixedJson = tryFixJsonFormat(toolCallsJson);
                    if (fixedJson != null && !fixedJson.equals(toolCallsJson)) {
                        log.info("尝试修复JSON格式，修复后的内容：{}", fixedJson);
                        try {
                            JsonNode fixedArray = objectMapper.readTree(fixedJson);
                            if (fixedArray.isArray()) {
                                log.info("JSON修复成功，重新解析");
                                // 递归调用自己处理修复后的JSON
                                return parseDeepSeekToolCalls(fixedJson);
                            }
                        } catch (Exception fixException) {
                            log.error("修复后的JSON仍然无法解析：{}", fixException.getMessage());
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.error("解析DeepSeek工具调用时发生错误", e);
        }

        return requests;
    }

    /**
     * 解析使用分隔符格式的工具调用
     * 格式：<｜tool▁call▁begin｜>function<｜tool▁sep｜>function_name\n```json\n{...}\n```<｜tool▁call▁end｜>
     */
    private String parseToolCallsWithSeparator(String content) {
        try {
            List<Map<String, Object>> toolCalls = new ArrayList<>();

            // 查找分隔符格式的工具调用
            String[] separatorPatterns = {
                "<｜tool▁call▁begin｜>([\\s\\S]*?)<｜tool▁call▁end｜>",
                "<\\|tool_call_begin\\|>([\\s\\S]*?)<\\|tool_call_end\\|>"
            };

            for (String pattern : separatorPatterns) {
                java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
                java.util.regex.Matcher m = p.matcher(content);

                while (m.find()) {
                    String toolCallContent = m.group(1).trim();
                    log.info("找到分隔符格式的工具调用内容：{}", toolCallContent);

                    // 解析分隔符格式：function<｜tool▁sep｜>function_name\n```json\n{...}\n```
                    String[] parts = toolCallContent.split("<｜tool▁sep｜>|<\\|tool_sep\\|>");

                    if (parts.length >= 2) {
                        // 解析函数名和参数
                        String functionNamePart = parts[1].trim();
                        String arguments = parts.length > 2 ? parts[2].trim() : "{}";

                        // 从函数名部分提取纯函数名（可能包含换行和markdown代码块）
                        String functionName = extractFunctionName(functionNamePart);

                        // 如果函数名部分包含参数，提取参数
                        if (functionNamePart.contains("```")) {
                            arguments = extractJsonFromMarkdown(functionNamePart);
                        } else {
                            // 提取JSON内容，处理markdown代码块格式
                            arguments = extractJsonFromMarkdown(arguments);
                        }

                        // 验证JSON格式
                        if (!isValidJson(arguments)) {
                            log.warn("工具调用 {} 的参数不是有效的JSON格式，使用默认空对象：{}", functionName, arguments);
                            arguments = "{}";
                        }

                        Map<String, Object> toolCall = new HashMap<>();
                        toolCall.put("name", functionName);
                        toolCall.put("arguments", arguments);
                        toolCalls.add(toolCall);

                        log.info("成功解析分隔符格式工具调用：{} with args: {}", functionName, arguments);
                    }
                }
            }

            if (!toolCalls.isEmpty()) {
                String result = objectMapper.writeValueAsString(toolCalls);
                log.info("成功构建工具调用JSON数组，包含 {} 个工具调用", toolCalls.size());
                return result;
            }

        } catch (Exception e) {
            log.error("解析分隔符格式工具调用时发生错误", e);
        }

        return null;
    }

    /**
     * 从markdown代码块中提取JSON内容
     */
    private String extractJsonFromMarkdown(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "{}";
        }

        // 移除markdown代码块标记
        text = text.trim();

        // 处理```json\n{...}\n```格式
        if (text.contains("```json")) {
            java.util.regex.Pattern jsonPattern = java.util.regex.Pattern.compile("```json\\s*([\\s\\S]*?)\\s*```");
            java.util.regex.Matcher jsonMatcher = jsonPattern.matcher(text);
            if (jsonMatcher.find()) {
                String jsonContent = jsonMatcher.group(1).trim();
                log.debug("从markdown代码块中提取JSON：{}", jsonContent);
                return jsonContent;
            }
        }

        // 处理```\n{...}\n```格式
        if (text.contains("```")) {
            java.util.regex.Pattern codePattern = java.util.regex.Pattern.compile("```\\s*([\\s\\S]*?)\\s*```");
            java.util.regex.Matcher codeMatcher = codePattern.matcher(text);
            if (codeMatcher.find()) {
                String codeContent = codeMatcher.group(1).trim();
                // 检查是否是JSON格式
                if (codeContent.startsWith("{") || codeContent.startsWith("[")) {
                    log.debug("从代码块中提取JSON：{}", codeContent);
                    return codeContent;
                }
            }
        }

        // 如果没有代码块标记，直接返回原文本
        if (text.startsWith("{") || text.startsWith("[")) {
            return text;
        }

        // 默认返回空对象
        return "{}";
    }

    /**
     * 验证字符串是否为有效的JSON格式
     */
    private boolean isValidJson(String jsonString) {
        try {
            objectMapper.readTree(jsonString);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从包含函数名和可能的其他内容的字符串中提取纯函数名
     */
    private String extractFunctionName(String functionNamePart) {
        if (functionNamePart == null || functionNamePart.trim().isEmpty()) {
            return "";
        }

        String cleaned = functionNamePart.trim();

        // 如果包含换行符，取第一行
        if (cleaned.contains("\n")) {
            cleaned = cleaned.split("\n")[0].trim();
        }

        // 如果包含markdown代码块标记，取代码块之前的部分
        if (cleaned.contains("```")) {
            cleaned = cleaned.split("```")[0].trim();
        }

        // 移除可能的空格和特殊字符
        cleaned = cleaned.replaceAll("[\\s\\r\\n]+", "").trim();

        log.debug("从 '{}' 提取函数名：'{}'", functionNamePart, cleaned);
        return cleaned;
    }

    /**
     * 尝试修复常见的JSON格式问题
     */
    private String tryFixJsonFormat(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return null;
        }

        String fixed = jsonString.trim();

        try {
            // 1. 移除可能的HTML标签或特殊字符
            if (fixed.contains("<") && fixed.contains(">")) {
                // 移除HTML标签，但保留JSON内容
                fixed = fixed.replaceAll("<[^>]*>", "");
                log.debug("移除HTML标签后：{}", fixed);
            }

            // 2. 处理可能的转义问题
            if (fixed.contains("\\\"")) {
                fixed = fixed.replace("\\\"", "\"");
                log.debug("处理转义字符后：{}", fixed);
            }

            // 3. 确保是数组格式
            if (!fixed.startsWith("[") && !fixed.startsWith("{")) {
                // 查找第一个 { 或 [
                int firstBrace = fixed.indexOf("{");
                int firstBracket = fixed.indexOf("[");

                if (firstBrace >= 0 && (firstBracket < 0 || firstBrace < firstBracket)) {
                    fixed = fixed.substring(firstBrace);
                } else if (firstBracket >= 0) {
                    fixed = fixed.substring(firstBracket);
                }
                log.debug("提取JSON部分后：{}", fixed);
            }

            // 4. 确保结尾正确
            if (fixed.startsWith("[") && !fixed.endsWith("]")) {
                int lastBracket = fixed.lastIndexOf("]");
                if (lastBracket > 0) {
                    fixed = fixed.substring(0, lastBracket + 1);
                } else {
                    fixed = fixed + "]";
                }
                log.debug("修复数组结尾后：{}", fixed);
            } else if (fixed.startsWith("{") && !fixed.endsWith("}")) {
                int lastBrace = fixed.lastIndexOf("}");
                if (lastBrace > 0) {
                    fixed = fixed.substring(0, lastBrace + 1);
                } else {
                    fixed = fixed + "}";
                }
                log.debug("修复对象结尾后：{}", fixed);
            }

            // 5. 验证修复后的JSON
            if (isValidJson(fixed)) {
                return fixed;
            }

        } catch (Exception e) {
            log.debug("JSON修复过程中发生错误：{}", e.getMessage());
        }

        return null;
    }

    private void updateSessionInfo(Long userId, String sessionId, ChatSession session, String lastMessage) throws JsonProcessingException {
        session.setUpdateTime(LocalDateTime.now());
        session.setMessageCount(session.getMessageCount() + 2); // 用户消息 + 助手消息
        session.setLastMessage(lastMessage.length() > 100 ? lastMessage.substring(0, 100) + "..." : lastMessage);
        
        // 保存会话
        saveSession(userId, session);
    }
    
    private String generateSessionId() {
        return "session_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    private String generateMessageId() {
        return "msg_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    // ==================== 内部类 ====================
    
    private static class ChatSession {
        private String sessionId;
        private String title;
        private Long userId;
        private TokenService.UserType userType;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;
        private Integer messageCount;
        private String lastMessage;
        
        // Getters and Setters
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        @JsonProperty
        public TokenService.UserType getUserType() { return userType; }
        public void setUserType(TokenService.UserType userType) { this.userType = userType; }
        
        public LocalDateTime getCreateTime() { return createTime; }
        public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
        
        public LocalDateTime getUpdateTime() { return updateTime; }
        public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
        
        public Integer getMessageCount() { return messageCount; }
        public void setMessageCount(Integer messageCount) { this.messageCount = messageCount; }
        
        public String getLastMessage() { return lastMessage; }
        public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    }
    
    private static class ChatMessage {
        private String messageId;
        private String role;
        private String content;
        private LocalDateTime timestamp;
        
        // Getters and Setters
        public String getMessageId() { return messageId; }
        public void setMessageId(String messageId) { this.messageId = messageId; }
        
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }
} 