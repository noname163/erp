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

import com.dat.erp.dto.request.payroll.PayrollPolicyRequest;
import com.dat.erp.dto.response.payroll.PayrollPolicyResponse;
import com.dat.erp.services.PayrollPolicyService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Payroll Policy", description = "APIs for payroll policy and rate rule management")
@RestController
@RequestMapping("/api/payroll-policies")
public class PayrollPolicyController {

    private final PayrollPolicyService payrollPolicyService;

    public PayrollPolicyController(PayrollPolicyService payrollPolicyService) {
        this.payrollPolicyService = payrollPolicyService;
    }

    @PostMapping("")
    @PreAuthorize("hasRoles({'ADMIN', 'HUMAN_RESOURCES'})")
    public ResponseEntity<PayrollPolicyResponse> create(@Valid @RequestBody PayrollPolicyRequest request) {
        return ResponseEntity.status(201).body(payrollPolicyService.create(request));
    }

    @PutMapping("/{code}")
    @PreAuthorize("hasRoles({'ADMIN', 'HUMAN_RESOURCES'})")
    public ResponseEntity<PayrollPolicyResponse> update(@PathVariable String code,
            @Valid @RequestBody PayrollPolicyRequest request) {
        return ResponseEntity.ok(payrollPolicyService.update(code, request));
    }

    @GetMapping("/{code}")
    @PreAuthorize("hasRoles({'ADMIN', 'HUMAN_RESOURCES'})")
    public ResponseEntity<PayrollPolicyResponse> get(@PathVariable String code) {
        return ResponseEntity.ok(payrollPolicyService.get(code));
    }

    @GetMapping("")
    @PreAuthorize("hasRoles({'ADMIN', 'HUMAN_RESOURCES'})")
    public ResponseEntity<List<PayrollPolicyResponse>> list() {
        return ResponseEntity.ok(payrollPolicyService.list());
    }

    @DeleteMapping("/{code}")
    @PreAuthorize("hasRoles({'ADMIN', 'HUMAN_RESOURCES'})")
    public ResponseEntity<Void> delete(@PathVariable String code) {
        payrollPolicyService.delete(code);
        return ResponseEntity.noContent().build();
    }
}
