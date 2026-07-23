package com.dat.erp.dto.request;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SalaryTemplateRequest {
    @NotBlank(message = "name is required")
    @Size(max = 255, message = "name must be at most 255 characters")
    private String name;

    private String description;

    @NotBlank(message = "totalAmount is required")
    @Pattern(regexp = "^[0-9]+(\\.[0-9]+)?$", message = "totalAmount must be a positive numeric value")
    private String totalAmount;

    @NotNull(message = "effectiveFrom is required")
    private LocalDate effectiveFrom;

    @NotNull(message = "effectiveTo is required")
    private LocalDate effectiveTo;

    private String currency;

    @NotNull(message = "details is required")
    @Size(min = 1, message = "details must have at least 1 item")
    private List<@Valid SalaryTemplateDetailRequest> details;
}

