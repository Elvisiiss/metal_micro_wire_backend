package com.mmw.metal_micro_wire_backend.dto.scenario;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 应用场景分页响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationScenarioPageResponse {
    
    /**
     * 应用场景列表
     */
    private List<ApplicationScenarioResponse> scenarios;
    
    /**
     * 当前页码
     */
    private int currentPage;
    
    /**
     * 每页大小
     */
    private int pageSize;
    
    /**
     * 总页数
     */
    private int totalPages;
    
    /**
     * 总记录数
     */
    private long totalElements;
    
    /**
     * 是否是第一页
     */
    private boolean first;
    
    /**
     * 是否是最后一页
     */
    private boolean last;
    
    /**
     * 从分页对象创建响应
     */
    public static ApplicationScenarioPageResponse fromPage(Page<ApplicationScenarioResponse> page) {
        return ApplicationScenarioPageResponse.builder()
                .scenarios(page.getContent())
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
} 