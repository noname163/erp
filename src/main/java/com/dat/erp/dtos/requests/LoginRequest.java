package com.dat.erp.dtos.requests;

import lombok.Getter;

@Getter
public class LoginRequest {
    private String employeeEmail;
    private String password;
}
