// =============================================================================
// Clinic Service Entity - كيان خدمات العيادة
// =============================================================================

package com.nakqeeb.amancare.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * كيان خدمات العيادة
 */
@Entity
@Table(name = "clinic_services",
        indexes = {
                @Index(name = "idx_clinic_active", columnList = "clinic_id, is_active")
        })
public class ClinicService extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinic_id", nullable = false)
    private Clinic clinic;

    @NotBlank(message = "اسم الخدمة مطلوب")
    @Size(max = 255, message = "اسم الخدمة يجب أن يكون أقل من 255 حرف")
    @Column(name = "service_name", nullable = false)
    private String serviceName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "السعر مطلوب")
    @DecimalMin(value = "0.00", message = "السعر يجب أن يكون أكبر من أو يساوي صفر")
    @Column(name = "price", precision = 10, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(name = "duration_minutes")
    private Integer durationMinutes = 30;

    @Column(name = "is_active")
    private Boolean isActive = true;

    // Constructors
    public ClinicService() {}

    public ClinicService(Clinic clinic, String serviceName, BigDecimal price) {
        this.clinic = clinic;
        this.serviceName = serviceName;
        this.price = price;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Clinic getClinic() { return clinic; }
    public void setClinic(Clinic clinic) { this.clinic = clinic; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}