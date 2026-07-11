package com.studenthub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacultyResponseDto {
    private Long id;
    private String employeeId;
    private String designation;
    private LocalDate joiningDate;
    private UserResponseDto user;
    private DepartmentResponseDto department;
    private Set<String> courseCodes;
}
