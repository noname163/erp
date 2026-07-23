package com.dat.erp.dto.response;

import lombok.Data;

@Data
public class EmployeeResponse {
    private String code;
    private String email;
    private String fullName;
    private String department;
    private String role;
}

