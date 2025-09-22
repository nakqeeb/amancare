package com.nakqeeb.amancare.entity.healthrecords;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.nakqeeb.amancare.entity.MedicalRecord;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

// =============================================================================
// Referral Entity - كيان التحويل
// =============================================================================
@Entity
@Table(name = "medical_record_referrals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = "medicalRecord")
public class Referral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_record_id", nullable = false)
    @JsonBackReference
    private MedicalRecord medicalRecord;

    @Enumerated(EnumType.STRING)
    @Column(name = "referral_type", nullable = false, length = 20)
    @NotNull(message = "نوع التحويل مطلوب")
    private ReferralType referralType;

    @Column(name = "referred_to", nullable = false, length = 200)
    @NotBlank(message = "المحول إليه مطلوب")
    @Size(max = 200, message = "المحول إليه يجب أن يكون أقل من 200 حرف")
    private String referredTo;

    @Column(name = "specialty", length = 100)
    @Size(max = 100, message = "التخصص يجب أن يكون أقل من 100 حرف")
    private String specialty;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    @NotNull(message = "أولوية التحويل مطلوبة")
    private ReferralPriority priority;

    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "سبب التحويل مطلوب")
    private String reason;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "referral_date", nullable = false)
    @NotNull(message = "تاريخ التحويل مطلوب")
    private LocalDate referralDate;

    @Column(name = "appointment_date")
    private LocalDate appointmentDate;

    @Column(name = "is_completed", nullable = false)
    @Builder.Default
    private Boolean isCompleted = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
