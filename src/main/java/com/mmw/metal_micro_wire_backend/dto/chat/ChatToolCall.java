package com.mmw.metal_micro_wire_backend.dto.chat;

import lombok.Data;

import java.util.Map;

/**
 * AI工具调用相关DTO
 */
public class ChatToolCall {
    
    /**
     * 工具定义
     */
    @Data
    public static class Tool {
        /**
         * 工具类型（目前只支持function）
         */
        private String type = "function";
        
        /**
         * 函数定义
         */
        private Function function;
    }
    
    /**
     * 函数定义
     */
    @Data
    public static class Function {
        /**
         * 函数名称
         */
        private String name;
        
        /**
         * 函数描述
         */
        private String description;
        
        /**
         * 函数参数定义（JSON Schema格式）
         */
        private Map<String, Object> parameters;
    }
    
    /**
     * 工具调用请求
     */
    @Data
    public static class ToolCallRequest {
        /**
         * 工具调用ID
         */
        private String id;
        
        /**
         * 工具类型
         */
        private String type;
        
        /**
         * 函数调用信息
         */
        private FunctionCall function;
    }
    
    /**
     * 函数调用信息
     */
    @Data
    public static class FunctionCall {
        /**
         * 函数名称
         */
        private String name;
        
        /**
         * 函数参数（JSON字符串）
         */
        private String arguments;
    }
    
    /**
     * 工具调用结果
     */
    @Data
    public static class ToolCallResult {
        /**
         * 工具调用ID
         */
        private String toolCallId;
        
        /**
         * 调用结果
         */
        private String result;
        
        /**
         * 是否成功
         */
        private boolean success;
        
        /**
         * 错误信息（如果失败）
         */
        private String error;
    }
} 