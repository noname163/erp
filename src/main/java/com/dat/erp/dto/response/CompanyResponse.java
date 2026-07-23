package com.dat.erp.dto.response;

import lombok.Data;

@Data
public class CompanyResponse {
    private String code;
    private String email;
    private String name;
    private String address;
    private String taxNumber;
    private String industry;
    private String phoneNumber;
}
