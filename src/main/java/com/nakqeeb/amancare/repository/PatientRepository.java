// =============================================================================
// Patient Repository - مستودع المرضى
// =============================================================================

package com.nakqeeb.amancare.repository;

import com.nakqeeb.amancare.entity.BloodType;
import com.nakqeeb.amancare.entity.Clinic;
import com.nakqeeb.amancare.entity.Gender;
import com.nakqeeb.amancare.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
     * Enhanced search with multiple filters including gender, bloodType, and isActive
     * البحث المحسن مع عدة فلاتر
     */
    @Query("SELECT p FROM Patient p WHERE p.clinic = :clinic " +
            "AND (:search IS NULL OR :search = '' OR " +
            "LOWER(p.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "p.phone LIKE CONCAT('%', :search, '%') OR " +
            "p.patientNumber LIKE CONCAT('%', :search, '%')) " +
            "AND (:gender IS NULL OR p.gender = :gender) " +
            "AND (:bloodType IS NULL OR p.bloodType = :bloodType) " +
            "AND (:isActive IS NULL OR p.isActive = :isActive)")
    Page<Patient> searchPatientsWithFilters(@Param("clinic") Clinic clinic,
                                            @Param("search") String search,
                                            @Param("gender") Gender gender,
                                            @Param("bloodType") BloodType bloodType,
                                            @Param("isActive") Boolean isActive,
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

    // ===================================================================
    // ENHANCED STATISTICS QUERIES - FIXED AND COMPLETE
    // ===================================================================

    /**
     * Count ALL patients in clinic (both active and inactive)
     */
    @Query("SELECT COUNT(p) FROM Patient p WHERE p.clinic = :clinic")
    long countAllPatientsByClinic(@Param("clinic") Clinic clinic);

    /**
     * Count only ACTIVE patients in clinic
     */
    @Query("SELECT COUNT(p) FROM Patient p WHERE p.clinic = :clinic AND p.isActive = true")
    long countActivePatientsByClinic(@Param("clinic") Clinic clinic);

    /**
     * Count only INACTIVE patients in clinic
     */
    @Query("SELECT COUNT(p) FROM Patient p WHERE p.clinic = :clinic AND p.isActive = false")
    long countInactivePatientsByClinic(@Param("clinic") Clinic clinic);

    /**
     * Count new patients created between dates
     */
    @Query("SELECT COUNT(p) FROM Patient p WHERE p.clinic = :clinic AND " +
            "p.createdAt >= :startDateTime AND p.createdAt <= :endDateTime")
    long countPatientsCreatedBetween(@Param("clinic") Clinic clinic,
                                     @Param("startDateTime") LocalDateTime startDateTime,
                                     @Param("endDateTime") LocalDateTime endDateTime);

    /**
     * Count MALE patients in clinic
     */
    @Query("SELECT COUNT(p) FROM Patient p WHERE p.clinic = :clinic AND p.gender = 'MALE'")
    long countMalePatientsByClinic(@Param("clinic") Clinic clinic);

    /**
     * Count FEMALE patients in clinic
     */
    @Query("SELECT COUNT(p) FROM Patient p WHERE p.clinic = :clinic AND p.gender = 'FEMALE'")
    long countFemalePatientsByClinic(@Param("clinic") Clinic clinic);

    /**
     * Calculate average age of all patients in clinic
     */
    @Query("SELECT AVG(YEAR(CURRENT_DATE) - YEAR(p.dateOfBirth)) FROM Patient p " +
            "WHERE p.clinic = :clinic AND p.dateOfBirth IS NOT NULL")
    Double calculateAverageAgeByClinic(@Param("clinic") Clinic clinic);

    /**
     * Count patients with appointments today
     */
    @Query("SELECT COUNT(DISTINCT a.patient) FROM Appointment a " +
            "WHERE a.clinic = :clinic " +
            "AND DATE(a.appointmentDate) = CURRENT_DATE " +
            "AND a.status NOT IN ('CANCELLED', 'NO_SHOW')")
    long countPatientsWithAppointmentsToday(@Param("clinic") Clinic clinic);

    /**
     * Count patients with pending (unpaid) invoices
     */
    @Query("SELECT COUNT(DISTINCT i.patient) FROM Invoice i " +
            "WHERE i.patient.clinic = :clinic " +
            "AND i.status IN ('PENDING', 'PARTIALLY_PAID')")
    long countPatientsWithPendingInvoices(@Param("clinic") Clinic clinic);

    /**
     * Calculate total outstanding balance for all patients in clinic
     */
    @Query("SELECT COALESCE(SUM(i.balanceDue), 0) FROM Invoice i " +
            "WHERE i.patient.clinic = :clinic " +
            "AND i.status IN ('PENDING', 'PARTIALLY_PAID')")
    Double calculateTotalOutstandingBalance(@Param("clinic") Clinic clinic);

    /**
     * Count patients by blood type in clinic
     */
    @Query("SELECT p.bloodType, COUNT(p) FROM Patient p " +
            "WHERE p.clinic = :clinic AND p.bloodType IS NOT NULL " +
            "GROUP BY p.bloodType")
    List<Object[]> countPatientsByBloodType(@Param("clinic") Clinic clinic);

    /**
     * Count non-deleted patients in a clinic
     * This is for clinics that use soft delete (isDeleted flag)
     */
    // long countByClinicAndIsDeletedFalse(Clinic clinic);

    // Alternative if you're using isActive instead of isDeleted:
    /**
     * Count active patients in a clinic (already exists as countActivePatientsByClinic)
     * You can use the existing method instead
     */
    long countByClinicAndIsActiveTrue(Clinic clinic);

    /**
     * البحث عن مريض حسب المعرف والعيادة
     * Find patient by ID and clinic ID
     */
    Optional<Patient> findByIdAndClinic(Long id, Clinic clinic);

    Optional<Patient> findByPatientNumber(String patientNumber);

    Optional<Patient> findByPhoneAndClinic(String phone, Clinic clinic);

    Optional<Patient> findByEmailAndClinic(String email, Clinic clinic);
}