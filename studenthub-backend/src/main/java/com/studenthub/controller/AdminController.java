package com.studenthub.controller;

import com.studenthub.dto.*;
import com.studenthub.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // ==================================================
    // DEPARTMENTS
    // ==================================================
    @PostMapping("/departments")
    public ResponseEntity<DepartmentResponseDto> createDepartment(@Valid @RequestBody DepartmentRequestDto deptDto) {
        return new ResponseEntity<>(adminService.createDepartment(deptDto), HttpStatus.CREATED);
    }

    @PutMapping("/departments/{id}")
    public ResponseEntity<DepartmentResponseDto> updateDepartment(@PathVariable Long id, @Valid @RequestBody DepartmentRequestDto deptDto) {
        return ResponseEntity.ok(adminService.updateDepartment(id, deptDto));
    }

    @GetMapping("/departments/{id}")
    public ResponseEntity<DepartmentResponseDto> getDepartment(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getDepartmentById(id));
    }

    @GetMapping("/departments")
    public ResponseEntity<Page<DepartmentResponseDto>> getAllDepartments(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(adminService.getAllDepartments(search, pageable));
    }

    @DeleteMapping("/departments/{id}")
    public ResponseEntity<Map<String, String>> deleteDepartment(@PathVariable Long id) {
        adminService.deleteDepartment(id);
        return ResponseEntity.ok(Map.of("message", "Department deleted successfully"));
    }

    // ==================================================
    // COURSES
    // ==================================================
    @PostMapping("/courses")
    public ResponseEntity<CourseResponseDto> createCourse(@Valid @RequestBody CourseRequestDto courseDto) {
        return new ResponseEntity<>(adminService.createCourse(courseDto), HttpStatus.CREATED);
    }

    @PutMapping("/courses/{id}")
    public ResponseEntity<CourseResponseDto> updateCourse(@PathVariable Long id, @Valid @RequestBody CourseRequestDto courseDto) {
        return ResponseEntity.ok(adminService.updateCourse(id, courseDto));
    }

    @GetMapping("/courses/{id}")
    public ResponseEntity<CourseResponseDto> getCourse(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getCourseById(id));
    }

    @GetMapping("/courses")
    public ResponseEntity<Page<CourseResponseDto>> getAllCourses(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(adminService.getAllCourses(search, pageable));
    }

    @DeleteMapping("/courses/{id}")
    public ResponseEntity<Map<String, String>> deleteCourse(@PathVariable Long id) {
        adminService.deleteCourse(id);
        return ResponseEntity.ok(Map.of("message", "Course deleted successfully"));
    }

    @PostMapping("/courses/{courseId}/assign-faculty/{facultyId}")
    public ResponseEntity<CourseResponseDto> assignFaculty(@PathVariable Long courseId, @PathVariable Long facultyId) {
        return ResponseEntity.ok(adminService.assignFacultyToCourse(courseId, facultyId));
    }

    @DeleteMapping("/courses/{courseId}/remove-faculty/{facultyId}")
    public ResponseEntity<CourseResponseDto> removeFaculty(@PathVariable Long courseId, @PathVariable Long facultyId) {
        return ResponseEntity.ok(adminService.removeFacultyFromCourse(courseId, facultyId));
    }

    // ==================================================
    // STUDENTS
    // ==================================================
    @PostMapping("/students")
    public ResponseEntity<StudentResponseDto> createStudent(@Valid @RequestBody StudentRequestDto studentDto) {
        return new ResponseEntity<>(adminService.createStudent(studentDto), HttpStatus.CREATED);
    }

    @PutMapping("/students/{id}")
    public ResponseEntity<StudentResponseDto> updateStudent(@PathVariable Long id, @Valid @RequestBody StudentRequestDto studentDto) {
        return ResponseEntity.ok(adminService.updateStudent(id, studentDto));
    }

    @GetMapping("/students/{id}")
    public ResponseEntity<StudentResponseDto> getStudent(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getStudentById(id));
    }

    @GetMapping("/students")
    public ResponseEntity<Page<StudentResponseDto>> getAllStudents(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "rollNumber") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(adminService.getAllStudents(search, pageable));
    }

    @DeleteMapping("/students/{id}")
    public ResponseEntity<Map<String, String>> deleteStudent(@PathVariable Long id) {
        adminService.deleteStudent(id);
        return ResponseEntity.ok(Map.of("message", "Student deleted successfully"));
    }

    // ==================================================
    // FACULTY
    // ==================================================
    @PostMapping("/faculty")
    public ResponseEntity<FacultyResponseDto> createFaculty(@Valid @RequestBody FacultyRequestDto facultyDto) {
        return new ResponseEntity<>(adminService.createFaculty(facultyDto), HttpStatus.CREATED);
    }

    @PutMapping("/faculty/{id}")
    public ResponseEntity<FacultyResponseDto> updateFaculty(@PathVariable Long id, @Valid @RequestBody FacultyRequestDto facultyDto) {
        return ResponseEntity.ok(adminService.updateFaculty(id, facultyDto));
    }

    @GetMapping("/faculty/{id}")
    public ResponseEntity<FacultyResponseDto> getFaculty(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getFacultyById(id));
    }

    @GetMapping("/faculty")
    public ResponseEntity<Page<FacultyResponseDto>> getAllFaculty(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "employeeId") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(adminService.getAllFaculty(search, pageable));
    }

    @DeleteMapping("/faculty/{id}")
    public ResponseEntity<Map<String, String>> deleteFaculty(@PathVariable Long id) {
        adminService.deleteFaculty(id);
        return ResponseEntity.ok(Map.of("message", "Faculty deleted successfully"));
    }

    // ==================================================
    // REPORTS / STATS
    // ==================================================
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(adminService.getAdminDashboardStats());
    }
}
