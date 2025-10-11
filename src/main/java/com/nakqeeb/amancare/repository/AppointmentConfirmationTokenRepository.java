// src/main/java/com/nakqeeb/amancare/repository/AppointmentConfirmationTokenRepository.java

package com.nakqeeb.amancare.repository;

import com.nakqeeb.amancare.entity.AppointmentConfirmationToken;
import com.nakqeeb.amancare.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

@Repository
public interface AppointmentConfirmationTokenRepository extends JpaRepository<AppointmentConfirmationToken, Long> {

    Optional<AppointmentConfirmationToken> findByToken(String token);

    Optional<AppointmentConfirmationToken> findByAppointment(Appointment appointment);

    void deleteByAppointment(Appointment appointment);

    // Clean up expired tokens (for scheduled job)
    void deleteByExpiryDateBefore(LocalDateTime dateTime);
}