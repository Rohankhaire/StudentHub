package com.studenthub.service;

import com.studenthub.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public interface FacultyService {
    List<CourseResponseDto> getAssignedCourses(String facultyEmail);
    
    // Attendance
    AttendanceResponseDto saveAttendance(AttendanceRequestDto attendanceDto, String facultyEmail);
    List<AttendanceResponseDto> getAttendanceByCourseAndDate(Long courseId, LocalDate date);
    
    // Marks
    MarkResponseDto saveMark(MarkRequestDto markDto, String facultyEmail);
    Page<MarkResponseDto> getMarksByCourse(Long courseId, Pageable pageable);
    
    // Notes Upload/Manage
    NoteResponseDto uploadNote(Long courseId, String title, MultipartFile file, String facultyEmail);
    void deleteNote(Long noteId, String facultyEmail);
    List<NoteResponseDto> getNotesByCourse(Long courseId);
}
