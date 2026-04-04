package com.dat.erp.dto.response;

import java.time.LocalDateTime;

import com.dat.erp.constants.PayrollRunStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayrollRunResponse {
    private String code;
    private String period;
    private PayrollRunStatus status;
    private LocalDateTime runAt;
    private LocalDateTime closeAt;
    private String runBy;
    private String updatedBy;
}
