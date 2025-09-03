// ===================================================================
// 1. EMAIL VERIFICATION TOKEN ENTITY
// src/main/java/com/nakqeeb/amancare/entity/EmailVerificationToken.java
// ===================================================================
package com.nakqeeb.amancare.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_verification_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Column(nullable = false)
    private boolean used = false;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Constructor for creating new token
    public EmailVerificationToken(String token, String email, Long userId, LocalDateTime expiryDate) {
        this.token = token;
        this.email = email;
        this.userId = userId;
        this.expiryDate = expiryDate;
        this.used = false;
        this.createdAt = LocalDateTime.now();
    }

    // Check if token is expired
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }

    // Check if token is valid (not expired and not used)
    public boolean isValid() {
        return !isExpired() && !used;
    }
}