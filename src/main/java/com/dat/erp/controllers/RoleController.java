package com.dat.erp.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dat.erp.dto.response.SelectionOptionResponse;
import com.dat.erp.services.RoleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Role Management", description = "APIs for retrieving role data")
@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @Operation(summary = "Get role options", description = "Returns role options by filters.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role options retrieved successfully")
    })
    @GetMapping("/options")
    public ResponseEntity<List<SelectionOptionResponse>> getRoleOptionsByCompanyCode(
            @Parameter(description = "Role name (optional)") @RequestParam(required = false) String name) {
        return ResponseEntity.ok(roleService.getRoleOptionsByCompanyCode(name, true, null));
    }
}
