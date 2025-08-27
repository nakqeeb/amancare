// =============================================================================
// Patient Repository - مستودع المرضى
// =============================================================================

package com.nakqeeb.amancare.repository;

import com.nakqeeb.amancare.entity.Clinic;
import com.nakqeeb.amancare.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * مستودع المرضى
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    /**
     * البحث عن المرضى حسب العيادة
     */
    List<Patient> findByClinic(Clinic clinic);

    /**
     * البحث عن المرضى النشطين حسب العيادة
     */
    List<Patient> findByClinicAndIsActiveTrue(Clinic clinic);

    /**
     * البحث عن مريض حسب رقم المريض في العيادة
     */
    Optional<Patient> findByClinicAndPatientNumber(Clinic clinic, String patientNumber);

    /**
     * البحث عن مريض حسب رقم الهاتف في العيادة
     */
    Optional<Patient> findByClinicAndPhone(Clinic clinic, String phone);

    /**
     * البحث في المرضى حسب الاسم
     */
    @Query("SELECT p FROM Patient p WHERE p.clinic = :clinic AND " +
            "(LOWER(p.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
            "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :name, '%')))")
    Page<Patient> findByClinicAndNameContaining(@Param("clinic") Clinic clinic,
                                                @Param("name") String name,
                                                Pageable pageable);

    /**
     * البحث في المرضى حسب رقم الهاتف
     */
    @Query("SELECT p FROM Patient p WHERE p.clinic = :clinic AND p.phone LIKE %:phone%")
    Page<Patient> findByClinicAndPhoneContaining(@Param("clinic") Clinic clinic,
                                                 @Param("phone") String phone,
                                                 Pageable pageable);

    /**
     * البحث الشامل في المرضى
     */
    @Query("SELECT p FROM Patient p WHERE p.clinic = :clinic AND p.isActive = true AND " +
            "(LOWER(p.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "p.phone LIKE CONCAT('%', :search, '%') OR " +
            "p.patientNumber LIKE CONCAT('%', :search, '%'))")
    Page<Patient> searchPatients(@Param("clinic") Clinic clinic,
                                 @Param("search") String search,
                                 Pageable pageable);

    /**
     * المرضى الذين لديهم مواعيد اليوم
     */
    @Query("SELECT DISTINCT p FROM Patient p JOIN p.appointments a WHERE " +
            "p.clinic = :clinic AND a.appointmentDate = :date")
    List<Patient> findPatientsWithAppointmentsOnDate(@Param("clinic") Clinic clinic,
                                                     @Param("date") LocalDate date);

    /**
     * المرضى الجدد (خلال فترة معينة)
     */
    @Query("SELECT p FROM Patient p WHERE p.clinic = :clinic AND p.createdAt >= :since")
    List<Patient> findNewPatientsSince(@Param("clinic") Clinic clinic,
                                       @Param("since") LocalDate since);

    /**
     * المرضى النشطين مع ترقيم الصفحات
     */
    Page<Patient> findByClinicAndIsActiveTrue(Clinic clinic, Pageable pageable);

    /**
     * التحقق من وجود رقم مريض في العيادة
     */
    boolean existsByClinicAndPatientNumber(Clinic clinic, String patientNumber);

    /**
     * إحصائيات المرضى
     */
    @Query("SELECT COUNT(p) FROM Patient p WHERE p.clinic = :clinic AND p.isActive = true")
    long countActivePatientsByClinic(@Param("clinic") Clinic clinic);

    /**
     * المرضى الذين تم إنشاؤهم خلال فترة
     */
    @Query("SELECT COUNT(p) FROM Patient p WHERE p.clinic = :clinic AND " +
            "p.createdAt BETWEEN :startDate AND :endDate")
    long countPatientsCreatedBetween(@Param("clinic") Clinic clinic,
                                     @Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate);
}