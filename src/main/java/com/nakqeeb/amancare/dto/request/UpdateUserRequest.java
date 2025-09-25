package com.nakqeeb.amancare.dto.request;

import com.nakqeeb.amancare.entity.UserRole;
import com.nakqeeb.amancare.validation.ValidYemeniPhone;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * طلب تحديث بيانات المستخدم
 * Update User Request DTO
 */
@Schema(description = "طلب تحديث بيانات المستخدم")
public class UpdateUserRequest {

    @Email(message = "صيغة البريد الإلكتروني غير صحيحة")
    @Schema(description = "البريد الإلكتروني", example = "user@example.com")
    private String email;

    @Size(min = 2, max = 50, message = "الاسم الأول يجب أن يكون بين 2 و 50 حرفاً")
    @Schema(description = "الاسم الأول", example = "أحمد")
    private String firstName;

    @Size(min = 2, max = 50, message = "الاسم الأخير يجب أن يكون بين 2 و 50 حرفاً")
    @Schema(description = "الاسم الأخير", example = "محمد")
    private String lastName;

    // @ValidYemeniPhone(message = "رقم الهاتف يجب أن يكون رقماً يمنياً صحيحاً")
    @Schema(description = "رقم الهاتف", example = "771234567")
    private String phone;

    @Schema(description = "دور المستخدم في النظام", example = "DOCTOR")
    private UserRole role;

    @Schema(description = "التخصص (للأطباء)", example = "طب الأطفال")
    private String specialization;

//    @Schema(description = "رقم الترخيص (للأطباء)", example = "DOC-2024-001")
//    private String licenseNumber;

    @Schema(description = "حالة تفعيل المستخدم", example = "true")
    private Boolean isActive;

    @Size(min = 6, message = "كلمة المرور يجب أن تكون على الأقل 6 أحرف")
    @Schema(description = "كلمة المرور الجديدة (اختيارية)")
    private String newPassword;

    // Constructors
    public UpdateUserRequest() {}

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

//    public String getLicenseNumber() {
//        return licenseNumber;
//    }
//
//    public void setLicenseNumber(String licenseNumber) {
//        this.licenseNumber = licenseNumber;
//    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    // Helper methods
    public boolean hasPasswordChange() {
        return newPassword != null && !newPassword.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "UpdateUserRequest{" +
                "email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", phone='" + phone + '\'' +
                ", role=" + role +
                ", specialization='" + specialization + '\'' +
                //", licenseNumber='" + licenseNumber + '\'' +
                ", isActive=" + isActive +
                ", hasPasswordChange=" + hasPasswordChange() +
                '}';
    }
}