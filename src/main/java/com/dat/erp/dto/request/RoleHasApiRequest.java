package com.dat.erp.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RoleHasApiRequest {
    @NotBlank(message = "Role is required")
    @Size(max = 50, message = "Role must be at most 50 characters")
    private String roleCode;
    private List<String> endpoints;
}
