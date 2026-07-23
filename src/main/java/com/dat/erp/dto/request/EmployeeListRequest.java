package com.dat.erp.dto.request;

import java.util.List;

import com.dat.erp.dto.request.enums.EmployeeStatusFilter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeListRequest extends PaginationRequest{
    private String keyword;
    private Integer minAge;
    private Integer maxAge;
    private List<Long> departmentIds;
    private List<Long> skillIds;
    private EmployeeStatusFilter status;
}
