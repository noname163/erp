package com.dat.erp.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EmailRequest {
    @NotBlank(message = "From email is required")
    @Email(message = "Invalid from email format")
    @Size(max = 255, message = "From email must be at most 255 characters")
    private String from;

    @NotBlank(message = "To email is required")
    @Email(message = "Invalid to email format")
    @Size(max = 255, message = "To email must be at most 255 characters")
    private String to;

    @NotBlank(message = "Full name is required")
    @Size(max = 255, message = "Full name must be at most 255 characters")
    private String fullName;

    @Size(max = 50, message = "Gender must be at most 50 characters")
    private String gender;

    @NotBlank(message = "Html template is required")
    @Size(max = 255, message = "Html template must be at most 255 characters")
    private String htmlFilePath;
}

