package com.dat.erp.dtos.requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    private String employeeEmail;
    private String password;
}
