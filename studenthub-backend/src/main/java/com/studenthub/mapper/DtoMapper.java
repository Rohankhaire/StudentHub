package com.studenthub.mapper;

import com.studenthub.dto.*;
import com.studenthub.entity.*;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class DtoMapper {

    public static UserResponseDto toUserResponseDto(User user) {
        if (user == null) return null;
        
        Set<String> roles = user.getRoles() != null ? 
            user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()) : 
            Collections.emptySet();

        return UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .status(user.getStatus().name())
                .roles(roles)
                .build();
    }

    public static DepartmentResponseDto toDepartmentResponseDto(Department department, Long studentCount, Long facultyCount) {
        if (department == null) return null;

        return DepartmentResponseDto.builder()
                .id(department.getId())
                .name(department.getName())
                .code(department.getCode())
                .description(department.getDescription())
                .studentCount(studentCount != null ? studentCount : 0L)
                .facultyCount(facultyCount != null ? facultyCount : 0L)
                .build();
    }

    public static DepartmentResponseDto toDepartmentResponseDto(Department department) {
        return toDepartmentResponseDto(department, 0L, 0L);
    }

    public static StudentResponseDto toStudentResponseDto(Student student) {
        if (student == null) return null;

        Set<String> courseCodes = student.getCourses() != null ?
            student.getCourses().stream().map(Course::getCode).collect(Collectors.toSet()) :
            Collections.emptySet();

        return StudentResponseDto.builder()
                .id(student.getId())
                .rollNumber(student.getRollNumber())
                .dateOfBirth(student.getDateOfBirth())
                .enrollmentDate(student.getEnrollmentDate())
                .profilePicturePath(student.getProfilePicturePath())
                .user(toUserResponseDto(student.getUser()))
                .department(toDepartmentResponseDto(student.getDepartment()))
                .courseCodes(courseCodes)
                .build();
    }

    public static FacultyResponseDto toFacultyResponseDto(Faculty faculty) {
        if (faculty == null) return null;

        Set<String> courseCodes = faculty.getCourses() != null ?
            faculty.getCourses().stream().map(Course::getCode).collect(Collectors.toSet()) :
            Collections.emptySet();

        return FacultyResponseDto.builder()
                .id(faculty.getId())
                .employeeId(faculty.getEmployeeId())
                .designation(faculty.getDesignation())
                .joiningDate(faculty.getJoiningDate())
                .user(toUserResponseDto(faculty.getUser()))
                .department(toDepartmentResponseDto(faculty.getDepartment()))
                .courseCodes(courseCodes)
                .build();
    }

    public static CourseResponseDto toCourseResponseDto(Course course) {
        if (course == null) return null;

        Set<String> facultyNames = course.getFaculties() != null ?
            course.getFaculties().stream().map(f -> f.getUser().getFullName()).collect(Collectors.toSet()) :
            Collections.emptySet();

        return CourseResponseDto.builder()
                .id(course.getId())
                .name(course.getName())
                .code(course.getCode())
                .description(course.getDescription())
                .credits(course.getCredits())
                .department(toDepartmentResponseDto(course.getDepartment()))
                .facultyNames(facultyNames)
                .build();
    }

    public static AttendanceResponseDto toAttendanceResponseDto(Attendance attendance) {
        if (attendance == null) return null;

        return AttendanceResponseDto.builder()
                .id(attendance.getId())
                .studentId(attendance.getStudent().getId())
                .studentRoll(attendance.getStudent().getRollNumber())
                .studentName(attendance.getStudent().getUser().getFullName())
                .courseId(attendance.getCourse().getId())
                .courseName(attendance.getCourse().getName())
                .courseCode(attendance.getCourse().getCode())
                .date(attendance.getDate())
                .status(attendance.getStatus().name())
                .remarks(attendance.getRemarks())
                .createdByFullName(attendance.getCreatedBy() != null ? attendance.getCreatedBy().getFullName() : "System")
                .build();
    }

    public static MarkResponseDto toMarkResponseDto(Mark mark) {
        if (mark == null) return null;

        return MarkResponseDto.builder()
                .id(mark.getId())
                .studentId(mark.getStudent().getId())
                .studentRoll(mark.getStudent().getRollNumber())
                .studentName(mark.getStudent().getUser().getFullName())
                .courseId(mark.getCourse().getId())
                .courseName(mark.getCourse().getName())
                .courseCode(mark.getCourse().getCode())
                .examType(mark.getExamType().name())
                .marksObtained(mark.getMarksObtained())
                .maxMarks(mark.getMaxMarks())
                .gradingDate(mark.getGradingDate())
                .remarks(mark.getRemarks())
                .gradedByFullName(mark.getGradedBy() != null ? mark.getGradedBy().getFullName() : "System")
                .build();
    }

    public static AnnouncementResponseDto toAnnouncementResponseDto(Announcement announcement) {
        if (announcement == null) return null;

        return AnnouncementResponseDto.builder()
                .id(announcement.getId())
                .title(announcement.getTitle())
                .content(announcement.getContent())
                .targetAudience(announcement.getTargetAudience().name())
                .createdByFullName(announcement.getCreatedBy() != null ? announcement.getCreatedBy().getFullName() : "Admin")
                .createdAt(announcement.getCreatedAt())
                .build();
    }

    public static NoteResponseDto toNoteResponseDto(Note note) {
        if (note == null) return null;

        return NoteResponseDto.builder()
                .id(note.getId())
                .courseId(note.getCourse().getId())
                .courseCode(note.getCourse().getCode())
                .courseName(note.getCourse().getName())
                .title(note.getTitle())
                .fileName(note.getFileName())
                .fileType(note.getFileType())
                .filePath(note.getFilePath())
                .uploadedByFullName(note.getUploadedBy() != null ? note.getUploadedBy().getFullName() : "Faculty")
                .uploadedAt(note.getUploadedAt())
                .build();
    }
}
