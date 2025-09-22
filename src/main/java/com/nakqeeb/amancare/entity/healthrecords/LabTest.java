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
// LabTest Entity - كيان الفحص المخبري
// =============================================================================
@Entity
@Table(name = "medical_record_lab_tests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = "medicalRecord")
public class LabTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_record_id", nullable = false)
    @JsonBackReference
    private MedicalRecord medicalRecord;

    @Column(name = "test_name", nullable = false, length = 200)
    @NotBlank(message = "اسم الفحص مطلوب")
    @Size(max = 200, message = "اسم الفحص يجب أن يكون أقل من 200 حرف")
    private String testName;

    @Column(name = "test_code", length = 50)
    @Size(max = 50, message = "كود الفحص يجب أن يكون أقل من 50 حرف")
    private String testCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    @NotNull(message = "فئة الفحص مطلوبة")
    private LabTestCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "urgency", nullable = false, length = 20)
    @NotNull(message = "أولوية الفحص مطلوبة")
    private TestUrgency urgency;

    @Column(name = "specimen_type", length = 100)
    @Size(max = 100, message = "نوع العينة يجب أن يكون أقل من 100 حرف")
    private String specimenType;

    @Column(name = "instructions", length = 500)
    @Size(max = 500, message = "تعليمات الفحص يجب أن تكون أقل من 500 حرف")
    private String instructions;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @NotNull(message = "حالة الفحص مطلوبة")
    @Builder.Default
    private TestStatus status = TestStatus.ORDERED;

    @Column(name = "ordered_date", nullable = false)
    @NotNull(message = "تاريخ طلب الفحص مطلوب")
    private LocalDate orderedDate;

    @Column(name = "result_date")
    private LocalDate resultDate;

    @Column(name = "results", columnDefinition = "TEXT")
    private String results;

    @Column(name = "normal_range", length = 200)
    @Size(max = 200, message = "المعدل الطبيعي يجب أن يكون أقل من 200 حرف")
    private String normalRange;

    @Column(name = "interpretation", columnDefinition = "TEXT")
    private String interpretation;

    @Column(name = "performed_by", length = 100)
    @Size(max = 100, message = "اسم من قام بالفحص يجب أن يكون أقل من 100 حرف")
    private String performedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
