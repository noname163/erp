package com.dat.erp.dto.response;

import lombok.Data;

@Data
public class CompanyResponse {
    private Long id;
    private String name;
    private String industry;
    private String address;
    private String phoneNumber;
}
