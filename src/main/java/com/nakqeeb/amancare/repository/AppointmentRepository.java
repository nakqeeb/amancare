// =============================================================================
// Appointment Repository - مستودع المواعيد
// =============================================================================

package com.nakqeeb.amancare.repository;

import com.nakqeeb.amancare.entity.Appointment;
import com.nakqeeb.amancare.entity.AppointmentStatus;
import com.nakqeeb.amancare.entity.Clinic;
import com.nakqeeb.amancare.entity.Patient;
import com.nakqeeb.amancare.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * مستودع المواعيد - محدث مع جميع الدوال المطلوبة
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // =============================================================================
    // الدوال الأساسية
    // =============================================================================

    /**
     * البحث عن المواعيد حسب العيادة والتاريخ
     */
    List<Appointment> findByClinicAndAppointmentDate(Clinic clinic, LocalDate date);
    Page<Appointment> findByClinicAndAppointmentDate(Clinic clinic, LocalDate date, Pageable pageable);

    /**
     * البحث عن المواعيد حسب الطبيب والتاريخ
     */
    List<Appointment> findByDoctorAndAppointmentDate(User doctor, LocalDate date);
    Page<Appointment> findByDoctorAndAppointmentDate(User doctor, LocalDate date, Pageable pageable);

    /**
     * البحث عن المواعيد حسب المريض
     */
    List<Appointment> findByPatient(Patient patient);

    /**
     * البحث عن المواعيد حسب العيادة والحالة
     */
    List<Appointment> findByClinicAndStatus(Clinic clinic, AppointmentStatus status);
    Page<Appointment> findByClinicAndStatus(Clinic clinic, AppointmentStatus status, Pageable pageable);

    /**
     * البحث عن المواعيد حسب الطبيب والحالة
     */
    List<Appointment> findByDoctorAndStatus(User doctor, AppointmentStatus status);

    /**
     * البحث عن جميع مواعيد العيادة
     */
    Page<Appointment> findByClinic(Clinic clinic, Pageable pageable);

    /**
     * البحث عن جميع مواعيد الطبيب
     */
    Page<Appointment> findByDoctor(User doctor, Pageable pageable);

    /**
     * البحث عن المواعيد حسب الطبيب والتاريخ والحالة
     */
    List<Appointment> findByDoctorAndAppointmentDateAndStatus(User doctor, LocalDate date, AppointmentStatus status);
    Page<Appointment> findByDoctorAndAppointmentDateAndStatus(User doctor, LocalDate date, AppointmentStatus status, Pageable pageable);

    // =============================================================================
    // الدوال المُصححة - تم تبسيط المنطق
    // =============================================================================

    /**
     * البحث عن المواعيد المحتملة للتعارض (مبسطة)
     * سنتحقق من التعارض في الكود Java بدلاً من SQL المعقد
     */
    @Query("SELECT a FROM Appointment a WHERE a.doctor = :doctor AND a.appointmentDate = :date AND " +
            "a.status NOT IN ('CANCELLED', 'NO_SHOW') AND " +
            "a.appointmentTime BETWEEN :startTime AND :endTime")
    List<Appointment> findPotentialConflictingAppointments(@Param("doctor") User doctor,
                                                           @Param("date") LocalDate date,
                                                           @Param("startTime") LocalTime startTime,
                                                           @Param("endTime") LocalTime endTime);

    /**
     * البحث عن جميع مواعيد الطبيب في تاريخ معين (للتحقق من التعارض في Java)
     */
    @Query("SELECT a FROM Appointment a WHERE a.doctor = :doctor AND a.appointmentDate = :date AND " +
            "a.status NOT IN ('CANCELLED', 'NO_SHOW') ORDER BY a.appointmentTime")
    List<Appointment> findDoctorAppointmentsForConflictCheck(@Param("doctor") User doctor,
                                                             @Param("date") LocalDate date);

    // =============================================================================
    // باقي الدوال (لم تتغير)
    // =============================================================================

    /**
     * مواعيد اليوم للعيادة
     */
    @Query("SELECT a FROM Appointment a WHERE a.clinic = :clinic AND a.appointmentDate = CURRENT_DATE " +
            "ORDER BY a.appointmentTime")
    List<Appointment> findTodayAppointments(@Param("clinic") Clinic clinic);

    /**
     * مواعيد اليوم للطبيب
     */
    @Query("SELECT a FROM Appointment a WHERE a.doctor = :doctor AND a.appointmentDate = CURRENT_DATE " +
            "ORDER BY a.appointmentTime")
    List<Appointment> findTodayAppointmentsByDoctor(@Param("doctor") User doctor);

    /**
     * المواعيد القادمة للمريض
     */
    @Query("SELECT a FROM Appointment a WHERE a.patient = :patient AND " +
            "((a.appointmentDate > CURRENT_DATE) OR " +
            "(a.appointmentDate = CURRENT_DATE AND a.appointmentTime > CURRENT_TIME)) AND " +
            "a.status IN ('SCHEDULED', 'CONFIRMED') " +
            "ORDER BY a.appointmentDate, a.appointmentTime")
    List<Appointment> findUpcomingAppointmentsByPatient(@Param("patient") Patient patient);

    /**
     * إحصائيات المواعيد
     */
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.clinic = :clinic AND a.appointmentDate = :date")
    long countAppointmentsByDate(@Param("clinic") Clinic clinic, @Param("date") LocalDate date);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.clinic = :clinic AND a.status = :status AND " +
            "a.appointmentDate = :date")
    long countAppointmentsByDateAndStatus(@Param("clinic") Clinic clinic,
                                          @Param("date") LocalDate date,
                                          @Param("status") AppointmentStatus status);

    /**
     * المواعيد المتأخرة (لم تتم)
     */
    @Query("SELECT a FROM Appointment a WHERE a.clinic = :clinic AND " +
            "((a.appointmentDate < CURRENT_DATE) OR " +
            "(a.appointmentDate = CURRENT_DATE AND a.appointmentTime < CURRENT_TIME)) AND " +
            "a.status IN ('SCHEDULED', 'CONFIRMED') " +
            "ORDER BY a.appointmentDate DESC, a.appointmentTime DESC")
    List<Appointment> findOverdueAppointments(@Param("clinic") Clinic clinic);

    /**
     * البحث في المواعيد (بالاسم أو رقم المريض)
     */
    @Query("SELECT a FROM Appointment a WHERE a.clinic = :clinic AND " +
            "(LOWER(a.patient.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(a.patient.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "a.patient.patientNumber LIKE CONCAT('%', :search, '%') OR " +
            "a.patient.phone LIKE CONCAT('%', :search, '%'))")
    Page<Appointment> searchAppointments(@Param("clinic") Clinic clinic,
                                         @Param("search") String search,
                                         Pageable pageable);

    /**
     * Count all appointments in a clinic
     */
    long countByClinic(Clinic clinic);

    /**
     * Count today's appointments in a clinic
     */
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.clinic = :clinic " +
            "AND DATE(a.appointmentDate) = :date " +
            "AND a.status NOT IN ('CANCELLED', 'NO_SHOW')")
    long countTodayAppointmentsByClinic(@Param("clinic") Clinic clinic,
                                        @Param("date") LocalDate date);

    /**
     * Count appointments by status in a clinic
     */
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.clinic = :clinic AND a.status = :status")
    long countByClinicAndStatus(@Param("clinic") Clinic clinic,
                                @Param("status") AppointmentStatus status);

    /**
     * Find upcoming appointments in a clinic
     */
    @Query("SELECT a FROM Appointment a WHERE a.clinic = :clinic " +
            "AND a.appointmentDate >= :fromDate " +
            "AND a.status IN ('SCHEDULED', 'CONFIRMED') " +
            "ORDER BY a.appointmentDate ASC")
    List<Appointment> findUpcomingAppointmentsByClinic(@Param("clinic") Clinic clinic,
                                                       @Param("fromDate") LocalDate fromDate);

    /**
     * Count completed appointments in a clinic
     */
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.clinic = :clinic " +
            "AND a.status = 'COMPLETED'")
    long countCompletedAppointmentsByClinic(@Param("clinic") Clinic clinic);

    /**
     * Count cancelled appointments in a clinic
     */
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.clinic = :clinic " +
            "AND a.status IN ('CANCELLED', 'NO_SHOW')")
    long countCancelledAppointmentsByClinic(@Param("clinic") Clinic clinic);

    /**
     * البحث عن موعد حسب المعرف والعيادة
     * Find appointment by ID and clinic ID
     */
    // @Query("SELECT a FROM Appointment a WHERE a.id = :id AND a.clinic.id = :clinicId")
    Optional<Appointment> findByIdAndClinic(Long id, Clinic clinic);
}