package com.studenthub.repository;

import com.studenthub.entity.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByCourseId(Long courseId);

    @Query("SELECT n FROM Note n WHERE n.course.id = :courseId")
    Page<Note> findByCourseIdPageable(@Param("courseId") Long courseId, Pageable pageable);

    @Query("SELECT n FROM Note n WHERE " +
           "LOWER(n.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(n.fileName) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Note> searchNotes(@Param("search") String search, Pageable pageable);
}
