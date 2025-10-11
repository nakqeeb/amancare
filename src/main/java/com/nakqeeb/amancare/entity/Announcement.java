// src/main/java/com/nakqeeb/amancare/entity/Announcement.java

package com.nakqeeb.amancare.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "announcements",
        indexes = {
                @Index(name = "idx_announcement_active", columnList = "is_active"),
                @Index(name = "idx_announcement_dates", columnList = "start_date, end_date"),
                @Index(name = "idx_announcement_clinic", columnList = "clinic_id")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Announcement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    @NotNull(message = "نوع الإعلان مطلوب")
    private AnnouncementType type;

    @NotBlank(message = "عنوان الإعلان مطلوب")
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @NotBlank(message = "نص الإعلان مطلوب")
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinic_id")
    private Clinic clinic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private User doctor;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 20)
    private AnnouncementPriority priority = AnnouncementPriority.MEDIUM;

    @NotNull(message = "تاريخ البداية مطلوب")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "action_url")
    private String actionUrl;

    @Column(name = "action_text", length = 100)
    private String actionText;

    public boolean isCurrentlyActive() {
        if (!isActive) return false;

        LocalDate today = LocalDate.now();
        boolean afterStart = !today.isBefore(startDate);
        boolean beforeEnd = endDate == null || !today.isAfter(endDate);

        return afterStart && beforeEnd;
    }
}