package com.mmw.metal_micro_wire_backend.dto.iot;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * IoT设备消息DTO
 * 用于解析华为云IoT平台推送的设备消息
 */
@Data
public class IoTDeviceMessage {
    
    /**
     * 资源类型，如 "device.property"
     */
    private String resource;
    
    /**
     * 事件类型，如 "report"
     */
    private String event;
    
    /**
     * 事件时间
     */
    @JsonProperty("event_time")
    private String eventTime;
    
    /**
     * 通知数据
     */
    @JsonProperty("notify_data")
    private NotifyData notifyData;
    
    /**
     * 通知数据内部类
     */
    @Data
    public static class NotifyData {
        /**
         * 消息头
         */
        private Header header;
        
        /**
         * 消息体
         */
        private Body body;
    }
    
    /**
     * 消息头内部类
     */
    @Data
    public static class Header {
        /**
         * 应用ID
         */
        @JsonProperty("app_id")
        private String appId;
        
        /**
         * 设备ID
         */
        @JsonProperty("device_id")
        private String deviceId;
        
        /**
         * 节点ID
         */
        @JsonProperty("node_id")
        private String nodeId;
        
        /**
         * 产品ID
         */
        @JsonProperty("product_id")
        private String productId;
        
        /**
         * 网关ID
         */
        @JsonProperty("gateway_id")
        private String gatewayId;
        
        /**
         * 标签列表
         */
        private List<Tag> tags;
    }
    
    /**
     * 标签内部类
     */
    @Data
    public static class Tag {
        /**
         * 标签键
         */
        @JsonProperty("tag_key")
        private String tagKey;
        
        /**
         * 标签值
         */
        @JsonProperty("tag_value")
        private String tagValue;
    }
    
    /**
     * 消息体内部类
     */
    @Data
    public static class Body {
        /**
         * 服务列表
         */
        private List<Service> services;
    }
    
    /**
     * 服务内部类
     */
    @Data
    public static class Service {
        /**
         * 服务ID
         */
        @JsonProperty("service_id")
        private String serviceId;
        
        /**
         * 属性数据
         */
        private Map<String, Object> properties;
        
        /**
         * 事件时间
         */
        @JsonProperty("event_time")
        private String eventTime;
    }
} 