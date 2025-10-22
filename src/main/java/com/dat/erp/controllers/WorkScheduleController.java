package com.dat.erp.controllers;

import java.time.LocalDate;
import java.util.List;

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
import com.dat.erp.dto.request.CreateWorkScheduleWithEmployeesRequest;
import com.dat.erp.dto.request.WorkScheduleRequest;
import com.dat.erp.dto.response.CustomApiResponse;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.WorkScheduleResponse;
import com.dat.erp.services.WorkScheduleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controller for managing work schedules.
 */
@Tag(name = "Work Schedule Management", description = "APIs for creating and retrieving work schedules")
@RestController
@RequestMapping("/api/work-schedules")
public class WorkScheduleController {

    @Autowired
    private WorkScheduleService workScheduleService;

    /**
     * Create a new work schedule.
     *
     * @param request request body with schedule details
     * @return created schedule code
     */
    @Operation(summary = "Create work schedule", description = "Creates a new work schedule.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Work schedule created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping("")
    public ResponseEntity<CustomApiResponse<String>> createWorkSchedule(@RequestBody WorkScheduleRequest request) {
        return ResponseBuilder.created(workScheduleService.createWorkScheduleService(request));
    }

    /**
     * Create multiple work schedules in bulk.
     *
     * @param requests list of work schedule requests
     * @return operation status
     */
    @Operation(summary = "Create work schedules in bulk", description = "Creates multiple work schedules in a single request.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Work schedules created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping("/bulk")
    public ResponseEntity<CustomApiResponse<String>> createWorkSchedules(@RequestBody List<WorkScheduleRequest> requests) {
        return ResponseBuilder.created(workScheduleService.createWorkSchedulesService(requests));
    }

    /**
     * Create a work schedule and assign employees to it.
     *
     * @param request payload including schedule info and employee codes
     * @return operation status
     */
    @Operation(summary = "Create schedule and assign employees", description = "Creates a work schedule and assigns a list of employees to it.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Work schedule and assignments created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping("/with-employees")
    public ResponseEntity<CustomApiResponse<String>> createWithEmployees(
            @RequestBody CreateWorkScheduleWithEmployeesRequest request) {
        return ResponseBuilder.created(
                workScheduleService.createWorkScheduleWithEmployees(request.getWorkSchedule(), request.getEmployeeCodes()));
    }

    /**
     * Get a paginated list of work schedules.
     *
     * @param shiftDate optional shift date filter
     * @param page page number
     * @param size page size
     * @param sortBy sort field
     * @param sortDir sort direction (ASC or DESC)
     * @return paged list of work schedules
     */
    @Operation(summary = "List work schedules", description = "Returns a paginated list of work schedules.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = WorkScheduleResponse.class)))
    })
    @GetMapping("")
    public ResponseEntity<PagedResponse<WorkScheduleResponse>> listWorkSchedules(
            @Parameter(description = "Shift date filter") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate shiftDate,
            @Parameter(description = "Page number") @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size") @RequestParam(required = false) Integer size,
            @Parameter(description = "Field to sort by") @RequestParam(required = false) String sortBy,
            @Parameter(description = "Sort direction (ASC or DESC)", example = "DESC") @RequestParam(required = false, defaultValue = Defaults.SORT_DIR_DESC) String sortDir) {
        return ResponseEntity.ok(workScheduleService.getWorkSchedule(shiftDate, size, page, sortBy, sortDir));
    }
}

