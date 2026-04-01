package com.dat.erp.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dat.erp.dto.request.CompanyCalendarRequest;
import com.dat.erp.dto.response.CompanyCalendarDateResponse;
import com.dat.erp.dto.response.CompanyCalendarListResponse;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.CompanyCalendarResponse;
import com.dat.erp.services.CompanyCalendarService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Company Calendar", description = "APIs for company calendar management")
@RestController
@RequestMapping("/api/company-calendars")
public class CompanyCalendarController {

    private final CompanyCalendarService companyCalendarService;

    public CompanyCalendarController(CompanyCalendarService companyCalendarService) {
        this.companyCalendarService = companyCalendarService;
    }

    @Operation(summary = "Create company calendar", description = "Creates a company calendar with required effective dates and unique calendar dates.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Company calendar created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping("")
    @PreAuthorize("hasRoles({'HUMAN_RESOURCES'})")
    public ResponseEntity<CompanyCalendarResponse> createCompanyCalendar(@Valid @RequestBody CompanyCalendarRequest request) {
        return ResponseEntity.status(201).body(companyCalendarService.createCompanyCalendar(request));
    }

    @Operation(summary = "Update company calendar", description = "Updates one company calendar and replaces its date list with the submitted dates.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Company calendar updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Company calendar not found")
    })
    @PutMapping("/{code}")
    @PreAuthorize("hasRoles({'HUMAN_RESOURCES'})")
    public ResponseEntity<CompanyCalendarResponse> updateCompanyCalendar(
            @Parameter(description = "Company calendar code") @PathVariable String code,
            @Valid @RequestBody CompanyCalendarRequest request) {
        return ResponseEntity.ok(companyCalendarService.updateCompanyCalendar(code, request));
    }

    @Operation(summary = "Get company calendar list", description = "Returns a paginated company calendar list filtered by name, region, and time zone.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Company calendar list retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("")
    @PreAuthorize("hasRoles({'HUMAN_RESOURCES'})")
    public ResponseEntity<PagedResponse<CompanyCalendarListResponse>> getCompanyCalendars(
            @Parameter(description = "Calendar name (contains)") @RequestParam(required = false) String name,
            @Parameter(description = "Region (contains)") @RequestParam(required = false) String region,
            @Parameter(description = "Time zone (exact IANA id)") @RequestParam(required = false) String timeZone,
            @Parameter(description = "Page number") @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size") @RequestParam(required = false) Integer size,
            @Parameter(description = "Field to sort by") @RequestParam(required = false) String sortBy,
            @Parameter(description = "Sort direction (ASC or DESC)", example = "DESC") @RequestParam(defaultValue = "DESC") String sortDir) {
        return ResponseEntity.ok(companyCalendarService.getCompanyCalendars(name, region, timeZone, page, size, sortBy, sortDir));
    }

    @Operation(summary = "Get company calendar dates by code", description = "Returns all company calendar dates for one calendar code.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Company calendar dates retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Company calendar not found")
    })
    @GetMapping("/{code}/dates")
    @PreAuthorize("hasRoles({'HUMAN_RESOURCES'})")
    public ResponseEntity<List<CompanyCalendarDateResponse>> getCompanyCalendarDates(
            @Parameter(description = "Company calendar code") @PathVariable String code) {
        return ResponseEntity.ok(companyCalendarService.getCompanyCalendarDates(code));
    }
}
