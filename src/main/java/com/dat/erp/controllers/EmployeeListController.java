package com.dat.erp.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dat.erp.dto.request.EmployeeListRequest;
import com.dat.erp.dto.request.enums.EmployeeStatusFilter;
import com.dat.erp.dto.request.enums.SortType;
import com.dat.erp.dto.response.PaginationResponse;
import com.dat.erp.dto.response.employee.EmployeeListItem;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.services.EmployeeListService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Employee Management", description = "APIs for retrieving employees")
@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeListController {

    private final EmployeeListService employeeListService;

    public EmployeeListController(EmployeeListService employeeListService) {
        this.employeeListService = employeeListService;
    }

    @Operation(summary = "Get list employee", description = "Returns paginated employees with role-based data scope enforcement.")
    @GetMapping("")
    public ResponseEntity<PaginationResponse<EmployeeListItem>> getEmployees(
            @RequestParam(required = false, defaultValue = "0") Integer pageNo,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize,
            @RequestParam(required = false, defaultValue = "name") String orderBy,
            @RequestParam(required = false, defaultValue = "ASC") String sortType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) Integer maxAge,
            @RequestParam(required = false) String departmentIds,
            @RequestParam(required = false) String skillIds,
            @RequestParam(required = false) String status) {

        EmployeeListRequest request = EmployeeListRequest.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .orderBy(orderBy)
                .sortType(parseSortType(sortType))
                .keyword(keyword)
                .minAge(minAge)
                .maxAge(maxAge)
                .departmentIds(parseCsvLongs(departmentIds, "EMP_400_005"))
                .skillIds(parseCsvLongs(skillIds, "EMP_400_006"))
                .status(parseStatus(status))
                .build();

        return ResponseEntity.ok(employeeListService.getEmployees(request));
    }

    private static List<Long> parseCsvLongs(String raw, String errorCode) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return List.of(raw.split(",")).stream()
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .map(Long::parseLong)
                    .toList();
        } catch (NumberFormatException ex) {
            throw new BadRequestException(errorCode + ": Invalid numeric id list");
        }
    }

    private static SortType parseSortType(String raw) {
        if (raw == null || raw.isBlank()) {
            return SortType.ASC;
        }
        try {
            return SortType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("EMP_400_007: sortType must be ASC or DESC");
        }
    }

    private static EmployeeStatusFilter parseStatus(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return EmployeeStatusFilter.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("EMP_400_004: status must be ACTIVE or INACTIVE");
        }
    }
}
