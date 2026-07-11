package com.studenthub.service;

import com.studenthub.dto.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface StudentService {
    StudentResponseDto getProfileByEmail(String studentEmail);
    StudentResponseDto updateProfile(String studentEmail, String phone, LocalDate dob, String password, MultipartFile profilePic);
    
    List<AttendanceResponseDto> getMyAttendance(String studentEmail);
    List<MarkResponseDto> getMyMarks(String studentEmail);
    List<CourseResponseDto> getMyCourses(String studentEmail);
    
    // Notes Access
    List<NoteResponseDto> getNotesForMyCourses(String studentEmail);
    
    // Analytics
    Map<String, Object> getStudentDashboardStats(String studentEmail);
}
