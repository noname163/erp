package com.dat.erp.dto.response.department;

import lombok.Data;

@Data
public class DepartmentResponse {
    private Long id;
    private String name;
    private String code;
    private String description;
    private String status;
    private String companyCode;
    private String companyName;
}
