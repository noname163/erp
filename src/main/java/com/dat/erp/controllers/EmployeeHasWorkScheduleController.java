package com.dat.erp.controllers;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dat.erp.builders.ResponseBuilder;
import com.dat.erp.constants.Defaults;
import com.dat.erp.dto.request.EmployeeHasWorkScheduleRequest;
import com.dat.erp.dto.response.CustomApiResponse;
import com.dat.erp.dto.response.EmployeeHasWorkScheduleResponse;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.searchs.EmployeeScheduleFilter;
import com.dat.erp.services.EmployeeHasWorkScheduleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controller for managing employee schedule assignments.
 */
@Tag(name = "Employee Work Schedule", description = "APIs for assigning employees to schedules and querying assignments")
@RestController
@RequestMapping("/api/employee-has-work-schedules")
public class EmployeeHasWorkScheduleController {

    @Autowired
    private EmployeeHasWorkScheduleService employeeHasWorkScheduleService;

    /**
     * Create employee-schedule assignments.
     *
     * @param request request payload defining assignments
     * @return operation status
     */
    @Operation(summary = "Create employee schedule assignments", description = "Creates assignments between employees and work schedules.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Assignments created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping("")
    public ResponseEntity<CustomApiResponse<String>> createAssignments(
            @RequestBody EmployeeHasWorkScheduleRequest request) {
        return ResponseBuilder.created(employeeHasWorkScheduleService.createEmployeeHasWorkSchedule(request));
    }

    /**
     * Get a paginated list of employee schedule assignments.
     *
     * @param startDate start date filter
     * @param endDate end date filter
     * @param companyCode company code filter
     * @param shiftType shift type filter
     * @param page page number
     * @param size page size
     * @param sortBy sort field
     * @param sortDir sort direction
     * @return paged list of assignments
     */
    @Operation(summary = "List employee schedule assignments", description = "Returns a paginated list of employee schedule assignments.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EmployeeHasWorkScheduleResponse.class)))
    })
    @GetMapping("")
    public ResponseEntity<PagedResponse<EmployeeHasWorkScheduleResponse>> listAssignments(
            @Parameter(description = "Start date filter")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date filter")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Company code filter") @RequestParam(required = false) String companyCode,
            @Parameter(description = "Shift type filter") @RequestParam(required = false) String shiftType,
            @Parameter(description = "Page number") @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size") @RequestParam(required = false) Integer size,
            @Parameter(description = "Field to sort by") @RequestParam(required = false) String sortBy,
            @Parameter(description = "Sort direction (ASC or DESC)", example = "DESC") @RequestParam(required = false, defaultValue = Defaults.SORT_DIR_DESC) String sortDir) {

        EmployeeScheduleFilter filter = new EmployeeScheduleFilter(startDate, endDate, companyCode, shiftType);
        return ResponseEntity.ok(
                employeeHasWorkScheduleService.getEmployeeSchedule(filter, size, page, sortBy, sortDir));
    }
}

