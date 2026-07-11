package com.studenthub.controller;

import com.studenthub.dto.*;
import com.studenthub.service.StudentService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/student")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/profile")
    public ResponseEntity<StudentResponseDto> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(studentService.getProfileByEmail(userDetails.getUsername()));
    }

    @PutMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StudentResponseDto> updateProfile(
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "dob", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dob,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "profilePic", required = false) MultipartFile profilePic,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(studentService.updateProfile(userDetails.getUsername(), phone, dob, password, profilePic));
    }

    @GetMapping("/attendance")
    public ResponseEntity<List<AttendanceResponseDto>> getMyAttendance(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(studentService.getMyAttendance(userDetails.getUsername()));
    }

    @GetMapping("/marks")
    public ResponseEntity<List<MarkResponseDto>> getMyMarks(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(studentService.getMyMarks(userDetails.getUsername()));
    }

    @GetMapping("/courses")
    public ResponseEntity<List<CourseResponseDto>> getMyCourses(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(studentService.getMyCourses(userDetails.getUsername()));
    }

    @GetMapping("/notes")
    public ResponseEntity<List<NoteResponseDto>> getMyNotes(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(studentService.getNotesForMyCourses(userDetails.getUsername()));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(studentService.getStudentDashboardStats(userDetails.getUsername()));
    }
}
