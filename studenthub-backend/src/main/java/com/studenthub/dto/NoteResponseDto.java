package com.studenthub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteResponseDto {
    private Long id;
    private Long courseId;
    private String courseCode;
    private String courseName;
    private String title;
    private String fileName;
    private String fileType;
    private String filePath;
    private String uploadedByFullName;
    private LocalDateTime uploadedAt;
}
