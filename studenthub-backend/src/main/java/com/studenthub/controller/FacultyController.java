package com.studenthub.controller;

import com.studenthub.dto.*;
import com.studenthub.service.FacultyService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/v1/faculty")
public class FacultyController {

    private final FacultyService facultyService;

    public FacultyController(FacultyService facultyService) {
        this.facultyService = facultyService;
    }

    @GetMapping("/courses")
    public ResponseEntity<List<CourseResponseDto>> getAssignedCourses(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(facultyService.getAssignedCourses(userDetails.getUsername()));
    }

    @PostMapping("/attendance")
    public ResponseEntity<AttendanceResponseDto> saveAttendance(
            @Valid @RequestBody AttendanceRequestDto attendanceDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(facultyService.saveAttendance(attendanceDto, userDetails.getUsername()));
    }

    @GetMapping("/attendance")
    public ResponseEntity<List<AttendanceResponseDto>> getAttendance(
            @RequestParam Long courseId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(facultyService.getAttendanceByCourseAndDate(courseId, date));
    }

    @PostMapping("/marks")
    public ResponseEntity<MarkResponseDto> saveMark(
            @Valid @RequestBody MarkRequestDto markDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(facultyService.saveMark(markDto, userDetails.getUsername()));
    }

    @GetMapping("/marks")
    public ResponseEntity<Page<MarkResponseDto>> getMarks(
            @RequestParam Long courseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        return ResponseEntity.ok(facultyService.getMarksByCourse(courseId, pageable));
    }

    @PostMapping(value = "/notes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<NoteResponseDto> uploadNote(
            @RequestParam("courseId") Long courseId,
            @RequestParam("title") String title,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        return new ResponseEntity<>(facultyService.uploadNote(courseId, title, file, userDetails.getUsername()), HttpStatus.CREATED);
    }

    @DeleteMapping("/notes/{id}")
    public ResponseEntity<Map<String, String>> deleteNote(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        facultyService.deleteNote(id, userDetails.getUsername());
        return ResponseEntity.ok(Map.of("message", "Note deleted successfully"));
    }

    @GetMapping("/courses/{courseId}/notes")
    public ResponseEntity<List<NoteResponseDto>> getNotes(@PathVariable Long courseId) {
        return ResponseEntity.ok(facultyService.getNotesByCourse(courseId));
    }
}
