package com.dat.erp.dto.response;

import java.util.List;

import com.dat.erp.dto.response.department.DepartmentResponse;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CompanyDetailResponse extends CompanyResponse {
    List<DepartmentResponse> department;
}
