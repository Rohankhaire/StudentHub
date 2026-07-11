package com.studenthub.repository;

import com.studenthub.entity.Faculty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FacultyRepository extends JpaRepository<Faculty, Long> {
    Optional<Faculty> findByUserEmail(String email);
    Optional<Faculty> findByEmployeeId(String employeeId);
    boolean existsByEmployeeId(String employeeId);

    @Query("SELECT f FROM Faculty f JOIN f.user u WHERE " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(f.employeeId) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Faculty> searchFaculty(@Param("search") String search, Pageable pageable);

    @Query("SELECT COUNT(f) FROM Faculty f WHERE f.department.id = :deptId")
    long countByDepartmentId(@Param("deptId") Long deptId);
}
