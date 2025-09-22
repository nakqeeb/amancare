package com.nakqeeb.amancare.dto.response;

import com.nakqeeb.amancare.entity.Clinic;
import com.nakqeeb.amancare.entity.SubscriptionPlan;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ClinicResponse {
    private Long id;
    private String name;
    private String description;
    private String address;
    private String phone;
    private String email;
    private LocalTime workingHoursStart;
    private LocalTime workingHoursEnd;
    private String workingDays;
    private SubscriptionPlan subscriptionPlan;
    private LocalDate subscriptionStartDate;
    private LocalDate subscriptionEndDate;
    private Boolean isActive;
    private String createdAt;
    private String updatedAt;

    // Statistics summary (optional)
    private Long totalUsers;
    private Long totalPatients;
    private Long activePatients;

    /**
     * Convert entity to response DTO
     */
    public static ClinicResponse fromEntity(Clinic clinic) {
        ClinicResponse response = new ClinicResponse();
        response.setId(clinic.getId());
        response.setName(clinic.getName());
        response.setDescription(clinic.getDescription());
        response.setAddress(clinic.getAddress());
        response.setPhone(clinic.getPhone());
        response.setEmail(clinic.getEmail());
        response.setWorkingHoursStart(clinic.getWorkingHoursStart());
        response.setWorkingHoursEnd(clinic.getWorkingHoursEnd());
        response.setWorkingDays(clinic.getWorkingDays());
        response.setSubscriptionPlan(clinic.getSubscriptionPlan());
        response.setSubscriptionStartDate(clinic.getSubscriptionStartDate());
        response.setSubscriptionEndDate(clinic.getSubscriptionEndDate());
        response.setIsActive(clinic.getIsActive());

        if (clinic.getCreatedAt() != null) {
            response.setCreatedAt(clinic.getCreatedAt().toString());
        }
        if (clinic.getUpdatedAt() != null) {
            response.setUpdatedAt(clinic.getUpdatedAt().toString());
        }

        return response;
    }
}