package com.nakqeeb.amancare.dto.request;

import com.nakqeeb.amancare.entity.SubscriptionPlan;
import com.nakqeeb.amancare.validation.ValidYemeniPhone;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateClinicRequest {

    @NotBlank(message = "اسم العيادة مطلوب")
    @Size(min = 3, max = 255, message = "اسم العيادة يجب أن يكون بين 3 و 255 حرف")
    private String name;

    @Size(max = 1000, message = "الوصف يجب أن يكون أقل من 1000 حرف")
    private String description;

    @NotBlank(message = "عنوان العيادة مطلوب")
    @Size(max = 500, message = "العنوان يجب أن يكون أقل من 500 حرف")
    private String address;

    @NotBlank(message = "رقم الهاتف مطلوب")
    @ValidYemeniPhone(message = "رقم الهاتف يجب أن يكون رقماً يمنياً صحيحاً")
    private String phone;

    @Email(message = "البريد الإلكتروني غير صحيح")
    private String email;

    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "وقت البداية غير صحيح (HH:MM)")
    private String workingHoursStart = "08:00";

    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "وقت النهاية غير صحيح (HH:MM)")
    private String workingHoursEnd = "18:00";

    private String workingDays = "SUN,MON,TUE,WED,THU";

    private SubscriptionPlan subscriptionPlan = SubscriptionPlan.BASIC;
}