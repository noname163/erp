package com.dat.erp.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalaryListResponse {
    private String name;
    private String formula;
    private String calculateMethod;
    private String isDeduct;
    private String createdBy;
    private LocalDateTime updatedAt;
}
