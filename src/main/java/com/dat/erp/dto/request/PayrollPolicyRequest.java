package com.dat.erp.dto.request;

import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PayrollPolicyRequest {
    @NotBlank(message = "name is required")
    @Size(max = 255, message = "name must be at most 255 characters")
    private String name;
    @jakarta.validation.Valid
    private com.dat.erp.data.PayrollStatutorySettings statutorySettings;

    @Positive(message = "standardQuantityPerDay must be a positive integer")
    private Integer standardQuantityPerDay;

    private String unitCode;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime standardStartTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime standardEndTime;

    @Size(max = 255, message = "roundingRule must be at most 255 characters")
    private String roundingRule;

    @NotNull(message = "effectiveFrom is required")
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;
}
