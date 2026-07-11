package com.studenthub.serviceImpl;

import com.studenthub.entity.Course;
import com.studenthub.entity.Faculty;
import com.studenthub.entity.Student;
import com.studenthub.repository.CourseRepository;
import com.studenthub.repository.FacultyRepository;
import com.studenthub.repository.StudentRepository;
import com.studenthub.service.ReportService;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {

    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final CourseRepository courseRepository;

    public ReportServiceImpl(StudentRepository studentRepository,
                             FacultyRepository facultyRepository,
                             CourseRepository courseRepository) {
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
        this.courseRepository = courseRepository;
    }

    @Override
    public ByteArrayInputStream exportStudentsCsv() {
        List<Student> students = studentRepository.findAll();
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(out)) {
            // Header
            writer.println("Roll Number,Full Name,Email,Phone,Department,Enrollment Date");
            
            for (Student s : students) {
                writer.println(String.format("%s,%s,%s,%s,%s,%s",
                        escapeCsv(s.getRollNumber()),
                        escapeCsv(s.getUser().getFullName()),
                        escapeCsv(s.getUser().getEmail()),
                        escapeCsv(s.getUser().getPhone()),
                        escapeCsv(s.getDepartment() != null ? s.getDepartment().getName() : "N/A"),
                        s.getEnrollmentDate()
                ));
            }
            writer.flush();
        }
        return new ByteArrayInputStream(out.toByteArray());
    }

    @Override
    public ByteArrayInputStream exportFacultyCsv() {
        List<Faculty> facultyList = facultyRepository.findAll();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(out)) {
            writer.println("Employee ID,Full Name,Email,Phone,Department,Designation,Joining Date");

            for (Faculty f : facultyList) {
                writer.println(String.format("%s,%s,%s,%s,%s,%s,%s",
                        escapeCsv(f.getEmployeeId()),
                        escapeCsv(f.getUser().getFullName()),
                        escapeCsv(f.getUser().getEmail()),
                        escapeCsv(f.getUser().getPhone()),
                        escapeCsv(f.getDepartment() != null ? f.getDepartment().getName() : "N/A"),
                        escapeCsv(f.getDesignation()),
                        f.getJoiningDate()
                ));
            }
            writer.flush();
        }
        return new ByteArrayInputStream(out.toByteArray());
    }

    @Override
    public ByteArrayInputStream exportCoursesCsv() {
        List<Course> courses = courseRepository.findAll();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(out)) {
            writer.println("Course Code,Course Name,Credits,Department");

            for (Course c : courses) {
                writer.println(String.format("%s,%s,%d,%s",
                        escapeCsv(c.getCode()),
                        escapeCsv(c.getName()),
                        c.getCredits(),
                        escapeCsv(c.getDepartment() != null ? c.getDepartment().getName() : "N/A")
                ));
            }
            writer.flush();
        }
        return new ByteArrayInputStream(out.toByteArray());
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
