package com.studenthub.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class StudentRequestDto {

    // User account fields
    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    private String email;

    private String password; // Optional on update, required on create

    @NotBlank(message = "Full name is required")
    private String fullName;

    private String phone;

    // Student fields
    @NotBlank(message = "Roll number is required")
    private String rollNumber;

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    private LocalDate dateOfBirth;

    @NotNull(message = "Enrollment date is required")
    private LocalDate enrollmentDate;
}
