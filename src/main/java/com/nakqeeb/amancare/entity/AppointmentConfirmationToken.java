// src/main/java/com/nakqeeb/amancare/entity/AppointmentConfirmationToken.java

package com.nakqeeb.amancare.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointment_confirmation_tokens",
        indexes = {
                @Index(name = "idx_token", columnList = "token"),
                @Index(name = "idx_appointment_id", columnList = "appointment_id")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentConfirmationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String token;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "is_used", nullable = false)
    private Boolean isUsed = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public AppointmentConfirmationToken(String token, Appointment appointment, LocalDateTime expiryDate) {
        this.token = token;
        this.appointment = appointment;
        this.expiryDate = expiryDate;
        this.isUsed = false;
    }

    public boolean isValid() {
        return !isUsed && LocalDateTime.now().isBefore(expiryDate);
    }

    public void markAsUsed() {
        this.isUsed = true;
        this.confirmedAt = LocalDateTime.now();
    }
}