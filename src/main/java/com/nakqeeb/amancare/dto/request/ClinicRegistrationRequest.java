package com.nakqeeb.amancare.dto.request;

/**
 * طلب تسجيل عيادة جديدة
 */
public class ClinicRegistrationRequest {
    // بيانات العيادة
    private String clinicName;
    private String clinicDescription;
    private String clinicAddress;
    private String clinicPhone;
    private String clinicEmail;

    // بيانات مدير العيادة
    private String adminUsername;
    private String adminEmail;
    private String adminPassword;
    private String adminFirstName;
    private String adminLastName;
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
