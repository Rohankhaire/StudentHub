package com.studenthub.service;

import com.studenthub.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface AdminService {

    // Department CRUD
    DepartmentResponseDto createDepartment(DepartmentRequestDto deptDto);
    DepartmentResponseDto updateDepartment(Long id, DepartmentRequestDto deptDto);
    DepartmentResponseDto getDepartmentById(Long id);
    Page<DepartmentResponseDto> getAllDepartments(String search, Pageable pageable);
    void deleteDepartment(Long id);

    // Course CRUD
    CourseResponseDto createCourse(CourseRequestDto courseDto);
    CourseResponseDto updateCourse(Long id, CourseRequestDto courseDto);
    CourseResponseDto getCourseById(Long id);
    Page<CourseResponseDto> getAllCourses(String search, Pageable pageable);
    void deleteCourse(Long id);
    CourseResponseDto assignFacultyToCourse(Long courseId, Long facultyId);
    CourseResponseDto removeFacultyFromCourse(Long courseId, Long facultyId);

    // Student CRUD
    StudentResponseDto createStudent(StudentRequestDto studentDto);
    StudentResponseDto updateStudent(Long id, StudentRequestDto studentDto);
    StudentResponseDto getStudentById(Long id);
    Page<StudentResponseDto> getAllStudents(String search, Pageable pageable);
    void deleteStudent(Long id);

    // Faculty CRUD
    FacultyResponseDto createFaculty(FacultyRequestDto facultyDto);
    FacultyResponseDto updateFaculty(Long id, FacultyRequestDto facultyDto);
    FacultyResponseDto getFacultyById(Long id);
    Page<FacultyResponseDto> getAllFaculty(String search, Pageable pageable);
    void deleteFaculty(Long id);

    // Dashboard Statistics
    Map<String, Object> getAdminDashboardStats();
}
