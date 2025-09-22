package com.nakqeeb.amancare.dto.response.healthrecords;

import com.nakqeeb.amancare.entity.healthrecords.Referral;
import com.nakqeeb.amancare.entity.healthrecords.ReferralPriority;
import com.nakqeeb.amancare.entity.healthrecords.ReferralType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReferralResponse {

    private Long id;
    private ReferralType referralType;
    private String referralTypeArabic;
    private String referredTo;
    private String specialty;
    private ReferralPriority priority;
    private String priorityArabic;
    private String reason;
    private String notes;
    private LocalDate referralDate;
    private LocalDate appointmentDate;
    private Boolean isCompleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ReferralResponse fromEntity(Referral referral) {
        return ReferralResponse.builder()
                .id(referral.getId())
                .referralType(referral.getReferralType())
                .referralTypeArabic(referral.getReferralType() != null ?
                        referral.getReferralType().getArabicName() : null)
                .referredTo(referral.getReferredTo())
                .specialty(referral.getSpecialty())
                .priority(referral.getPriority())
                .priorityArabic(referral.getPriority() != null ?
                        referral.getPriority().getArabicName() : null)
                .reason(referral.getReason())
                .notes(referral.getNotes())
                .referralDate(referral.getReferralDate())
                .appointmentDate(referral.getAppointmentDate())
                .isCompleted(referral.getIsCompleted())
                .createdAt(referral.getCreatedAt())
                .updatedAt(referral.getUpdatedAt())
                .build();
    }
}
