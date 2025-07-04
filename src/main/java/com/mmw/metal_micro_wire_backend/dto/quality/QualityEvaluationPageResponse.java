package com.mmw.metal_micro_wire_backend.dto.quality;

import com.mmw.metal_micro_wire_backend.dto.wirematerial.WireMaterialResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 质量评估分页响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualityEvaluationPageResponse {
    
    /**
     * 线材列表
     */
    private List<WireMaterialResponse> wireMaterials;
    
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
    public static QualityEvaluationPageResponse fromPage(Page<WireMaterialResponse> page) {
        return QualityEvaluationPageResponse.builder()
                .wireMaterials(page.getContent())
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
} 