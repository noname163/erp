package com.dat.erp.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dat.erp.dto.request.payroll.PayrollAdjustmentRequest;
import com.dat.erp.dto.response.payroll.PayrollAdjustmentResponse;
import com.dat.erp.services.PayrollAdjustmentService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Payroll Adjustment", description = "APIs for payroll bonus, deduction, retro, and settlement adjustments")
@RestController
@RequestMapping("/api/payroll-adjustments")
public class PayrollAdjustmentController {

    private final PayrollAdjustmentService payrollAdjustmentService;

    public PayrollAdjustmentController(PayrollAdjustmentService payrollAdjustmentService) {
        this.payrollAdjustmentService = payrollAdjustmentService;
    }

    @PostMapping("")
    @PreAuthorize("hasRoles({'ADMIN', 'HUMAN_RESOURCES'})")
    public ResponseEntity<PayrollAdjustmentResponse> create(@Valid @RequestBody PayrollAdjustmentRequest request) {
        return ResponseEntity.status(201).body(payrollAdjustmentService.create(request));
    }

    @PutMapping("/{code}")
    @PreAuthorize("hasRoles({'ADMIN', 'HUMAN_RESOURCES'})")
    public ResponseEntity<PayrollAdjustmentResponse> update(@PathVariable String code,
            @Valid @RequestBody PayrollAdjustmentRequest request) {
        return ResponseEntity.ok(payrollAdjustmentService.update(code, request));
    }

    @GetMapping("/{code}")
    @PreAuthorize("hasRoles({'ADMIN', 'HUMAN_RESOURCES'})")
    public ResponseEntity<PayrollAdjustmentResponse> get(@PathVariable String code) {
        return ResponseEntity.ok(payrollAdjustmentService.get(code));
    }

    @GetMapping("")
    @PreAuthorize("hasRoles({'ADMIN', 'HUMAN_RESOURCES'})")
    public ResponseEntity<List<PayrollAdjustmentResponse>> list() {
        return ResponseEntity.ok(payrollAdjustmentService.list());
    }

    @DeleteMapping("/{code}")
    @PreAuthorize("hasRoles({'ADMIN', 'HUMAN_RESOURCES'})")
    public ResponseEntity<Void> delete(@PathVariable String code) {
        payrollAdjustmentService.delete(code);
        return ResponseEntity.noContent().build();
    }
}
