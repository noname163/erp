package com.dat.erp.dto.response;

import lombok.Data;

@Data
public class CompanyResponse {
    private Long id;
    private String name;
    private String code;
    private String industry;
    private String taxNumber;
    private String address;
    private String phoneNumber;
}
