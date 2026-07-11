package com.studenthub.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MarkRequestDto {

    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Course ID is required")
    private Long courseId;

    @NotBlank(message = "Exam type is required (MID_TERM, FINAL, QUIZ, ASSIGNMENT)")
    private String examType;

    @NotNull(message = "Marks obtained is required")
    @DecimalMin(value = "0.0", message = "Marks obtained must be at least 0")
    @DecimalMax(value = "100.0", message = "Marks obtained cannot exceed 100")
    private BigDecimal marksObtained;

    @NotNull(message = "Max marks is required")
    @DecimalMin(value = "1.0", message = "Max marks must be at least 1")
    @DecimalMax(value = "100.0", message = "Max marks cannot exceed 100")
    private BigDecimal maxMarks;

    @NotNull(message = "Grading date is required")
    private LocalDate gradingDate;

    private String remarks;
}
