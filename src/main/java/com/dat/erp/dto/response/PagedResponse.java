package com.dat.erp.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {

    private List<T> data;

    private int page; // current page (0-based)
    private int size; // size per page
    private long totalElements;
    private int totalPages;
    private boolean last; // is last page

    private String message; // optional
    private boolean success; // optional

    /**
     * Factory method to build response from Page object
     */
    public static <T> PagedResponse<T> fromPage(Page<T> page, String message) {
        return PagedResponse.<T>builder()
                .data(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .message(message)
                .success(true)
                .build();
    }

    public static <T> PagedResponse<T> fromPage(Page<?> page, List<T> mappedData, String message) {
        return PagedResponse.<T>builder()
                .data(mappedData)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .message(message)
                .success(true)
                .build();
    }

}
