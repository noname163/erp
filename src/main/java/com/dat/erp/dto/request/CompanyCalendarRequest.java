package com.dat.erp.dto.request;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CompanyCalendarRequest {
    @NotBlank(message = "name is required")
    private String name;

    @NotNull(message = "effectiveFrom is required")
    private LocalDate effectiveFrom;

    @NotNull(message = "effectiveTo is required")
    private LocalDate effectiveTo;

    @NotNull(message = "dates is required")
    @Size(min = 1, message = "dates must have at least 1 item")
    private List<@Valid CompanyCalendarDateRequest> dates;
    
    @NotBlank(message = "region is required")
    private String region;

    @NotBlank(message = "timeZone is required")
    private String timeZone;

    @NotBlank(message = "note is required")
    private String note;
}
