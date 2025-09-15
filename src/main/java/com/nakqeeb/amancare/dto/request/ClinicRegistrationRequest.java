package com.nakqeeb.amancare.dto.request;

import com.nakqeeb.amancare.validation.ValidYemeniPhone;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * طلب تسجيل عيادة جديدة
 */
public class ClinicRegistrationRequest {
    // بيانات العيادة

    @NotBlank(message = "اسم العيادة مطلوب")
    private String clinicName;

    @NotBlank(message = "وصف العيادة مطلوب")
    private String clinicDescription;

    @NotBlank(message = "عنوان العيادة مطلوب")
    private String clinicAddress;

    @NotBlank(message = "رقم الهاتف مطلوب")
    @ValidYemeniPhone(message = "رقم الهاتف يجب أن يكون رقماً يمنياً صحيحاً")
    private String clinicPhone;

    @NotBlank(message = "البريد الإلكتروني للعيادة مطلوب")
    @Email(message = "صيغة البريد الإلكتروني غير صحيحة")
    private String clinicEmail;

    // بيانات مدير العيادة
    @NotBlank(message = "اسم المستخدم لمدير العيادة مطلوب")
    @Size(min = 3, max = 50, message = "اسم المستخدم لمدير العيادة يجب أن يكون بين 3 و 50 حرفاً")
    private String adminUsername;

    @NotBlank(message = "البريد الإلكتروني لمدير العيادة مطلوب")
    @Email(message = "صيغة البريد الإلكتروني غير صحيحة")
    private String adminEmail;

    @NotBlank(message = "كلمة المرور مطلوبة")
    @Size(min = 6, message = "كلمة المرور يجب أن تكون على الأقل 6 أحرف")
    private String adminPassword;

    @NotBlank(message = "الاسم الأول مطلوب")
    @Size(min = 2, max = 50, message = "الاسم الأول يجب أن يكون بين 2 و 50 حرفاً")
    private String adminFirstName;

    @NotBlank(message = "الاسم الأخير مطلوب")
    @Size(min = 2, max = 50, message = "الاسم الأخير يجب أن يكون بين 2 و 50 حرفاً")
    private String adminLastName;

    @NotBlank(message = "رقم الهاتف مطلوب")
    @ValidYemeniPhone(message = "رقم الهاتف يجب أن يكون رقماً يمنياً صحيحاً")
    private String adminPhone;

    // Constructors
    public ClinicRegistrationRequest() {}

    // Getters and Setters
    public String getClinicName() { return clinicName; }
    public void setClinicName(String clinicName) { this.clinicName = clinicName; }

    public String getClinicDescription() { return clinicDescription; }
    public void setClinicDescription(String clinicDescription) { this.clinicDescription = clinicDescription; }

    public String getClinicAddress() { return clinicAddress; }
    public void setClinicAddress(String clinicAddress) { this.clinicAddress = clinicAddress; }

    public String getClinicPhone() { return clinicPhone; }
    public void setClinicPhone(String clinicPhone) { this.clinicPhone = clinicPhone; }

    public String getClinicEmail() { return clinicEmail; }
    public void setClinicEmail(String clinicEmail) { this.clinicEmail = clinicEmail; }

    public String getAdminUsername() { return adminUsername; }
    public void setAdminUsername(String adminUsername) { this.adminUsername = adminUsername; }

    public String getAdminEmail() { return adminEmail; }
    public void setAdminEmail(String adminEmail) { this.adminEmail = adminEmail; }

    public String getAdminPassword() { return adminPassword; }
    public void setAdminPassword(String adminPassword) { this.adminPassword = adminPassword; }

    public String getAdminFirstName() { return adminFirstName; }
    public void setAdminFirstName(String adminFirstName) { this.adminFirstName = adminFirstName; }

    public String getAdminLastName() { return adminLastName; }
    public void setAdminLastName(String adminLastName) { this.adminLastName = adminLastName; }

    public String getAdminPhone() { return adminPhone; }
    public void setAdminPhone(String adminPhone) { this.adminPhone = adminPhone; }
}
