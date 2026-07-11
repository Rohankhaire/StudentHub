package com.studenthub.controller;

import com.studenthub.dto.*;
import com.studenthub.service.AnnouncementService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/announcements")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    public AnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY')")
    public ResponseEntity<AnnouncementResponseDto> createAnnouncement(
            @Valid @RequestBody AnnouncementRequestDto announcementDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        return new ResponseEntity<>(
                announcementService.createAnnouncement(announcementDto, userDetails.getUsername()),
                HttpStatus.CREATED
        );
    }

    @GetMapping
    public ResponseEntity<Page<AnnouncementResponseDto>> getAnnouncements(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(announcementService.getAnnouncementsForUser(userDetails.getUsername(), pageable));
    }

    @GetMapping("/admin-all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AnnouncementResponseDto>> getAllAnnouncements(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(announcementService.getAllAnnouncements(search, pageable));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY')")
    public ResponseEntity<Map<String, String>> deleteAnnouncement(@PathVariable Long id) {
        announcementService.deleteAnnouncement(id);
        return ResponseEntity.ok(Map.of("message", "Announcement deleted successfully"));
    }
}
