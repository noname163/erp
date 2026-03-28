package com.dat.erp.dto.response;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyCalendarListResponse {
    private String code;
    private String name;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private String region;
    private String timeZone;
    private String note;
    private String createdBy;
}
