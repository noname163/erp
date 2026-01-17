package com.dat.erp.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CompanyRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must be at most 255 characters")
    private String email;

    @NotBlank(message = "Company name is required")
    @Size(max = 100, message = "Company name must be at most 100 characters")
    private String name;

    @NotBlank(message = "Industry is required")
    @Size(max = 50, message = "Industry must be at most 50 characters")
    private String industry;

    @NotBlank(message = "Tax is required")
    @Size(max = 50, message = "Tax number must be at most 50 characters")
    private String taxNumber;

    @NotBlank(message = "Address is required")
    @Size(max = 255, message = "Address must be at most 255 characters")
    private String address;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[0-9\\- ]{7,20}$", message = "Invalid phone number format")
    private String phoneNumber;
}
