package com.mmw.metal_micro_wire_backend.service.impl;

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
        JsonNode choices = responseJson.get("choices");
        
        if (choices != null && choices.isArray() && choices.size() > 0) {
            JsonNode firstChoice = choices.get(0);
            JsonNode message = firstChoice.get("message");
            if (message != null) {
                // 检查是否有工具调用
                JsonNode toolCalls = message.get("tool_calls");
                if (toolCalls != null && toolCalls.isArray() && toolCalls.size() > 0) {
                    return handleToolCalls(toolCalls, messageHistory, newMessage, userId);
                }
                
                // 普通文本响应
                JsonNode content = message.get("content");
                if (content != null) {
                    return content.asText();
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
            return callDeepSeekApiWithToolResults(messageHistory, newMessage, toolCalls, toolMessages);
        }
        
        return "工具调用完成，但没有返回结果";
    }
    
    /**
     * 带工具调用结果的API调用
     */
    private String callDeepSeekApiWithToolResults(List<ChatMessage> messageHistory, String newMessage, JsonNode toolCalls, List<Map<String, Object>> toolMessages) throws Exception {
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
                JsonNode content = message.get("content");
                if (content != null) {
                    return content.asText();
                }
            }
        }
        
        throw new RuntimeException("DeepSeek API响应格式不正确");
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