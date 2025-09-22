package com.nakqeeb.amancare.entity.healthrecords;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.nakqeeb.amancare.entity.MedicalRecord;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

// =============================================================================
// Diagnosis Entity - كيان التشخيص
// =============================================================================
@Entity
@Table(name = "medical_record_diagnoses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = "medicalRecord")
public class Diagnosis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_record_id", nullable = false)
    @JsonBackReference
    private MedicalRecord medicalRecord;

    @Column(name = "icd_code", length = 20)
    private String icdCode; // ICD-10 code

    @Column(name = "description", nullable = false, length = 500)
    @NotBlank(message = "وصف التشخيص مطلوب")
    @Size(max = 500, message = "وصف التشخيص يجب أن يكون أقل من 500 حرف")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    @NotNull(message = "نوع التشخيص مطلوب")
    private DiagnosisType type;

    @Column(name = "is_primary", nullable = false)
    @NotNull(message = "تحديد التشخيص الأساسي مطلوب")
    private Boolean isPrimary;

    @Column(name = "notes", length = 500)
    @Size(max = 500, message = "ملاحظات التشخيص يجب أن تكون أقل من 500 حرف")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
