package com.dat.erp.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    private String employeeEmail;
    private String password;
}
