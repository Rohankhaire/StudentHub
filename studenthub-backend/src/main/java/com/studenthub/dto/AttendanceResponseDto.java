package com.studenthub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceResponseDto {
    private Long id;
    private Long studentId;
    private String studentRoll;
    private String studentName;
    private Long courseId;
    private String courseName;
    private String courseCode;
    private LocalDate date;
    private String status;
    private String remarks;
    private String createdByFullName;
}
