// src/main/java/com/nakqeeb/amancare/repository/AnnouncementRepository.java

package com.nakqeeb.amancare.repository;

import com.nakqeeb.amancare.entity.Announcement;
import com.nakqeeb.amancare.entity.AnnouncementType;
import com.nakqeeb.amancare.entity.Clinic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    /**
     * Find all currently active announcements
     */
    @Query("SELECT a FROM Announcement a WHERE a.isActive = true " +
            "AND a.startDate <= :today " +
            "AND (a.endDate IS NULL OR a.endDate >= :today) " +
            "ORDER BY a.priority DESC, a.createdAt DESC")
    List<Announcement> findActiveAnnouncements(@Param("today") LocalDate today);

    /**
     * Find active announcements for specific clinic
     */
    @Query("SELECT a FROM Announcement a WHERE a.isActive = true " +
            "AND a.startDate <= :today " +
            "AND (a.endDate IS NULL OR a.endDate >= :today) " +
            "AND (a.clinic IS NULL OR a.clinic = :clinic) " +
            "ORDER BY a.priority DESC, a.createdAt DESC")
    List<Announcement> findActiveAnnouncementsByClinic(
            @Param("clinic") Clinic clinic,
            @Param("today") LocalDate today);

    /**
     * Find announcements by type
     */
    List<Announcement> findByTypeAndIsActiveTrueOrderByPriorityDescCreatedAtDesc(AnnouncementType type);

    /**
     * Find all announcements for clinic
     */
    List<Announcement> findByClinicOrderByCreatedAtDesc(Clinic clinic);
}