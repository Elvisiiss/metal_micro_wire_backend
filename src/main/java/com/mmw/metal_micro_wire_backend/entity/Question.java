package com.mmw.metal_micro_wire_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 问题实体类 - 存储设备问题数据
 */
@Entity
@Table(name = "questions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Question {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 设备ID
     */
    @Column(name = "device_id", nullable = false)
    private String deviceId;
    
    /**
     * 用户问题内容
     */
    @Column(name = "question_content", columnDefinition = "TEXT")
    private String questionContent;
    
    /**
     * 响应状态 (0: 未处理, 1: 已处理)
     */
    @Column(name = "response_status", nullable = false)
    @Builder.Default
    private Integer responseStatus = 0;
    
    /**
     * AI响应内容
     */
    @Column(name = "ai_response_content", columnDefinition = "TEXT")
    private String aiResponseContent;
    
    /**
     * 事件发生时间
     */
    @Column(name = "event_time", nullable = false)
    private LocalDateTime eventTime;
    
    /**
     * 创建时间
     */
    @Column(name = "create_time", nullable = false)
    @Builder.Default
    private LocalDateTime createTime = LocalDateTime.now();
    
    /**
     * 响应时间
     */
    @Column(name = "response_time")
    private LocalDateTime responseTime;
    
    @PrePersist
    public void prePersist() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    public void preUpdate() {
        if (responseStatus == 1 && responseTime == null) {
            responseTime = LocalDateTime.now();
        }
    }
} 