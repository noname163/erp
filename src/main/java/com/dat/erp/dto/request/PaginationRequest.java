package com.dat.erp.dto.request;

import com.dat.erp.dto.request.enums.SortType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PaginationRequest {
    private Integer pageNo;
    private Integer pageSize;
    private SortType sortType;
    private String orderBy;
}
