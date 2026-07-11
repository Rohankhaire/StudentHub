package com.studenthub.serviceImpl;

import com.studenthub.dto.*;
import com.studenthub.entity.*;
import com.studenthub.exception.DuplicateResourceException;
import com.studenthub.exception.ResourceNotFoundException;
import com.studenthub.mapper.DtoMapper;
import com.studenthub.repository.*;
import com.studenthub.service.AdminService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminServiceImpl(UserRepository userRepository,
                            RoleRepository roleRepository,
                            DepartmentRepository departmentRepository,
                            CourseRepository courseRepository,
                            StudentRepository studentRepository,
                            FacultyRepository facultyRepository,
                            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.departmentRepository = departmentRepository;
        this.courseRepository = courseRepository;
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ==================================================
    // DEPARTMENTS
    // ==================================================
    @Override
    @Transactional
    public DepartmentResponseDto createDepartment(DepartmentRequestDto deptDto) {
        if (departmentRepository.existsByCode(deptDto.getCode())) {
            throw new DuplicateResourceException("Department code already exists: " + deptDto.getCode());
        }
        if (departmentRepository.existsByName(deptDto.getName())) {
            throw new DuplicateResourceException("Department name already exists: " + deptDto.getName());
        }

        Department department = Department.builder()
                .name(deptDto.getName())
                .code(deptDto.getCode())
                .description(deptDto.getDescription())
                .build();

        department = departmentRepository.save(department);
        return DtoMapper.toDepartmentResponseDto(department);
    }

    @Override
    @Transactional
    public DepartmentResponseDto updateDepartment(Long id, DepartmentRequestDto deptDto) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));

        departmentRepository.findByCode(deptDto.getCode()).ifPresent(d -> {
            if (!d.getId().equals(id)) {
                throw new DuplicateResourceException("Department code already in use: " + deptDto.getCode());
            }
        });

        department.setName(deptDto.getName());
        department.setCode(deptDto.getCode());
        department.setDescription(deptDto.getDescription());

        department = departmentRepository.save(department);
        return DtoMapper.toDepartmentResponseDto(department);
    }

    @Override
    public DepartmentResponseDto getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
        long studentCount = studentRepository.countByDepartmentId(id);
        long facultyCount = facultyRepository.countByDepartmentId(id);
        return DtoMapper.toDepartmentResponseDto(department, studentCount, facultyCount);
    }

    @Override
    public Page<DepartmentResponseDto> getAllDepartments(String search, Pageable pageable) {
        Page<Department> departmentsPage;
        if (search != null && !search.trim().isEmpty()) {
            departmentsPage = departmentRepository.searchDepartments(search.trim(), pageable);
        } else {
            departmentsPage = departmentRepository.findAll(pageable);
        }

        return departmentsPage.map(d -> {
            long studentCount = studentRepository.countByDepartmentId(d.getId());
            long facultyCount = facultyRepository.countByDepartmentId(d.getId());
            return DtoMapper.toDepartmentResponseDto(d, studentCount, facultyCount);
        });
    }

    @Override
    @Transactional
    public void deleteDepartment(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Department not found with id: " + id);
        }
        departmentRepository.deleteById(id);
    }

    // ==================================================
    // COURSES
    // ==================================================
    @Override
    @Transactional
    public CourseResponseDto createCourse(CourseRequestDto courseDto) {
        if (courseRepository.existsByCode(courseDto.getCode())) {
            throw new DuplicateResourceException("Course code already exists: " + courseDto.getCode());
        }

        Department department = departmentRepository.findById(courseDto.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + courseDto.getDepartmentId()));

        Course course = Course.builder()
                .name(courseDto.getName())
                .code(courseDto.getCode())
                .description(courseDto.getDescription())
                .credits(courseDto.getCredits())
                .department(department)
                .build();

        course = courseRepository.save(course);
        return DtoMapper.toCourseResponseDto(course);
    }

    @Override
    @Transactional
    public CourseResponseDto updateCourse(Long id, CourseRequestDto courseDto) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));

        courseRepository.findByCode(courseDto.getCode()).ifPresent(c -> {
            if (!c.getId().equals(id)) {
                throw new DuplicateResourceException("Course code already in use: " + courseDto.getCode());
            }
        });

        Department department = departmentRepository.findById(courseDto.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + courseDto.getDepartmentId()));

        course.setName(courseDto.getName());
        course.setCode(courseDto.getCode());
        course.setDescription(courseDto.getDescription());
        course.setCredits(courseDto.getCredits());
        course.setDepartment(department);

        course = courseRepository.save(course);
        return DtoMapper.toCourseResponseDto(course);
    }

    @Override
    public CourseResponseDto getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
        return DtoMapper.toCourseResponseDto(course);
    }

    @Override
    public Page<CourseResponseDto> getAllCourses(String search, Pageable pageable) {
        Page<Course> coursesPage;
        if (search != null && !search.trim().isEmpty()) {
            coursesPage = courseRepository.searchCourses(search.trim(), pageable);
        } else {
            coursesPage = courseRepository.findAll(pageable);
        }
        return coursesPage.map(DtoMapper::toCourseResponseDto);
    }

    @Override
    @Transactional
    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Course not found with id: " + id);
        }
        courseRepository.deleteById(id);
    }

    @Override
    @Transactional
    public CourseResponseDto assignFacultyToCourse(Long courseId, Long facultyId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + courseId));
        Faculty faculty = facultyRepository.findById(facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found: " + facultyId));

        course.getFaculties().add(faculty);
        courseRepository.save(course);
        return DtoMapper.toCourseResponseDto(course);
    }

    @Override
    @Transactional
    public CourseResponseDto removeFacultyFromCourse(Long courseId, Long facultyId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + courseId));
        Faculty faculty = facultyRepository.findById(facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found: " + facultyId));

        course.getFaculties().remove(faculty);
        courseRepository.save(course);
        return DtoMapper.toCourseResponseDto(course);
    }

    // ==================================================
    // STUDENTS
    // ==================================================
    @Override
    @Transactional
    public StudentResponseDto createStudent(StudentRequestDto studentDto) {
        if (userRepository.existsByEmail(studentDto.getEmail())) {
            throw new DuplicateResourceException("Email already in use: " + studentDto.getEmail());
        }
        if (studentRepository.existsByRollNumber(studentDto.getRollNumber())) {
            throw new DuplicateResourceException("Roll number already in use: " + studentDto.getRollNumber());
        }

        Department department = departmentRepository.findById(studentDto.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + studentDto.getDepartmentId()));

        Role role = roleRepository.findByName("ROLE_STUDENT")
                .orElseThrow(() -> new ResourceNotFoundException("Student role not found"));

        String rawPassword = studentDto.getPassword() != null ? studentDto.getPassword() : "Student@123";

        User user = User.builder()
                .email(studentDto.getEmail())
                .password(passwordEncoder.encode(rawPassword))
                .fullName(studentDto.getFullName())
                .phone(studentDto.getPhone())
                .status(UserStatus.ACTIVE)
                .roles(new HashSet<>(Collections.singletonList(role)))
                .build();

        Student student = Student.builder()
                .user(user)
                .rollNumber(studentDto.getRollNumber())
                .department(department)
                .dateOfBirth(studentDto.getDateOfBirth())
                .enrollmentDate(studentDto.getEnrollmentDate())
                .build();

        student = studentRepository.save(student);
        return DtoMapper.toStudentResponseDto(student);
    }

    @Override
    @Transactional
    public StudentResponseDto updateStudent(Long id, StudentRequestDto studentDto) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));

        final Long studentUserId = student.getUser().getId();
        userRepository.findByEmail(studentDto.getEmail()).ifPresent(u -> {
            if (!u.getId().equals(studentUserId)) {
                throw new DuplicateResourceException("Email already in use: " + studentDto.getEmail());
            }
        });

        studentRepository.findByRollNumber(studentDto.getRollNumber()).ifPresent(s -> {
            if (!s.getId().equals(id)) {
                throw new DuplicateResourceException("Roll number already in use: " + studentDto.getRollNumber());
            }
        });

        Department department = departmentRepository.findById(studentDto.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + studentDto.getDepartmentId()));

        User user = student.getUser();
        user.setEmail(studentDto.getEmail());
        user.setFullName(studentDto.getFullName());
        user.setPhone(studentDto.getPhone());
        if (studentDto.getPassword() != null && !studentDto.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(studentDto.getPassword()));
        }

        student.setRollNumber(studentDto.getRollNumber());
        student.setDepartment(department);
        student.setDateOfBirth(studentDto.getDateOfBirth());
        student.setEnrollmentDate(studentDto.getEnrollmentDate());

        student = studentRepository.save(student);
        return DtoMapper.toStudentResponseDto(student);
    }

    @Override
    public StudentResponseDto getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
        return DtoMapper.toStudentResponseDto(student);
    }

    @Override
    public Page<StudentResponseDto> getAllStudents(String search, Pageable pageable) {
        Page<Student> studentsPage;
        if (search != null && !search.trim().isEmpty()) {
            studentsPage = studentRepository.searchStudents(search.trim(), pageable);
        } else {
            studentsPage = studentRepository.findAll(pageable);
        }
        return studentsPage.map(DtoMapper::toStudentResponseDto);
    }

    @Override
    @Transactional
    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
        // Deleting student cascade deletes user due to CascadeType.ALL on student.user
        studentRepository.delete(student);
    }

    // ==================================================
    // FACULTY
    // ==================================================
    @Override
    @Transactional
    public FacultyResponseDto createFaculty(FacultyRequestDto facultyDto) {
        if (userRepository.existsByEmail(facultyDto.getEmail())) {
            throw new DuplicateResourceException("Email already in use: " + facultyDto.getEmail());
        }
        if (facultyRepository.existsByEmployeeId(facultyDto.getEmployeeId())) {
            throw new DuplicateResourceException("Employee ID already in use: " + facultyDto.getEmployeeId());
        }

        Department department = departmentRepository.findById(facultyDto.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + facultyDto.getDepartmentId()));

        Role role = roleRepository.findByName("ROLE_FACULTY")
                .orElseThrow(() -> new ResourceNotFoundException("Faculty role not found"));

        String rawPassword = facultyDto.getPassword() != null ? facultyDto.getPassword() : "Faculty@123";

        User user = User.builder()
                .email(facultyDto.getEmail())
                .password(passwordEncoder.encode(rawPassword))
                .fullName(facultyDto.getFullName())
                .phone(facultyDto.getPhone())
                .status(UserStatus.ACTIVE)
                .roles(new HashSet<>(Collections.singletonList(role)))
                .build();

        Faculty faculty = Faculty.builder()
                .user(user)
                .employeeId(facultyDto.getEmployeeId())
                .department(department)
                .designation(facultyDto.getDesignation())
                .joiningDate(facultyDto.getJoiningDate())
                .build();

        faculty = facultyRepository.save(faculty);
        return DtoMapper.toFacultyResponseDto(faculty);
    }

    @Override
    @Transactional
    public FacultyResponseDto updateFaculty(Long id, FacultyRequestDto facultyDto) {
        Faculty faculty = facultyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found with id: " + id));

        final Long facultyUserId = faculty.getUser().getId();
        userRepository.findByEmail(facultyDto.getEmail()).ifPresent(u -> {
            if (!u.getId().equals(facultyUserId)) {
                throw new DuplicateResourceException("Email already in use: " + facultyDto.getEmail());
            }
        });

        facultyRepository.findByEmployeeId(facultyDto.getEmployeeId()).ifPresent(f -> {
            if (!f.getId().equals(id)) {
                throw new DuplicateResourceException("Employee ID already in use: " + facultyDto.getEmployeeId());
            }
        });

        Department department = departmentRepository.findById(facultyDto.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + facultyDto.getDepartmentId()));

        User user = faculty.getUser();
        user.setEmail(facultyDto.getEmail());
        user.setFullName(facultyDto.getFullName());
        user.setPhone(facultyDto.getPhone());
        if (facultyDto.getPassword() != null && !facultyDto.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(facultyDto.getPassword()));
        }

        faculty.setEmployeeId(facultyDto.getEmployeeId());
        faculty.setDepartment(department);
        faculty.setDesignation(facultyDto.getDesignation());
        faculty.setJoiningDate(facultyDto.getJoiningDate());

        faculty = facultyRepository.save(faculty);
        return DtoMapper.toFacultyResponseDto(faculty);
    }

    @Override
    public FacultyResponseDto getFacultyById(Long id) {
        Faculty faculty = facultyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found with id: " + id));
        return DtoMapper.toFacultyResponseDto(faculty);
    }

    @Override
    public Page<FacultyResponseDto> getAllFaculty(String search, Pageable pageable) {
        Page<Faculty> facultyPage;
        if (search != null && !search.trim().isEmpty()) {
            facultyPage = facultyRepository.searchFaculty(search.trim(), pageable);
        } else {
            facultyPage = facultyRepository.findAll(pageable);
        }
        return facultyPage.map(DtoMapper::toFacultyResponseDto);
    }

    @Override
    @Transactional
    public void deleteFaculty(Long id) {
        Faculty faculty = facultyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found with id: " + id));
        // Deleting faculty cascade deletes user due to CascadeType.ALL on faculty.user
        facultyRepository.delete(faculty);
    }

    // ==================================================
    // STATS
    // ==================================================
    @Override
    public Map<String, Object> getAdminDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalStudents", studentRepository.count());
        stats.put("totalFaculty", facultyRepository.count());
        stats.put("totalCourses", courseRepository.count());
        stats.put("totalDepartments", departmentRepository.count());
        return stats;
    }
}
