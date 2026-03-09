package com.dat.erp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalaryTemplateListResponse {
    private String name;
    private String description;
    private String effectiveFrom;
    private String effectiveTo;
    private String currency;
    private String totalAmount;
    private String createdBy;
}
