package com.studenthub.repository;

import com.studenthub.entity.Announcement;
import com.studenthub.entity.TargetAudience;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    List<Announcement> findByTargetAudienceInOrderByCreatedAtDesc(Collection<TargetAudience> audiences);

    @Query("SELECT a FROM Announcement a WHERE a.targetAudience IN :audiences")
    Page<Announcement> findAnnouncementsByAudience(@Param("audiences") Collection<TargetAudience> audiences, Pageable pageable);

    @Query("SELECT a FROM Announcement a WHERE " +
           "LOWER(a.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.content) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Announcement> searchAnnouncements(@Param("search") String search, Pageable pageable);
}
