package com.nakqeeb.amancare.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * طلب تجاوز مدة موعد
 */
@Schema(description = "طلب تجاوز مدة موعد")
public class OverrideDurationRequest {

    @Schema(description = "المدة الجديدة بالدقائق", example = "60", required = true)
    @NotNull(message = "المدة الجديدة مطلوبة")
    @Min(value = 5, message = "المدة يجب أن تكون 5 دقائق على الأقل")
    @Max(value = 240, message = "المدة يجب ألا تتجاوز 240 دقيقة")
    private Integer newDurationMinutes;

    @Schema(description = "سبب التجاوز", example = "استشارة معقدة تتطلب وقت إضافي", required = true)
    @NotNull(message = "سبب التجاوز مطلوب")
    private String reason;

    // Constructors
    public OverrideDurationRequest() {}

    public OverrideDurationRequest(Integer newDurationMinutes, String reason) {
        this.newDurationMinutes = newDurationMinutes;
        this.reason = reason;
    }

    // Getters and Setters
    public Integer getNewDurationMinutes() { return newDurationMinutes; }
    public void setNewDurationMinutes(Integer newDurationMinutes) {
        this.newDurationMinutes = newDurationMinutes;
    }

    public String getReason() { return reason; }
    public void setReason(String reason) {
        this.reason = reason;
    }
}