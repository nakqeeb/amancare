// =============================================================================
// UserFilters DTO
// =============================================================================
package com.nakqeeb.amancare.dto;

import com.nakqeeb.amancare.entity.UserRole;
import java.time.LocalDateTime;

public class UserFilters {

    private String searchTerm;
    private UserRole role;
    private Long clinicId;
    private Boolean isActive;
    private LocalDateTime createdFrom;
    private LocalDateTime createdTo;
    private Boolean hasLoginActivity;
    private String specialization; // For filtering doctors
    private Boolean includeSystemAdmins; // New field for including system admins in results

    // Constructors
    public UserFilters() {
    }

    public UserFilters(String searchTerm, UserRole role, Long clinicId) {
        this.searchTerm = searchTerm;
        this.role = role;
        this.clinicId = clinicId;
    }

    // Getters and Setters
    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
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

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedFrom() {
        return createdFrom;
    }

    public void setCreatedFrom(LocalDateTime createdFrom) {
        this.createdFrom = createdFrom;
    }

    public LocalDateTime getCreatedTo() {
        return createdTo;
    }

    public void setCreatedTo(LocalDateTime createdTo) {
        this.createdTo = createdTo;
    }

    public Boolean getHasLoginActivity() {
        return hasLoginActivity;
    }

    public void setHasLoginActivity(Boolean hasLoginActivity) {
        this.hasLoginActivity = hasLoginActivity;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    // Method to check if system admins should be included
    public Boolean isIncludeSystemAdmins() {
        return includeSystemAdmins;
    }

    public void setIncludeSystemAdmins(Boolean includeSystemAdmins) {
        this.includeSystemAdmins = includeSystemAdmins;
    }
}