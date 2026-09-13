package com.dat.erp.dto.request;

import java.util.List;

import com.dat.erp.constants.PayrollRerunMode;

import io.micrometer.common.lang.NonNull;

import com.dat.erp.constants.Messages;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class PayrollRerunRequest {

    @NotBlank(message = Messages.ERROR_PAYROLL_RERUN_REASON_REQUIRED)
    private String reason;

    private List<String> employeeCodes;

    @NonNull
    private PayrollRerunMode mode = PayrollRerunMode.FULL_RUN ;

    private boolean dryRun;
}
