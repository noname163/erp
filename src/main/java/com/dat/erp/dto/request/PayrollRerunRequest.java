package com.dat.erp.dto.request;

import java.util.List;

import com.dat.erp.constants.PayrollRerunMode;

import lombok.Data;

@Data
public class PayrollRerunRequest {
    private String reason;
    private List<String> employeeCodes;
    private PayrollRerunMode mode;
    private Boolean dryRun;
}
