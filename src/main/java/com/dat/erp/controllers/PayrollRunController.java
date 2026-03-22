package com.dat.erp.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dat.erp.dto.request.payroll.PayrollRunPreviewRequest;
import com.dat.erp.dto.response.payroll.EmployeePayslipResponse;
import com.dat.erp.dto.response.payroll.PayrollRunDetailResponse;
import com.dat.erp.dto.response.payroll.PayrollRunResponse;
import com.dat.erp.services.PayrollRunService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Payroll Run", description = "APIs for payroll preview, finalize, replay, and payslip detail")
@RestController
@RequestMapping("/api/payroll-runs")
public class PayrollRunController {

    private final PayrollRunService payrollRunService;

    public PayrollRunController(PayrollRunService payrollRunService) {
        this.payrollRunService = payrollRunService;
    }

    @PostMapping("/previews")
    @PreAuthorize("hasRoles({'ADMIN', 'HUMAN_RESOURCES'})")
    public ResponseEntity<PayrollRunResponse> createPreview(@Valid @RequestBody PayrollRunPreviewRequest request) {
        return ResponseEntity.status(201).body(payrollRunService.createPreview(request));
    }

    @PostMapping("/{runCode}/finalize")
    @PreAuthorize("hasRoles({'ADMIN', 'HUMAN_RESOURCES'})")
    public ResponseEntity<PayrollRunResponse> finalizeRun(@PathVariable String runCode) {
        return ResponseEntity.ok(payrollRunService.finalizeRun(runCode));
    }

    @PostMapping("/{runCode}/replay-snapshot")
    @PreAuthorize("hasRoles({'ADMIN', 'HUMAN_RESOURCES'})")
    public ResponseEntity<PayrollRunResponse> replaySnapshot(@PathVariable String runCode) {
        return ResponseEntity.ok(payrollRunService.replaySnapshot(runCode));
    }

    @GetMapping("/{runCode}")
    @PreAuthorize("hasRoles({'ADMIN', 'HUMAN_RESOURCES'})")
    public ResponseEntity<PayrollRunDetailResponse> getRun(@PathVariable String runCode) {
        return ResponseEntity.ok(payrollRunService.getRun(runCode));
    }

    @GetMapping("/{runCode}/employees/{userProfileCode}/payslip")
    @PreAuthorize("hasRoles({'ADMIN', 'HUMAN_RESOURCES'})")
    public ResponseEntity<EmployeePayslipResponse> getEmployeePayslip(@PathVariable String runCode,
            @PathVariable String userProfileCode) {
        return ResponseEntity.ok(payrollRunService.getPayslip(runCode, userProfileCode));
    }
}
