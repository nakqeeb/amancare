// src/main/java/com/nakqeeb/amancare/dto/response/DoctorAvailabilityResponse.java

package com.nakqeeb.amancare.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Doctor availability response")
public class DoctorAvailabilityResponse {

    @Schema(description = "معرف الطبيب")
    private Long doctorId;

    @Schema(description = "اسم الطبيب")
    private String doctorName;

    @Schema(description = "التخصص")
    private String specialization;

    @Schema(description = "معرف العيادة")
    private Long clinicId;

    @Schema(description = "اسم العيادة")
    private String clinicName;

    @Schema(description = "متاح الآن")
    private Boolean availableNow;

    @Schema(description = "متاح حتى")
    private String availableUntil;

    @Schema(description = "الوقت التالي المتاح")
    private String nextAvailableTime;

    @Schema(description = "الصورة الشخصية")
    private String profileImage;
}