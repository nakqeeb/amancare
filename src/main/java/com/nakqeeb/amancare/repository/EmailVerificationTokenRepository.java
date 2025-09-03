// ===================================================================
// 2. EMAIL VERIFICATION TOKEN REPOSITORY
// src/main/java/com/nakqeeb/amancare/repository/EmailVerificationTokenRepository.java
// ===================================================================
package com.nakqeeb.amancare.repository;

import com.nakqeeb.amancare.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findByToken(String token);

    Optional<EmailVerificationToken> findByEmailAndUsedFalse(String email);

    Optional<EmailVerificationToken> findByUserIdAndUsedFalse(Long userId);

    @Modifying
    @Query("DELETE FROM EmailVerificationToken e WHERE e.expiryDate < :now")
    void deleteExpiredTokens(LocalDateTime now);

    @Modifying
    @Query("DELETE FROM EmailVerificationToken e WHERE e.email = :email")
    void deleteByEmail(String email);

    @Modifying
    @Query("DELETE FROM EmailVerificationToken e WHERE e.userId = :userId")
    void deleteByUserId(Long userId);
}