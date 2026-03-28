package com.dat.erp.dto.request;

import java.time.LocalDate;

import com.dat.erp.constants.DayType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CompanyCalendarDateRequest {
    @NotNull(message = "calDate is required")
    private LocalDate calDate;

    @NotNull(message = "dayType is required")
    private DayType dayType;

    @NotBlank(message = "note is required")
    private String note;
}
