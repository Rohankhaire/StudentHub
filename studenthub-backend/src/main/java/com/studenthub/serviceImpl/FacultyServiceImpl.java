package com.studenthub.serviceImpl;

import com.studenthub.dto.*;
import com.studenthub.entity.*;
import com.studenthub.exception.ResourceNotFoundException;
import com.studenthub.exception.UnauthorizedException;
import com.studenthub.mapper.DtoMapper;
import com.studenthub.repository.*;
import com.studenthub.service.FacultyService;
import com.studenthub.service.FileStorageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FacultyServiceImpl implements FacultyService {

    private final FacultyRepository facultyRepository;
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;
    private final AttendanceRepository attendanceRepository;
    private final MarkRepository markRepository;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public FacultyServiceImpl(FacultyRepository facultyRepository,
                              CourseRepository courseRepository,
                              StudentRepository studentRepository,
                              AttendanceRepository attendanceRepository,
                              MarkRepository markRepository,
                              NoteRepository noteRepository,
                              UserRepository userRepository,
                              FileStorageService fileStorageService) {
        this.facultyRepository = facultyRepository;
        this.courseRepository = courseRepository;
        this.studentRepository = studentRepository;
        this.attendanceRepository = attendanceRepository;
        this.markRepository = markRepository;
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    private Faculty getFacultyByEmail(String email) {
        return facultyRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty account not found for email: " + email));
    }

    @Override
    public List<CourseResponseDto> getAssignedCourses(String facultyEmail) {
        Faculty faculty = getFacultyByEmail(facultyEmail);
        List<Course> courses = courseRepository.findCoursesAssignedToFaculty(faculty.getId());
        return courses.stream()
                .map(DtoMapper::toCourseResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AttendanceResponseDto saveAttendance(AttendanceRequestDto dto, String facultyEmail) {
        Faculty faculty = getFacultyByEmail(facultyEmail);
        
        // Verify course is assigned to faculty
        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + dto.getCourseId()));
        if (!course.getFaculties().contains(faculty)) {
            throw new UnauthorizedException("You are not authorized to grade this course");
        }

        Student student = studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + dto.getStudentId()));

        AttendanceStatus status = AttendanceStatus.valueOf(dto.getStatus().toUpperCase());

        Attendance attendance = attendanceRepository.findByStudentIdAndCourseIdAndDate(
                dto.getStudentId(), dto.getCourseId(), dto.getDate())
                .orElse(new Attendance());

        attendance.setStudent(student);
        attendance.setCourse(course);
        attendance.setDate(dto.getDate());
        attendance.setStatus(status);
        attendance.setRemarks(dto.getRemarks());
        attendance.setCreatedBy(faculty.getUser());

        attendance = attendanceRepository.save(attendance);
        return DtoMapper.toAttendanceResponseDto(attendance);
    }

    @Override
    public List<AttendanceResponseDto> getAttendanceByCourseAndDate(Long courseId, LocalDate date) {
        List<Attendance> attendances = attendanceRepository.findByCourseIdAndDate(courseId, date);
        return attendances.stream()
                .map(DtoMapper::toAttendanceResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MarkResponseDto saveMark(MarkRequestDto dto, String facultyEmail) {
        Faculty faculty = getFacultyByEmail(facultyEmail);
        
        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + dto.getCourseId()));
        if (!course.getFaculties().contains(faculty)) {
            throw new UnauthorizedException("You are not authorized to grade this course");
        }

        Student student = studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + dto.getStudentId()));

        ExamType examType = ExamType.valueOf(dto.getExamType().toUpperCase());

        // Upsert based on Student, Course, ExamType
        Mark mark = markRepository.findByStudentIdAndCourseId(dto.getStudentId(), dto.getCourseId()).stream()
                .filter(m -> m.getExamType() == examType)
                .findFirst()
                .orElse(new Mark());

        mark.setStudent(student);
        mark.setCourse(course);
        mark.setExamType(examType);
        mark.setMarksObtained(dto.getMarksObtained());
        mark.setMaxMarks(dto.getMaxMarks());
        mark.setGradingDate(dto.getGradingDate());
        mark.setRemarks(dto.getRemarks());
        mark.setGradedBy(faculty.getUser());

        mark = markRepository.save(mark);
        return DtoMapper.toMarkResponseDto(mark);
    }

    @Override
    public Page<MarkResponseDto> getMarksByCourse(Long courseId, Pageable pageable) {
        return markRepository.findByCourseId(courseId, pageable)
                .map(DtoMapper::toMarkResponseDto);
    }

    @Override
    @Transactional
    public NoteResponseDto uploadNote(Long courseId, String title, MultipartFile file, String facultyEmail) {
        Faculty faculty = getFacultyByEmail(facultyEmail);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + courseId));
        
        if (!course.getFaculties().contains(faculty)) {
            throw new UnauthorizedException("You are not authorized to upload notes for this course");
        }

        String filePath = fileStorageService.storeFile(file, "notes");

        Note note = Note.builder()
                .course(course)
                .title(title)
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .filePath(filePath)
                .uploadedBy(faculty.getUser())
                .build();

        note = noteRepository.save(note);
        return DtoMapper.toNoteResponseDto(note);
    }

    @Override
    @Transactional
    public void deleteNote(Long noteId, String facultyEmail) {
        Faculty faculty = getFacultyByEmail(facultyEmail);
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found: " + noteId));

        if (!note.getUploadedBy().getId().equals(faculty.getUser().getId())) {
            throw new UnauthorizedException("You are not authorized to delete this note");
        }

        fileStorageService.deleteFile(note.getFilePath());
        noteRepository.delete(note);
    }

    @Override
    public List<NoteResponseDto> getNotesByCourse(Long courseId) {
        return noteRepository.findByCourseId(courseId).stream()
                .map(DtoMapper::toNoteResponseDto)
                .collect(Collectors.toList());
    }
}
