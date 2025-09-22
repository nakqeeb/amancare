package com.nakqeeb.amancare.entity.healthrecords;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.nakqeeb.amancare.entity.MedicalRecord;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

// =============================================================================
// MedicalProcedure Entity - كيان الإجراء الطبي
// =============================================================================
@Entity
@Table(name = "medical_record_procedures")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = "medicalRecord")
public class MedicalProcedure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_record_id", nullable = false)
    @JsonBackReference
    private MedicalRecord medicalRecord;

    @Column(name = "procedure_name", nullable = false, length = 200)
    @NotBlank(message = "اسم الإجراء مطلوب")
    @Size(max = 200, message = "اسم الإجراء يجب أن يكون أقل من 200 حرف")
    private String procedureName;

    @Column(name = "procedure_code", length = 50)
    @Size(max = 50, message = "كود الإجراء يجب أن يكون أقل من 50 حرف")
    private String procedureCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    @NotNull(message = "فئة الإجراء مطلوبة")
    private ProcedureCategory category;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "performed_date", nullable = false)
    @NotNull(message = "تاريخ تنفيذ الإجراء مطلوب")
    private LocalDate performedDate;

    @Column(name = "performed_by", length = 100)
    @Size(max = 100, message = "اسم من قام بالإجراء يجب أن يكون أقل من 100 حرف")
    private String performedBy;

    @Column(name = "complications", columnDefinition = "TEXT")
    private String complications;

    @Column(name = "outcome", columnDefinition = "TEXT")
    private String outcome;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
