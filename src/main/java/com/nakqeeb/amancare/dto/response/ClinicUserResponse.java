package com.nakqeeb.amancare.dto.response;

import com.nakqeeb.amancare.entity.User;
import com.nakqeeb.amancare.entity.UserRole;
import java.time.LocalDateTime;

/**
 * Response DTO for clinic users
 */
public class ClinicUserResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String firstName;
    private String lastName;
    private String phone;
    private UserRole role;
    private String roleArabic;
    private String specialization;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default constructor
    public ClinicUserResponse() {}

    // Static factory method to create from entity
    public static ClinicUserResponse fromEntity(User user) {
        ClinicUserResponse response = new ClinicUserResponse();
        response.id = user.getId();
        response.username = user.getUsername();
        response.email = user.getEmail();
        response.firstName = user.getFirstName();
        response.lastName = user.getLastName();
        response.fullName = user.getFirstName() + " " + user.getLastName();
        response.phone = user.getPhone();
        response.role = user.getRole();
        response.roleArabic = user.getRole().getArabicName();
        response.specialization = user.getSpecialization();
        response.isActive = user.getIsActive();
        response.createdAt = user.getCreatedAt();
        response.updatedAt = user.getUpdatedAt();

        return response;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public String getRoleArabic() { return roleArabic; }
    public void setRoleArabic(String roleArabic) { this.roleArabic = roleArabic; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
