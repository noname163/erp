package com.dat.erp.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountRequest {
    @NotBlank(message = "Username is required")
    @Size(max = 255, message = "Username must be at most 255 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(max = 255, message = "Password must be at most 255 characters")
    private String password;

    @NotBlank(message = "Role type is required")
    @Size(max = 100, message = "Role type must be at most 100 characters")
    private String roleType;

    @JsonIgnore
    private String companyCode;

    @Size(max = 255, message = "Full name must be at most 255 characters")
    private String fullName;
}
