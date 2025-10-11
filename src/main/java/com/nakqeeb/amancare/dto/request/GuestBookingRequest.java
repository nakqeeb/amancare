// src/main/java/com/nakqeeb/amancare/dto/request/GuestBookingRequest.java

package com.nakqeeb.amancare.dto.request;

import com.nakqeeb.amancare.entity.AppointmentType;
import com.nakqeeb.amancare.entity.BloodType;
import com.nakqeeb.amancare.entity.Gender;
import com.nakqeeb.amancare.validation.ValidPatientName;
import com.nakqeeb.amancare.validation.ValidYemeniPhone;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "Guest appointment booking request")
@Data
public class GuestBookingRequest {

    // Patient Information (same as CreatePatientRequest)
    @Schema(description = "الاسم الأول", required = true)
    @NotBlank(message = "الاسم الأول مطلوب")
    @ValidPatientName(message = "الاسم الأول يجب أن يحتوي على أحرف عربية أو إنجليزية فقط")
    @Size(min = 2, max = 100, message = "الاسم الأول يجب أن يكون بين 2 و 100 حرف")
    private String firstName;

    @Schema(description = "الاسم الأخير", required = true)
    @NotBlank(message = "الاسم الأخير مطلوب")
    @ValidPatientName(message = "الاسم الأخير يجب أن يحتوي على أحرف عربية أو إنجليزية فقط")
    @Size(min = 2, max = 100, message = "الاسم الأخير يجب أن يكون بين 2 و 100 حرف")
    private String lastName;

    @Schema(description = "تاريخ الميلاد")
    @Past(message = "تاريخ الميلاد يجب أن يكون في الماضي")
    private LocalDate dateOfBirth;

    @Schema(description = "الجنس", required = true)
    @NotNull(message = "الجنس مطلوب")
    private Gender gender;

    @Schema(description = "رقم الهاتف", required = true)
    @NotBlank(message = "رقم الهاتف مطلوب")
    @ValidYemeniPhone(message = "رقم الهاتف غير صحيح")
    private String phone;

    @Schema(description = "البريد الإلكتروني", required = true)
    @NotBlank(message = "البريد الإلكتروني مطلوب")
    @Email(message = "البريد الإلكتروني غير صحيح")
    private String email;

    @Schema(description = "العنوان")
    private String address;

    @Schema(description = "اسم جهة الاتصال في حالات الطوارئ")
    private String emergencyContactName;

    @Schema(description = "رقم هاتف جهة الاتصال في حالات الطوارئ")
    @ValidYemeniPhone(message = "رقم الهاتف غير صحيح")
    private String emergencyContactPhone;

    @Schema(description = "فصيلة الدم")
    private BloodType bloodType;

    @Schema(description = "الحساسيات")
    private String allergies;

    @Schema(description = "الأمراض المزمنة")
    private String chronicDiseases;

    @Schema(description = "ملاحظات")
    private String notes;

    // Appointment Information
    @Schema(description = "معرف العيادة", required = true)
    @NotNull(message = "معرف العيادة مطلوب")
    private Long clinicId;

    @Schema(description = "معرف الطبيب", required = true)
    @NotNull(message = "معرف الطبيب مطلوب")
    private Long doctorId;

    @Schema(description = "تاريخ الموعد", required = true)
    @NotNull(message = "تاريخ الموعد مطلوب")
    @Future(message = "تاريخ الموعد يجب أن يكون في المستقبل")
    private LocalDate appointmentDate;

    @Schema(description = "وقت الموعد", required = true)
    @NotNull(message = "وقت الموعد مطلوب")
    private LocalTime appointmentTime;

    @Schema(description = "مدة الموعد بالدقائق")
    private Integer durationMinutes = 30;

    @Schema(description = "نوع الموعد")
    private AppointmentType appointmentType = AppointmentType.CONSULTATION;

    @Schema(description = "الشكوى الرئيسية")
    private String chiefComplaint;

    // Getters and Setters
    // ... (generate all getters and setters)
}