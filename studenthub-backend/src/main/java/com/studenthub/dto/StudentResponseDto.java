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
public class StudentResponseDto {
    private Long id;
    private String rollNumber;
    private LocalDate dateOfBirth;
    private LocalDate enrollmentDate;
    private String profilePicturePath;
    private UserResponseDto user;
    private DepartmentResponseDto department;
    private Set<String> courseCodes;
}
