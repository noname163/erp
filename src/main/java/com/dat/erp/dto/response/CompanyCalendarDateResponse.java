package com.dat.erp.dto.response;

import java.time.LocalDate;

import com.dat.erp.constants.DayType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyCalendarDateResponse {
    private LocalDate calDate;
    private DayType dayType;
    private String note;
}
