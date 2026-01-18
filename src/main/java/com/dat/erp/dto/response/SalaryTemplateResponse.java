package com.dat.erp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalaryTemplateResponse {
    private String code;
    private String name;
    private String description;
    private String totalAmount;
    private String effectiveFrom;
    private String effectiveTo;
    private String currency;
}

