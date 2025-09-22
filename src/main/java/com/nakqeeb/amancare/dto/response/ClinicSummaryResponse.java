package com.nakqeeb.amancare.dto.response;

import com.nakqeeb.amancare.entity.SubscriptionPlan;
import lombok.Data;

/**
 * Lightweight clinic response for lists and dropdowns
 */
@Data
public class ClinicSummaryResponse {
    private Long id;
    private String name;
    private String phone;
    private String email;
    private SubscriptionPlan subscriptionPlan;
    private Boolean isActive;
    private Long patientCount;
    private Long userCount;
}