package com.nakqeeb.amancare.dto.request;

import com.nakqeeb.amancare.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateUserRequest {

    @NotBlank(message = "اسم المستخدم مطلوب")
    @Size(min = 3, max = 50, message = "اسم المستخدم يجب أن يكون بين 3 و 50 حرفاً")
    private String username;

    @NotBlank(message = "البريد الإلكتروني مطلوب")
    @Email(message = "صيغة البريد الإلكتروني غير صحيحة")
    private String email;

    @NotBlank(message = "كلمة المرور مطلوبة")
    @Size(min = 6, message = "كلمة المرور يجب أن تكون على الأقل 6 أحرف")
    private String password;

    @NotBlank(message = "الاسم الأول مطلوب")
    @Size(min = 2, max = 50, message = "الاسم الأول يجب أن يكون بين 2 و 50 حرفاً")
    private String firstName;

    @NotBlank(message = "الاسم الأخير مطلوب")
    @Size(min = 2, max = 50, message = "الاسم الأخير يجب أن يكون بين 2 و 50 حرفاً")
    private String lastName;

    @NotBlank(message = "رقم الهاتف مطلوب")
    @Size(min = 10, max = 15, message = "رقم الهاتف يجب أن يكون بين 10 و 15 رقماً")
    private String phone;

    @NotNull(message = "الدور مطلوب")
    private UserRole role;

    private Long clinicId; // Required for non-system admin users

    private String specialization; // For doctors

    private String licenseNumber; // For doctors

    // Constructors
    public CreateUserRequest() {
    }

    public CreateUserRequest(String username, String email, String password,
                             String firstName, String lastName, String phone,
                             UserRole role, Long clinicId) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.role = role;
        this.clinicId = clinicId;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public Long getClinicId() {
        return clinicId;
    }

    public void setClinicId(Long clinicId) {
        this.clinicId = clinicId;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }
}