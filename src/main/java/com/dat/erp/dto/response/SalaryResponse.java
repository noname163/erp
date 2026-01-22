package com.dat.erp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalaryResponse {
    private String code;
    private String name;
    private String calculateMethod;
    private Boolean isDeduct;
}

