// =============================================================================
// Announcement Service - خدمة إدارة الإعلانات
// =============================================================================

package com.nakqeeb.amancare.service;

import com.nakqeeb.amancare.dto.request.CreateAnnouncementRequest;
import com.nakqeeb.amancare.dto.request.UpdateAnnouncementRequest;
import com.nakqeeb.amancare.dto.response.AnnouncementResponse;
import com.nakqeeb.amancare.entity.Announcement;
import com.nakqeeb.amancare.entity.Clinic;
import com.nakqeeb.amancare.entity.User;
import com.nakqeeb.amancare.exception.BadRequestException;
import com.nakqeeb.amancare.exception.ResourceNotFoundException;
import com.nakqeeb.amancare.repository.AnnouncementRepository;
import com.nakqeeb.amancare.repository.ClinicRepository;
import com.nakqeeb.amancare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final ClinicRepository clinicRepository;
    private final UserRepository userRepository;

    /**
     * Get all announcements with pagination
     */
    public Page<AnnouncementResponse> getAllAnnouncements(Pageable pageable) {
        log.info("Fetching all announcements with pagination");
        Page<Announcement> announcements = announcementRepository.findAll(pageable);
        return announcements.map(AnnouncementResponse::fromEntity);
    }

    /**
     * Get all announcements as list (no pagination)
     */
    public List<AnnouncementResponse> getAllAnnouncementsList() {
        log.info("Fetching all announcements as list");
        List<Announcement> announcements = announcementRepository.findAll();
        return announcements.stream()
                .map(AnnouncementResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get announcement by ID
     */
    public AnnouncementResponse getAnnouncementById(Long id) {
        log.info("Fetching announcement with ID: {}", id);
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("الإعلان غير موجود بمعرف: " + id));
        return AnnouncementResponse.fromEntity(announcement);
    }

    /**
     * Create new announcement
     */
    @Transactional
    public AnnouncementResponse createAnnouncement(CreateAnnouncementRequest request) {
        log.info("Creating new announcement with type: {}", request.getType());

        // Validate dates
        validateDates(request.getStartDate(), request.getEndDate());

        Announcement announcement = new Announcement();
        announcement.setType(request.getType());
        announcement.setTitle(request.getTitle());
        announcement.setMessage(request.getMessage());
        announcement.setPriority(request.getPriority());
        announcement.setStartDate(request.getStartDate());
        announcement.setEndDate(request.getEndDate());
        announcement.setIsActive(request.getIsActive());
        announcement.setImageUrl(request.getImageUrl());
        announcement.setActionUrl(request.getActionUrl());
        announcement.setActionText(request.getActionText());

        // Set clinic if provided
        if (request.getClinicId() != null) {
            Clinic clinic = clinicRepository.findById(request.getClinicId())
                    .orElseThrow(() -> new ResourceNotFoundException("العيادة غير موجودة بمعرف: " + request.getClinicId()));
            announcement.setClinic(clinic);
        }

        // Set doctor if provided
        if (request.getDoctorId() != null) {
            User doctor = userRepository.findById(request.getDoctorId())
                    .orElseThrow(() -> new ResourceNotFoundException("الطبيب غير موجود بمعرف: " + request.getDoctorId()));
            announcement.setDoctor(doctor);
        }

        Announcement savedAnnouncement = announcementRepository.save(announcement);
        log.info("Announcement created successfully with ID: {}", savedAnnouncement.getId());

        return AnnouncementResponse.fromEntity(savedAnnouncement);
    }

    /**
     * Update existing announcement
     */
    @Transactional
    public AnnouncementResponse updateAnnouncement(Long id, UpdateAnnouncementRequest request) {
        log.info("Updating announcement with ID: {}", id);

        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("الإعلان غير موجود بمعرف: " + id));

        // Update fields if provided
        if (request.getType() != null) {
            announcement.setType(request.getType());
        }
        if (request.getTitle() != null) {
            announcement.setTitle(request.getTitle());
        }
        if (request.getMessage() != null) {
            announcement.setMessage(request.getMessage());
        }
        if (request.getPriority() != null) {
            announcement.setPriority(request.getPriority());
        }
        if (request.getStartDate() != null) {
            announcement.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            announcement.setEndDate(request.getEndDate());
        }

        // Validate dates if both are set
        if (announcement.getStartDate() != null && announcement.getEndDate() != null) {
            validateDates(announcement.getStartDate(), announcement.getEndDate());
        }

        if (request.getIsActive() != null) {
            announcement.setIsActive(request.getIsActive());
        }
        if (request.getImageUrl() != null) {
            announcement.setImageUrl(request.getImageUrl());
        }
        if (request.getActionUrl() != null) {
            announcement.setActionUrl(request.getActionUrl());
        }
        if (request.getActionText() != null) {
            announcement.setActionText(request.getActionText());
        }

        // Update clinic if provided
        if (request.getClinicId() != null) {
            Clinic clinic = clinicRepository.findById(request.getClinicId())
                    .orElseThrow(() -> new ResourceNotFoundException("العيادة غير موجودة بمعرف: " + request.getClinicId()));
            announcement.setClinic(clinic);
        }

        // Update doctor if provided
        if (request.getDoctorId() != null) {
            User doctor = userRepository.findById(request.getDoctorId())
                    .orElseThrow(() -> new ResourceNotFoundException("الطبيب غير موجود بمعرف: " + request.getDoctorId()));
            announcement.setDoctor(doctor);
        }

        Announcement updatedAnnouncement = announcementRepository.save(announcement);
        log.info("Announcement updated successfully with ID: {}", id);

        return AnnouncementResponse.fromEntity(updatedAnnouncement);
    }

    /**
     * Activate announcement
     */
    @Transactional
    public AnnouncementResponse activateAnnouncement(Long id) {
        log.info("Activating announcement with ID: {}", id);

        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("الإعلان غير موجود بمعرف: " + id));

        announcement.setIsActive(true);
        Announcement savedAnnouncement = announcementRepository.save(announcement);

        log.info("Announcement activated successfully with ID: {}", id);
        return AnnouncementResponse.fromEntity(savedAnnouncement);
    }

    /**
     * Deactivate announcement
     */
    @Transactional
    public AnnouncementResponse deactivateAnnouncement(Long id) {
        log.info("Deactivating announcement with ID: {}", id);

        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("الإعلان غير موجود بمعرف: " + id));

        announcement.setIsActive(false);
        Announcement savedAnnouncement = announcementRepository.save(announcement);

        log.info("Announcement deactivated successfully with ID: {}", id);
        return AnnouncementResponse.fromEntity(savedAnnouncement);
    }

    /**
     * Delete announcement
     */
    @Transactional
    public void deleteAnnouncement(Long id) {
        log.info("Deleting announcement with ID: {}", id);

        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("الإعلان غير موجود بمعرف: " + id));

        announcementRepository.delete(announcement);
        log.info("Announcement deleted successfully with ID: {}", id);
    }

    /**
     * Validate dates
     */
    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new BadRequestException("تاريخ النهاية يجب أن يكون بعد تاريخ البداية");
        }
    }
}