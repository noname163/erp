package com.dat.erp.dto.request;

import java.util.List;

import com.dat.erp.constants.PayrollRerunMode;
import com.dat.erp.constants.Messages;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class PayrollRerunRequest {

    @NotBlank(message = Messages.ERROR_PAYROLL_RERUN_REASON_REQUIRED)
    private String reason;

    private List<String> employeeCodes;

    private PayrollRerunMode mode;

    private Boolean dryRun;
}
