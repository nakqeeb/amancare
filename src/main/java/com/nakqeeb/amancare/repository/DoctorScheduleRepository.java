// =============================================================================
// Doctor Schedule Repository - مستودع جدولة الأطباء
// =============================================================================

package com.nakqeeb.amancare.repository;

import com.nakqeeb.amancare.entity.DoctorSchedule;
import com.nakqeeb.amancare.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * مستودع جدولة الأطباء
 */
@Repository
public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, Long> {

    /**
     * البحث عن جداول الطبيب النشطة
     */
    List<DoctorSchedule> findByDoctorAndIsActiveTrueOrderByDayOfWeek(User doctor);

    /**
     * البحث عن جدول الطبيب في يوم معين
     */
    @Query("SELECT ds FROM DoctorSchedule ds WHERE ds.doctor = :doctor AND ds.dayOfWeek = :dayOfWeek " +
            "AND ds.isActive = true AND (ds.effectiveDate IS NULL OR ds.effectiveDate <= :date) " +
            "AND (ds.endDate IS NULL OR ds.endDate >= :date) " +
            "ORDER BY ds.effectiveDate DESC")
    Optional<DoctorSchedule> findDoctorScheduleForDay(@Param("doctor") User doctor,
                                                      @Param("dayOfWeek") DayOfWeek dayOfWeek,
                                                      @Param("date") LocalDate date);

    /**
     * البحث عن جميع الأطباء المتاحين في يوم ووقت معين
     */
    @Query("SELECT DISTINCT ds.doctor FROM DoctorSchedule ds WHERE ds.dayOfWeek = :dayOfWeek " +
            "AND ds.startTime <= :time AND ds.endTime > :time AND ds.isActive = true " +
            "AND ds.doctor.clinic.id = :clinicId " +
            "AND (ds.breakStartTime IS NULL OR :time < ds.breakStartTime OR :time >= ds.breakEndTime) " +
            "AND (ds.effectiveDate IS NULL OR ds.effectiveDate <= :date) " +
            "AND (ds.endDate IS NULL OR ds.endDate >= :date)")
    List<User> findAvailableDoctors(@Param("clinicId") Long clinicId,
                                    @Param("dayOfWeek") DayOfWeek dayOfWeek,
                                    @Param("time") LocalTime time,
                                    @Param("date") LocalDate date);

    /**
     * البحث عن جداول العيادة
     */
    @Query("SELECT ds FROM DoctorSchedule ds WHERE ds.doctor.clinic.id = :clinicId AND ds.isActive = true")
    List<DoctorSchedule> findByClinicId(@Param("clinicId") Long clinicId);

    /**
     * حذف الجداول القديمة للطبيب
     */
    void deleteByDoctorAndDayOfWeekAndIsActiveTrue(User doctor, DayOfWeek dayOfWeek);

    /**
     * Find schedule by ID and doctor
     */
    @Query("SELECT ds FROM DoctorSchedule ds WHERE ds.id = :scheduleId AND ds.doctor.id = :doctorId")
    Optional<DoctorSchedule> findByIdAndDoctorId(@Param("scheduleId") Long scheduleId,
                                                 @Param("doctorId") Long doctorId);

    /**
     * Find all schedules for a doctor on a specific day
     */
    List<DoctorSchedule> findByDoctorAndDayOfWeekAndIsActiveTrue(User doctor, DayOfWeek dayOfWeek);

    /**
     * Find schedules that overlap with given date range
     */
    @Query("SELECT ds FROM DoctorSchedule ds WHERE ds.doctor = :doctor " +
            "AND ds.dayOfWeek = :dayOfWeek " +
            "AND ds.isActive = true " +
            "AND ((ds.effectiveDate IS NULL OR ds.effectiveDate <= :endDate) " +
            "AND (ds.endDate IS NULL OR ds.endDate >= :startDate))")
    List<DoctorSchedule> findOverlappingSchedules(@Param("doctor") User doctor,
                                                  @Param("dayOfWeek") DayOfWeek dayOfWeek,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);

    /**
     * Check if a schedule exists for doctor on specific day with overlapping dates
     */
    @Query("SELECT COUNT(ds) > 0 FROM DoctorSchedule ds WHERE ds.doctor = :doctor " +
            "AND ds.dayOfWeek = :dayOfWeek " +
            "AND ds.isActive = true " +
            "AND ds.id != :excludeId " +
            "AND ((ds.effectiveDate IS NULL OR ds.effectiveDate <= :endDate) " +
            "AND (ds.endDate IS NULL OR ds.endDate >= :startDate))")
    boolean hasOverlappingSchedule(@Param("doctor") User doctor,
                                   @Param("dayOfWeek") DayOfWeek dayOfWeek,
                                   @Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate,
                                   @Param("excludeId") Long excludeId);

    /**
     * Find active appointments for a doctor on a specific day
     */
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.doctor = :doctor " +
            "AND a.appointmentDate = :date " +
            "AND a.status NOT IN ('CANCELLED')")
    long countActiveAppointmentsOnDate(@Param("doctor") User doctor,
                                       @Param("date") LocalDate date);
}
