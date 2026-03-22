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

import com.dat.erp.dto.request.payroll.WorkScheduleRequest;
import com.dat.erp.dto.response.payroll.WorkScheduleResponse;
import com.dat.erp.services.WorkScheduleService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Work Schedule", description = "APIs for payroll work schedule management")
@RestController
@RequestMapping("/api/work-schedules")
public class WorkScheduleController {

    private final WorkScheduleService workScheduleService;

    public WorkScheduleController(WorkScheduleService workScheduleService) {
        this.workScheduleService = workScheduleService;
    }

    @PostMapping("")
    @PreAuthorize("hasRoles({'ADMIN', 'HUMAN_RESOURCES'})")
    public ResponseEntity<WorkScheduleResponse> create(@Valid @RequestBody WorkScheduleRequest request) {
        return ResponseEntity.status(201).body(workScheduleService.create(request));
    }

    @PutMapping("/{code}")
    @PreAuthorize("hasRoles({'ADMIN', 'HUMAN_RESOURCES'})")
    public ResponseEntity<WorkScheduleResponse> update(@PathVariable String code,
            @Valid @RequestBody WorkScheduleRequest request) {
        return ResponseEntity.ok(workScheduleService.update(code, request));
    }

    @GetMapping("/{code}")
    @PreAuthorize("hasRoles({'ADMIN', 'HUMAN_RESOURCES'})")
    public ResponseEntity<WorkScheduleResponse> get(@PathVariable String code) {
        return ResponseEntity.ok(workScheduleService.get(code));
    }

    @GetMapping("")
    @PreAuthorize("hasRoles({'ADMIN', 'HUMAN_RESOURCES'})")
    public ResponseEntity<List<WorkScheduleResponse>> list() {
        return ResponseEntity.ok(workScheduleService.list());
    }

    @DeleteMapping("/{code}")
    @PreAuthorize("hasRoles({'ADMIN', 'HUMAN_RESOURCES'})")
    public ResponseEntity<Void> delete(@PathVariable String code) {
        workScheduleService.delete(code);
        return ResponseEntity.noContent().build();
    }
}
