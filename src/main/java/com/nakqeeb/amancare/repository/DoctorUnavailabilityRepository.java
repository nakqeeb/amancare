// =============================================================================
// Doctor Unavailability Repository - مستودع عدم توفر الأطباء
// =============================================================================

package com.nakqeeb.amancare.repository;

import com.nakqeeb.amancare.entity.DoctorUnavailability;
import com.nakqeeb.amancare.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * مستودع عدم توفر الأطباء
 */
@Repository
public interface DoctorUnavailabilityRepository extends JpaRepository<DoctorUnavailability, Long> {

    /**
     * البحث عن أوقات عدم التوفر للطبيب في تاريخ معين
     */
    List<DoctorUnavailability> findByDoctorAndUnavailableDate(User doctor, LocalDate date);

    /**
     * البحث عن أوقات عدم التوفر في فترة زمنية
     */
    @Query("SELECT du FROM DoctorUnavailability du WHERE du.doctor = :doctor " +
            "AND du.unavailableDate BETWEEN :startDate AND :endDate " +
            "ORDER BY du.unavailableDate, du.startTime")
    List<DoctorUnavailability> findByDoctorAndDateRange(@Param("doctor") User doctor,
                                                        @Param("startDate") LocalDate startDate,
                                                        @Param("endDate") LocalDate endDate);

    /**
     * البحث عن عدم التوفر لجميع أطباء العيادة في تاريخ معين
     */
    @Query("SELECT du FROM DoctorUnavailability du WHERE du.doctor.clinic.id = :clinicId " +
            "AND du.unavailableDate = :date")
    List<DoctorUnavailability> findByClinicAndDate(@Param("clinicId") Long clinicId,
                                                   @Param("date") LocalDate date);
}