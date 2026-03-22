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

import com.dat.erp.dto.request.payroll.CompanyCalendarRequest;
import com.dat.erp.dto.response.payroll.CompanyCalendarResponse;
import com.dat.erp.services.CompanyCalendarService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Company Calendar", description = "APIs for payroll company calendar management")
@RestController
@RequestMapping("/api/company-calendars")
public class CompanyCalendarController {

    private final CompanyCalendarService companyCalendarService;

    public CompanyCalendarController(CompanyCalendarService companyCalendarService) {
        this.companyCalendarService = companyCalendarService;
    }

    @PostMapping("")
    @PreAuthorize("hasRoles({'ADMIN', 'HUMAN_RESOURCES'})")
    public ResponseEntity<CompanyCalendarResponse> create(@Valid @RequestBody CompanyCalendarRequest request) {
        return ResponseEntity.status(201).body(companyCalendarService.create(request));
    }

    @PutMapping("/{code}")
    @PreAuthorize("hasRoles({'ADMIN', 'HUMAN_RESOURCES'})")
    public ResponseEntity<CompanyCalendarResponse> update(@PathVariable String code,
            @Valid @RequestBody CompanyCalendarRequest request) {
        return ResponseEntity.ok(companyCalendarService.update(code, request));
    }

    @GetMapping("/{code}")
    @PreAuthorize("hasRoles({'ADMIN', 'HUMAN_RESOURCES'})")
    public ResponseEntity<CompanyCalendarResponse> get(@PathVariable String code) {
        return ResponseEntity.ok(companyCalendarService.get(code));
    }

    @GetMapping("")
    @PreAuthorize("hasRoles({'ADMIN', 'HUMAN_RESOURCES'})")
    public ResponseEntity<List<CompanyCalendarResponse>> list() {
        return ResponseEntity.ok(companyCalendarService.list());
    }

    @DeleteMapping("/{code}")
    @PreAuthorize("hasRoles({'ADMIN', 'HUMAN_RESOURCES'})")
    public ResponseEntity<Void> delete(@PathVariable String code) {
        companyCalendarService.delete(code);
        return ResponseEntity.noContent().build();
    }
}
