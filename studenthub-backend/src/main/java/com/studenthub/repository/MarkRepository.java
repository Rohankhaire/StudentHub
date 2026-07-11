package com.studenthub.repository;

import com.studenthub.entity.Mark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MarkRepository extends JpaRepository<Mark, Long> {
    List<Mark> findByStudentId(Long studentId);
    List<Mark> findByStudentIdAndCourseId(Long studentId, Long courseId);

    @Query("SELECT m FROM Mark m WHERE m.course.id = :courseId")
    Page<Mark> findByCourseId(@Param("courseId") Long courseId, Pageable pageable);

    @Query("SELECT AVG(m.marksObtained / m.maxMarks) * 100 FROM Mark m WHERE m.student.id = :studentId")
    Double getAveragePercentageForStudent(@Param("studentId") Long studentId);

    @Query("SELECT AVG(m.marksObtained / m.maxMarks) * 100 FROM Mark m WHERE m.course.id = :courseId")
    Double getAveragePercentageForCourse(@Param("courseId") Long courseId);
}
