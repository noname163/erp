package com.dat.erp.dto.request;

import lombok.Data;

@Data
public class CompanyRequest {
    private String name;
    private String industry;
    private String address;
    private String phoneNumber;
}