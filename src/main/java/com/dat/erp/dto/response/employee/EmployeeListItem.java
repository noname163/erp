package com.dat.erp.dto.response.employee;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeListItem {
    private Long id;
    private String profileCode;
    private String code;
    private String name;
    private String firstName;
    private String lastName;
    private String email;
    private Integer age;
    private String department;
    private String departmentCode;
    private String phone;
    private String roleCode;
    private List<String> skills;
    private String status;
    private LocalDateTime createdAt;
    private String createdBy;
}

