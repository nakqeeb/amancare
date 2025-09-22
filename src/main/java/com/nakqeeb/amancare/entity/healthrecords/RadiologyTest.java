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
// RadiologyTest Entity - كيان فحص الأشعة
// =============================================================================
@Entity
@Table(name = "medical_record_radiology_tests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = "medicalRecord")
public class RadiologyTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_record_id", nullable = false)
    @JsonBackReference
    private MedicalRecord medicalRecord;

    @Column(name = "test_name", nullable = false, length = 200)
    @NotBlank(message = "اسم فحص الأشعة مطلوب")
    @Size(max = 200, message = "اسم فحص الأشعة يجب أن يكون أقل من 200 حرف")
    private String testName;

    @Enumerated(EnumType.STRING)
    @Column(name = "test_type", nullable = false, length = 30)
    @NotNull(message = "نوع الأشعة مطلوب")
    private RadiologyType testType;

    @Column(name = "body_part", length = 100)
    @Size(max = 100, message = "الجزء المراد فحصه يجب أن يكون أقل من 100 حرف")
    private String bodyPart;

    @Enumerated(EnumType.STRING)
    @Column(name = "urgency", nullable = false, length = 20)
    @NotNull(message = "أولوية الفحص مطلوبة")
    private TestUrgency urgency;

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

    @Column(name = "performed_date")
    private LocalDate performedDate;

    @Column(name = "findings", columnDefinition = "TEXT")
    private String findings;

    @Column(name = "impression", columnDefinition = "TEXT")
    private String impression;

    @Column(name = "radiologist_name", length = 100)
    @Size(max = 100, message = "اسم أخصائي الأشعة يجب أن يكون أقل من 100 حرف")
    private String radiologistName;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
