package com.studenthub.serviceImpl;

import com.studenthub.dto.*;
import com.studenthub.entity.Announcement;
import com.studenthub.entity.TargetAudience;
import com.studenthub.entity.User;
import com.studenthub.exception.ResourceNotFoundException;
import com.studenthub.mapper.DtoMapper;
import com.studenthub.repository.AnnouncementRepository;
import com.studenthub.repository.UserRepository;
import com.studenthub.service.AnnouncementService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final UserRepository userRepository;

    public AnnouncementServiceImpl(AnnouncementRepository announcementRepository, UserRepository userRepository) {
        this.announcementRepository = announcementRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public AnnouncementResponseDto createAnnouncement(AnnouncementRequestDto dto, String creatorEmail) {
        User creator = userRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Creator user not found"));

        TargetAudience targetAudience = TargetAudience.valueOf(dto.getTargetAudience().toUpperCase());

        Announcement announcement = Announcement.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .createdBy(creator)
                .targetAudience(targetAudience)
                .build();

        announcement = announcementRepository.save(announcement);
        return DtoMapper.toAnnouncementResponseDto(announcement);
    }

    @Override
    public Page<AnnouncementResponseDto> getAnnouncementsForUser(String userEmail, Pageable pageable) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<TargetAudience> audiences = new ArrayList<>();
        audiences.add(TargetAudience.ALL);

        boolean isAdmin = user.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"));
        boolean isFaculty = user.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_FACULTY"));
        boolean isStudent = user.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_STUDENT"));

        if (isAdmin) {
            audiences.add(TargetAudience.FACULTY);
            audiences.add(TargetAudience.STUDENT);
        } else if (isFaculty) {
            audiences.add(TargetAudience.FACULTY);
        } else if (isStudent) {
            audiences.add(TargetAudience.STUDENT);
        }

        return announcementRepository.findAnnouncementsByAudience(audiences, pageable)
                .map(DtoMapper::toAnnouncementResponseDto);
    }

    @Override
    public Page<AnnouncementResponseDto> getAllAnnouncements(String search, Pageable pageable) {
        if (search != null && !search.trim().isEmpty()) {
            return announcementRepository.searchAnnouncements(search.trim(), pageable)
                    .map(DtoMapper::toAnnouncementResponseDto);
        }
        return announcementRepository.findAll(pageable)
                .map(DtoMapper::toAnnouncementResponseDto);
    }

    @Override
    @Transactional
    public void deleteAnnouncement(Long id) {
        if (!announcementRepository.existsById(id)) {
            throw new ResourceNotFoundException("Announcement not found: " + id);
        }
        announcementRepository.deleteById(id);
    }
}
