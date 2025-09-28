package com.dat.erp.utils;

import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.dat.erp.dto.response.PagedResponse;

public class PageableUtils {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    /**
     * Build a pageable with optional sorting.
     *
     * @param page      page number (0-based). If null or negative, defaults to 0.
     * @param size      page size. If null, zero, or too big, defaults to 20 (max
     *                  100).
     * @param sortBy    field to sort by. If null or empty, unsorted.
     * @param direction ASC or DESC. Defaults to ASC.
     * @return Pageable object
     */
    public static Pageable create(Integer page, Integer size, String sortBy, String direction) {
        int pageNumber = (page == null || page < 0) ? DEFAULT_PAGE : page;
        int pageSize = (size == null || size <= 0) ? DEFAULT_SIZE
                : Math.min(size, MAX_SIZE);

        if (sortBy != null && !sortBy.isBlank()) {
            Sort.Direction sortDirection = "DESC".equalsIgnoreCase(direction) ? Sort.Direction.DESC
                    : Sort.Direction.ASC;

            return PageRequest.of(pageNumber, pageSize, Sort.by(sortDirection, sortBy));
        }

        return PageRequest.of(pageNumber, pageSize);
    }

    public static <E, D> PagedResponse<D> mapPage(Page<E> page, Function<E, D> mapper, String message) {
        List<D> data = page.getContent().stream().map(mapper).toList();
        return PagedResponse.fromPage(page, data, message);
    }

}
