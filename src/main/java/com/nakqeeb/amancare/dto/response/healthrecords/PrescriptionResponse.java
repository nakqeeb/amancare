package com.nakqeeb.amancare.dto.response.healthrecords;

import com.nakqeeb.amancare.entity.healthrecords.MedicationRoute;
import com.nakqeeb.amancare.entity.healthrecords.Prescription;
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
public class PrescriptionResponse {

    private Long id;
    private String medicationName;
    private String genericName;
    private String dosage;
    private String frequency;
    private String duration;
    private MedicationRoute route;
    private String routeArabic;
    private String instructions;
    private Integer quantity;
    private Integer refills;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isPrn;
    private LocalDateTime createdAt;

    public static PrescriptionResponse fromEntity(Prescription prescription) {
        return PrescriptionResponse.builder()
                .id(prescription.getId())
                .medicationName(prescription.getMedicationName())
                .genericName(prescription.getGenericName())
                .dosage(prescription.getDosage())
                .frequency(prescription.getFrequency())
                .duration(prescription.getDuration())
                .route(prescription.getRoute())
                .routeArabic(prescription.getRoute() != null ?
                        prescription.getRoute().getArabicName() : null)
                .instructions(prescription.getInstructions())
                .quantity(prescription.getQuantity())
                .refills(prescription.getRefills())
                .startDate(prescription.getStartDate())
                .endDate(prescription.getEndDate())
                .isPrn(prescription.getIsPrn())
                .createdAt(prescription.getCreatedAt())
                .build();
    }
}
