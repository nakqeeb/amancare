// src/main/java/com/nakqeeb/amancare/service/PublicService.java

package com.nakqeeb.amancare.service;

import com.nakqeeb.amancare.dto.response.AnnouncementResponse;
import com.nakqeeb.amancare.dto.response.ClinicResponse;
import com.nakqeeb.amancare.dto.response.DoctorAvailabilityResponse;
import com.nakqeeb.amancare.entity.*;
import com.nakqeeb.amancare.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PublicService {

    private final AnnouncementRepository announcementRepository;
    private final UserRepository userRepository;
    private final ClinicRepository clinicRepository;
    private final DoctorScheduleRepository scheduleRepository;
    private final DoctorScheduleService doctorScheduleService;

    /**
     * Get active announcements for public display
     */
    public List<AnnouncementResponse> getActiveAnnouncements() {
        log.info("Fetching active announcements");

        List<Announcement> announcements = announcementRepository
                .findActiveAnnouncements(LocalDate.now());

        return announcements.stream()
                .map(AnnouncementResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get currently available doctors
     */
    public List<DoctorAvailabilityResponse> getAvailableDoctors(Long clinicId) {
        log.info("Fetching available doctors for clinic: {}", clinicId);

        List<User> doctors;
        if (clinicId != null) {
            Clinic clinic = clinicRepository.findById(clinicId)
                    .orElseThrow(() -> new RuntimeException("العيادة غير موجودة"));
            doctors = userRepository.findByClinicAndRoleAndIsActiveTrue(clinic, UserRole.DOCTOR);
        } else {
            doctors = userRepository.findByRoleAndIsActiveTrue(UserRole.DOCTOR);
        }

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        DayOfWeek currentDay = today.getDayOfWeek();

        List<DoctorAvailabilityResponse> availableDoctors = new ArrayList<>();
        log.info("Found {} doctors for clinic {}", doctors.size(), clinicId);
        for (User doctor : doctors) {
            // Check if doctor is available now
            boolean isAvailable = doctorScheduleService.isDoctorAvailable(doctor, today, now);

            if (isAvailable) {
                DoctorAvailabilityResponse response = new DoctorAvailabilityResponse();
                response.setDoctorId(doctor.getId());
                response.setDoctorName(doctor.getFullName());
                response.setSpecialization(doctor.getSpecialization());
                response.setClinicId(doctor.getClinic().getId());
                response.setClinicName(doctor.getClinic().getName());
                response.setAvailableNow(true);
                // response.setProfileImage(doctor.getProfileImage());

                // Get schedule to determine available until time
                scheduleRepository.findDoctorScheduleForDay(doctor, currentDay, today)
                        .ifPresent(schedule -> {
                            response.setAvailableUntil(
                                    schedule.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm"))
                            );
                        });

                availableDoctors.add(response);
            }
        }

        return availableDoctors;
    }

    /**
     * Get active clinics for public display
     */
    public List<ClinicResponse> getActiveClinics() {
        log.info("Fetching active clinics for public");

        List<Clinic> clinics = clinicRepository.findByIsActiveTrue();

        // Return simplified clinic info (you can create a specific DTO for this)
        return clinics.stream()
                .map(ClinicResponse::fromEntity)
                .collect(Collectors.toList());
    }
}