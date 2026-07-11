package com.studenthub.serviceImpl;

import com.studenthub.dto.*;
import com.studenthub.entity.*;
import com.studenthub.exception.ResourceNotFoundException;
import com.studenthub.mapper.DtoMapper;
import com.studenthub.repository.*;
import com.studenthub.service.FileStorageService;
import com.studenthub.service.StudentService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final AttendanceRepository attendanceRepository;
    private final MarkRepository markRepository;
    private final NoteRepository noteRepository;
    private final FileStorageService fileStorageService;
    private final PasswordEncoder passwordEncoder;

    public StudentServiceImpl(StudentRepository studentRepository,
                              AttendanceRepository attendanceRepository,
                              MarkRepository markRepository,
                              NoteRepository noteRepository,
                              FileStorageService fileStorageService,
                              PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.attendanceRepository = attendanceRepository;
        this.markRepository = markRepository;
        this.noteRepository = noteRepository;
        this.fileStorageService = fileStorageService;
        this.passwordEncoder = passwordEncoder;
    }

    private Student getStudentByEmail(String email) {
        return studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student account not found for email: " + email));
    }

    @Override
    public StudentResponseDto getProfileByEmail(String studentEmail) {
        Student student = getStudentByEmail(studentEmail);
        return DtoMapper.toStudentResponseDto(student);
    }

    @Override
    @Transactional
    public StudentResponseDto updateProfile(String studentEmail, String phone, LocalDate dob, String password, MultipartFile profilePic) {
        Student student = getStudentByEmail(studentEmail);
        User user = student.getUser();

        if (phone != null) {
            user.setPhone(phone);
        }
        if (dob != null) {
            student.setDateOfBirth(dob);
        }
        if (password != null && !password.trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(password));
        }

        if (profilePic != null && !profilePic.isEmpty()) {
            // Delete old profile picture if exists
            if (student.getProfilePicturePath() != null) {
                fileStorageService.deleteFile(student.getProfilePicturePath());
            }
            String path = fileStorageService.storeFile(profilePic, "profiles");
            student.setProfilePicturePath(path);
        }

        studentRepository.save(student);
        return DtoMapper.toStudentResponseDto(student);
    }

    @Override
    public List<AttendanceResponseDto> getMyAttendance(String studentEmail) {
        Student student = getStudentByEmail(studentEmail);
        List<Attendance> attendances = attendanceRepository.findByStudentId(student.getId());
        return attendances.stream()
                .map(DtoMapper::toAttendanceResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<MarkResponseDto> getMyMarks(String studentEmail) {
        Student student = getStudentByEmail(studentEmail);
        List<Mark> marks = markRepository.findByStudentId(student.getId());
        return marks.stream()
                .map(DtoMapper::toMarkResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CourseResponseDto> getMyCourses(String studentEmail) {
        Student student = getStudentByEmail(studentEmail);
        return student.getCourses().stream()
                .map(DtoMapper::toCourseResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<NoteResponseDto> getNotesForMyCourses(String studentEmail) {
        Student student = getStudentByEmail(studentEmail);
        List<NoteResponseDto> notes = new ArrayList<>();
        for (Course course : student.getCourses()) {
            List<Note> courseNotes = noteRepository.findByCourseId(course.getId());
            notes.addAll(courseNotes.stream()
                    .map(DtoMapper::toNoteResponseDto)
                    .collect(Collectors.toList()));
        }
        return notes;
    }

    @Override
    public Map<String, Object> getStudentDashboardStats(String studentEmail) {
        Student student = getStudentByEmail(studentEmail);
        Long studentId = student.getId();

        long totalAttendanceRecords = attendanceRepository.countByStudentId(studentId);
        long presentCount = attendanceRepository.countByStudentIdAndStatus(studentId, AttendanceStatus.PRESENT);
        long lateCount = attendanceRepository.countByStudentIdAndStatus(studentId, AttendanceStatus.LATE);

        double attendanceRate = 0.0;
        if (totalAttendanceRecords > 0) {
            // Count late as half-day presence or full presence based on policy. Let's count PRESENT + LATE as attended days.
            attendanceRate = ((double)(presentCount + lateCount) / totalAttendanceRecords) * 100;
        }

        Double avgMarks = markRepository.getAveragePercentageForStudent(studentId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("attendanceRate", Math.round(attendanceRate * 100.0) / 100.0);
        stats.put("averageMarks", avgMarks != null ? Math.round(avgMarks * 100.0) / 100.0 : 0.0);
        stats.put("enrolledCoursesCount", student.getCourses().size());

        return stats;
    }
}
