package com.codingshuttle.youtube.hospitalManagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FindOrCreatePatientRequestDto {
    @NotBlank(message = "Name is required")
    private String name;

    @Email(message = "A valid email is required")
    @NotBlank(message = "Email is required")
    private String email;
}