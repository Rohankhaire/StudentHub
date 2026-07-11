package com.studenthub.service;

import com.studenthub.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AnnouncementService {
    AnnouncementResponseDto createAnnouncement(AnnouncementRequestDto dto, String creatorEmail);
    Page<AnnouncementResponseDto> getAnnouncementsForUser(String userEmail, Pageable pageable);
    Page<AnnouncementResponseDto> getAllAnnouncements(String search, Pageable pageable);
    void deleteAnnouncement(Long id);
}
