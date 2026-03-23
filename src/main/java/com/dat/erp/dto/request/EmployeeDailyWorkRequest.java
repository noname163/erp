package com.dat.erp.dto.request;

import java.time.LocalDate;
import java.time.LocalTime;

import com.dat.erp.constants.DailyWorkUnit;
import com.dat.erp.constants.DateType;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class EmployeeDailyWorkRequest {
    @NotBlank(message = "userProfileCode is required")
    private String userProfileCode;

    @NotNull(message = "workingDate is required")
    private LocalDate workingDate;

    @NotNull(message = "startTime is required")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @NotNull(message = "endTime is required")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    @NotNull(message = "quantity is required")
    @Positive(message = "quantity must be a positive integer")
    private Integer quantity;

    @NotNull(message = "unit is required")
    private DailyWorkUnit unit;

    @NotNull(message = "workType is required")
    private DateType workType;

    private Boolean usedPto;

    @PositiveOrZero(message = "otTime must be >= 0")
    private Integer otTime;
}
