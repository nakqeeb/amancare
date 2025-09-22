package com.nakqeeb.amancare.entity.healthrecords;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.nakqeeb.amancare.entity.MedicalRecord;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

// =============================================================================
// Prescription Entity - كيان الوصفة الطبية
// =============================================================================
@Entity
@Table(name = "medical_record_prescriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = "medicalRecord")
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_record_id", nullable = false)
    @JsonBackReference
    private MedicalRecord medicalRecord;

    @Column(name = "medication_name", nullable = false, length = 200)
    @NotBlank(message = "اسم الدواء مطلوب")
    @Size(max = 200, message = "اسم الدواء يجب أن يكون أقل من 200 حرف")
    private String medicationName;

    @Column(name = "generic_name", length = 200)
    @Size(max = 200, message = "الاسم العلمي يجب أن يكون أقل من 200 حرف")
    private String genericName;

    @Column(name = "dosage", nullable = false, length = 100)
    @NotBlank(message = "الجرعة مطلوبة")
    @Size(max = 100, message = "الجرعة يجب أن تكون أقل من 100 حرف")
    private String dosage;

    @Column(name = "frequency", nullable = false, length = 100)
    @NotBlank(message = "عدد مرات الاستخدام مطلوب")
    @Size(max = 100, message = "عدد مرات الاستخدام يجب أن يكون أقل من 100 حرف")
    private String frequency;

    @Column(name = "duration", nullable = false, length = 100)
    @NotBlank(message = "مدة العلاج مطلوبة")
    @Size(max = 100, message = "مدة العلاج يجب أن تكون أقل من 100 حرف")
    private String duration;

    @Enumerated(EnumType.STRING)
    @Column(name = "route", nullable = false, length = 20)
    @NotNull(message = "طريقة إعطاء الدواء مطلوبة")
    private MedicationRoute route;

    @Column(name = "instructions", length = 500)
    @Size(max = 500, message = "تعليمات الاستخدام يجب أن تكون أقل من 500 حرف")
    private String instructions;

    @Column(name = "quantity")
    @Min(value = 1, message = "الكمية يجب أن تكون أكبر من 0")
    private Integer quantity;

    @Column(name = "refills")
    @Min(value = 0, message = "عدد التجديدات يجب أن يكون 0 أو أكثر")
    @Max(value = 12, message = "عدد التجديدات يجب أن يكون أقل من أو يساوي 12")
    private Integer refills;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "is_prn", nullable = false)
    @Builder.Default
    private Boolean isPrn = false; // As needed

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
