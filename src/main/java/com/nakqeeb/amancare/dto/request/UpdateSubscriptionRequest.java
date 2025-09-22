package com.nakqeeb.amancare.dto.request;

import com.nakqeeb.amancare.entity.SubscriptionPlan;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateSubscriptionRequest {

    @NotNull(message = "خطة الاشتراك مطلوبة")
    private SubscriptionPlan subscriptionPlan;

    private String subscriptionStartDate;

    private String subscriptionEndDate;
}