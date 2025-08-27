// =============================================================================
// Clinic Entity - كيان العيادة
// =============================================================================

package com.nakqeeb.amancare.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * كيان العيادة
 */
@Entity
@Table(name = "clinics")
public class Clinic extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "اسم العيادة مطلوب")
    @Size(max = 255, message = "اسم العيادة يجب أن يكون أقل من 255 حرف")
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Size(max = 50, message = "رقم الهاتف يجب أن يكون أقل من 50 رقم")
    @Column(name = "phone", length = 50)
    private String phone;

    @Email(message = "البريد الإلكتروني غير صحيح")
    @Column(name = "email")
    private String email;

    @Column(name = "working_hours_start")
    private LocalTime workingHoursStart = LocalTime.of(8, 0);

    @Column(name = "working_hours_end")
    private LocalTime workingHoursEnd = LocalTime.of(18, 0);

    @Column(name = "working_days", length = 20)
    private String workingDays = "SUN,MON,TUE,WED,THU";

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_plan")
    private SubscriptionPlan subscriptionPlan = SubscriptionPlan.BASIC;

    @Column(name = "subscription_start_date")
    private LocalDate subscriptionStartDate;

    @Column(name = "subscription_end_date")
    private LocalDate subscriptionEndDate;

    @Column(name = "is_active")
    private Boolean isActive = true;

    // العلاقات
    @OneToMany(mappedBy = "clinic", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<User> users;

    @OneToMany(mappedBy = "clinic", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Patient> patients;

    @OneToMany(mappedBy = "clinic", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ClinicService> services;

    // Constructors
    public Clinic() {}

    public Clinic(String name, String description, String address, String phone, String email) {
        this.name = name;
        this.description = description;
        this.address = address;
        this.phone = phone;
        this.email = email;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalTime getWorkingHoursStart() { return workingHoursStart; }
    public void setWorkingHoursStart(LocalTime workingHoursStart) { this.workingHoursStart = workingHoursStart; }

    public LocalTime getWorkingHoursEnd() { return workingHoursEnd; }
    public void setWorkingHoursEnd(LocalTime workingHoursEnd) { this.workingHoursEnd = workingHoursEnd; }

    public String getWorkingDays() { return workingDays; }
    public void setWorkingDays(String workingDays) { this.workingDays = workingDays; }

    public SubscriptionPlan getSubscriptionPlan() { return subscriptionPlan; }
    public void setSubscriptionPlan(SubscriptionPlan subscriptionPlan) { this.subscriptionPlan = subscriptionPlan; }

    public LocalDate getSubscriptionStartDate() { return subscriptionStartDate; }
    public void setSubscriptionStartDate(LocalDate subscriptionStartDate) { this.subscriptionStartDate = subscriptionStartDate; }

    public LocalDate getSubscriptionEndDate() { return subscriptionEndDate; }
    public void setSubscriptionEndDate(LocalDate subscriptionEndDate) { this.subscriptionEndDate = subscriptionEndDate; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public List<User> getUsers() { return users; }
    public void setUsers(List<User> users) { this.users = users; }

    public List<Patient> getPatients() { return patients; }
    public void setPatients(List<Patient> patients) { this.patients = patients; }

    public List<ClinicService> getServices() { return services; }
    public void setServices(List<ClinicService> services) { this.services = services; }
}