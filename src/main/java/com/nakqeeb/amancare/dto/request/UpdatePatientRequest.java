package com.nakqeeb.amancare.dto.request;

import com.nakqeeb.amancare.entity.BloodType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "طلب تحديث بيانات مريض")
public class UpdatePatientRequest {

    @Schema(description = "الاسم الأول", example = "محمد")
    @Size(min = 2, max = 100, message = "الاسم الأول يجب أن يكون بين 2 و 100 حرف")
    private String firstName;

    @Schema(description = "الاسم الأخير", example = "أحمد")
    @Size(min = 2, max = 100, message = "الاسم الأخير يجب أن يكون بين 2 و 100 حرف")
    private String lastName;

    @Schema(description = "تاريخ الميلاد", example = "1990-05-15")
    @Past(message = "تاريخ الميلاد يجب أن يكون في الماضي")
    private LocalDate dateOfBirth;

    @Schema(description = "رقم الهاتف", example = "771234567")
    @Size(max = 50, message = "رقم الهاتف يجب أن يكون أقل من 50 رقم")
    private String phone;

    @Schema(description = "البريد الإلكتروني", example = "patient@example.com")
    @Email(message = "البريد الإلكتروني غير صحيح")
    private String email;

    @Schema(description = "العنوان", example = "حي الصافية، شارع الجامعة، صنعاء")
    private String address;

    @Schema(description = "اسم جهة الاتصال في حالات الطوارئ", example = "فاطمة أحمد")
    private String emergencyContactName;

    @Schema(description = "رقم جهة الاتصال في حالات الطوارئ", example = "773456789")
    private String emergencyContactPhone;

    @Schema(description = "فصيلة الدم", example = "O_POSITIVE")
    private BloodType bloodType;

    @Schema(description = "الحساسيات", example = "حساسية من البنسلين والمكسرات")
    private String allergies;

    @Schema(description = "الأمراض المزمنة", example = "ارتفاع ضغط الدم، السكري")
    private String chronicDiseases;

    @Schema(description = "ملاحظات إضافية", example = "يفضل المواعيد الصباحية")
    private String notes;

    // Constructors
    public UpdatePatientRequest() {}

    // Getters and Setters - نفس الـ getters والـ setters الموجودة أعلاه
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getEmergencyContactName() { return emergencyContactName; }
    public void setEmergencyContactName(String emergencyContactName) { this.emergencyContactName = emergencyContactName; }

    public String getEmergencyContactPhone() { return emergencyContactPhone; }
    public void setEmergencyContactPhone(String emergencyContactPhone) { this.emergencyContactPhone = emergencyContactPhone; }

    public BloodType getBloodType() { return bloodType; }
    public void setBloodType(BloodType bloodType) { this.bloodType = bloodType; }

    public String getAllergies() { return allergies; }
    public void setAllergies(String allergies) { this.allergies = allergies; }

    public String getChronicDiseases() { return chronicDiseases; }
    public void setChronicDiseases(String chronicDiseases) { this.chronicDiseases = chronicDiseases; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}