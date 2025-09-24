package com.dat.erp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RoleHasApiRequest {
    @NotBlank(message = "Role is required")
    @Size(max = 50, message = "Role must be at most 50 characters")
    private String roleCode;
    private Boolean create;
    private Boolean read;
    private Boolean update;
    private Boolean delete;
    private String endpoint;
}
