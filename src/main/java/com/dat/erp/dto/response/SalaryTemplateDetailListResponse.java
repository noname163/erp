package com.dat.erp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalaryTemplateDetailListResponse {
    private String salaryCode;
    private String dependenceCode;
    private String amount;
    private Integer quantity;
    private String unitName;
    private String salaryName;
    private String calculateMethod;
    private Boolean isDeduct;
    private Boolean isFixed;
}
