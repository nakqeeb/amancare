// =============================================================================
// User Entity - كيان المستخدم
// =============================================================================

package com.nakqeeb.amancare.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * كيان المستخدم
 */
@Entity
@Table(name = "users",
        indexes = {
                @Index(name = "idx_clinic_role", columnList = "clinic_id, role")
        })
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinic_id", nullable = false)
    private Clinic clinic;

    @NotBlank(message = "اسم المستخدم مطلوب")
    @Size(min = 3, max = 100, message = "اسم المستخدم يجب أن يكون بين 3 و 100 حرف")
    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @NotBlank(message = "البريد الإلكتروني مطلوب")
    @Email(message = "البريد الإلكتروني غير صحيح")
    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @NotBlank(message = "كلمة المرور مطلوبة")
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @NotBlank(message = "الاسم الأول مطلوب")
    @Size(max = 100, message = "الاسم الأول يجب أن يكون أقل من 100 حرف")
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotBlank(message = "الاسم الأخير مطلوب")
    @Size(max = 100, message = "الاسم الأخير يجب أن يكون أقل من 100 حرف")
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Size(max = 50, message = "رقم الهاتف يجب أن يكون أقل من 50 رقم")
    @Column(name = "phone", length = 50)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

    @Column(name = "specialization")
    private String specialization;

    @Column(name = "is_active")
    private Boolean isActive = false;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    // Constructors
    public User() {}

    public User(Clinic clinic, String username, String email, String passwordHash,
                String firstName, String lastName, UserRole role) {
        this.clinic = clinic;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Clinic getClinic() { return clinic; }
    public void setClinic(Clinic clinic) { this.clinic = clinic; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getFullName() { return firstName + " " + lastName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
}