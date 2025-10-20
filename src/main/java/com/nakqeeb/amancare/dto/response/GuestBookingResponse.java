// src/main/java/com/nakqeeb/amancare/dto/response/GuestBookingResponse.java

package com.nakqeeb.amancare.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Guest booking response with patient number")
public class GuestBookingResponse {

    @Schema(description = "معرف الموعد")
    private Long appointmentId;

    @Schema(description = "رقم المريض - استخدمه لإدارة مواعيدك")
    private String patientNumber;

    @Schema(description = "اسم المريض الكامل")
    private String patientFullName;

    @Schema(description = "اسم الطبيب")
    private String doctorName;

    @Schema(description = "التخصص")
    private String specialization;

    @Schema(description = "اسم العيادة")
    private String clinicName;

    @Schema(description = "تاريخ الموعد")
    private LocalDate appointmentDate;

    @Schema(description = "وقت الموعد")
    private LocalTime appointmentTime;

    @Schema(description = "رقم الرمز (Token)", example = "5")
    private Integer tokenNumber;

    @Schema(description = "البريد الإلكتروني")
    private String email;

    @Schema(description = "رسالة")
    private String message;
}