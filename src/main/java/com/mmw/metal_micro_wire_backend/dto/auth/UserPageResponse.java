package com.mmw.metal_micro_wire_backend.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

/**
 * 用户分页响应DTO - 兼容前端解析格式
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPageResponse {
    
    /**
     * 用户列表 (对应前端的content字段)
     */
    private List<UserInfoResponse> content;
    
    /**
     * 分页信息
     */
    private PageableInfo pageable;
    
    /**
     * 是否是最后一页
     */
    private boolean last;
    
    /**
     * 总记录数
     */
    private long totalElements;
    
    /**
     * 总页数
     */
    private int totalPages;
    
    /**
     * 每页大小
     */
    private int size;
    
    /**
     * 当前页码
     */
    private int number;
    
    /**
     * 排序信息
     */
    private SortInfo sort;
    
    /**
     * 是否是第一页
     */
    private boolean first;
    
    /**
     * 当前页实际元素数量
     */
    private int numberOfElements;
    
    /**
     * 是否为空
     */
    private boolean empty;
    
    /**
     * 分页信息DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageableInfo {
        private int pageNumber;
        private int pageSize;
        private SortInfo sort;
        private long offset;
        private boolean paged;
        private boolean unpaged;
    }
    
    /**
     * 排序信息DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SortInfo {
        private boolean empty;
        private boolean sorted;
        private boolean unsorted;
    }
    
    /**
     * 从分页对象创建响应
     */
    public static UserPageResponse fromPage(Page<UserInfoResponse> page) {
        Pageable pageable = page.getPageable();
        Sort sort = page.getSort();
        
        return UserPageResponse.builder()
                .content(page.getContent())
                .pageable(PageableInfo.builder()
                        .pageNumber(pageable.getPageNumber())
                        .pageSize(pageable.getPageSize())
                        .sort(SortInfo.builder()
                                .empty(sort.isEmpty())
                                .sorted(sort.isSorted())
                                .unsorted(sort.isUnsorted())
                                .build())
                        .offset(pageable.getOffset())
                        .paged(pageable.isPaged())
                        .unpaged(pageable.isUnpaged())
                        .build())
                .last(page.isLast())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .size(page.getSize())
                .number(page.getNumber())
                .sort(SortInfo.builder()
                        .empty(sort.isEmpty())
                        .sorted(sort.isSorted())
                        .unsorted(sort.isUnsorted())
                        .build())
                .first(page.isFirst())
                .numberOfElements(page.getNumberOfElements())
                .empty(page.isEmpty())
                .build();
    }
} 