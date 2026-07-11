package com.studenthub.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class FacultyRequestDto {

    // User account fields
    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    private String email;

    private String password; // Optional on update, required on create

    @NotBlank(message = "Full name is required")
    private String fullName;

    private String phone;

    // Faculty fields
    @NotBlank(message = "Employee ID is required")
    private String employeeId;

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    @NotBlank(message = "Designation is required")
    private String designation;

    @NotNull(message = "Joining date is required")
    private LocalDate joiningDate;
}
