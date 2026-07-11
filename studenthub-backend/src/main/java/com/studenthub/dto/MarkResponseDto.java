package com.studenthub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarkResponseDto {
    private Long id;
    private Long studentId;
    private String studentRoll;
    private String studentName;
    private Long courseId;
    private String courseName;
    private String courseCode;
    private String examType;
    private BigDecimal marksObtained;
    private BigDecimal maxMarks;
    private LocalDate gradingDate;
    private String remarks;
    private String gradedByFullName;
}
