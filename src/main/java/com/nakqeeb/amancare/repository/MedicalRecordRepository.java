// =============================================================================
// Medical Record Repository - مستودع السجلات الطبية
// src/main/java/com/nakqeeb/amancare/repository/MedicalRecordRepository.java
// =============================================================================

package com.nakqeeb.amancare.repository;

import com.nakqeeb.amancare.entity.MedicalRecord;
import com.nakqeeb.amancare.entity.healthrecords.RecordStatus;
import com.nakqeeb.amancare.entity.healthrecords.VisitType;
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

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    // =============================================================================
    // BASIC QUERIES
    // =============================================================================

    /**
     * Find medical record by ID and clinic (for multi-tenant support)
     */
    Optional<MedicalRecord> findByIdAndClinicId(Long id, Long clinicId);

    /**
     * Find all medical records for a clinic with pagination
     */
    Page<MedicalRecord> findByClinicIdOrderByVisitDateDescCreatedAtDesc(Long clinicId, Pageable pageable);

    /**
     * Find medical records by patient
     */
    Page<MedicalRecord> findByPatientIdAndClinicIdOrderByVisitDateDescCreatedAtDesc(
            Long patientId, Long clinicId, Pageable pageable);

    /**
     * Find medical records by doctor
     */
    Page<MedicalRecord> findByDoctorIdAndClinicIdOrderByVisitDateDescCreatedAtDesc(
            Long doctorId, Long clinicId, Pageable pageable);

    /**
     * Find medical record by appointment
     */
    Optional<MedicalRecord> findByAppointmentIdAndClinicId(Long appointmentId, Long clinicId);

    // =============================================================================
    // STATUS-BASED QUERIES
    // =============================================================================

    /**
     * Find medical records by status
     */
    Page<MedicalRecord> findByStatusAndClinicIdOrderByVisitDateDescCreatedAtDesc(
            RecordStatus status, Long clinicId, Pageable pageable);

    /**
     * Find medical records by patient and status
     */
    Page<MedicalRecord> findByPatientIdAndStatusAndClinicIdOrderByVisitDateDescCreatedAtDesc(
            Long patientId, RecordStatus status, Long clinicId, Pageable pageable);

    /**
     * Find medical records by doctor and status
     */
    Page<MedicalRecord> findByDoctorIdAndStatusAndClinicIdOrderByVisitDateDescCreatedAtDesc(
            Long doctorId, RecordStatus status, Long clinicId, Pageable pageable);

    // =============================================================================
    // DATE-BASED QUERIES
    // =============================================================================

    /**
     * Find medical records by visit date range
     */
    Page<MedicalRecord> findByClinicIdAndVisitDateBetweenOrderByVisitDateDescCreatedAtDesc(
            Long clinicId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    /**
     * Find medical records by patient and date range
     */
    Page<MedicalRecord> findByPatientIdAndClinicIdAndVisitDateBetweenOrderByVisitDateDescCreatedAtDesc(
            Long patientId, Long clinicId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    /**
     * Find medical records for today
     */
    @Query("SELECT mr FROM MedicalRecord mr WHERE mr.clinicId = :clinicId AND DATE(mr.visitDate) = CURRENT_DATE ORDER BY mr.createdAt DESC")
    List<MedicalRecord> findTodayRecords(@Param("clinicId") Long clinicId);

    /**
     * Find medical records for a specific date
     */
    List<MedicalRecord> findByClinicIdAndVisitDateOrderByCreatedAtDesc(Long clinicId, LocalDate visitDate);

    // =============================================================================
    // VISIT TYPE QUERIES
    // =============================================================================

    /**
     * Find medical records by visit type
     */
    Page<MedicalRecord> findByVisitTypeAndClinicIdOrderByVisitDateDescCreatedAtDesc(
            VisitType visitType, Long clinicId, Pageable pageable);

    /**
     * Find medical records by patient and visit type
     */
    Page<MedicalRecord> findByPatientIdAndVisitTypeAndClinicIdOrderByVisitDateDescCreatedAtDesc(
            Long patientId, VisitType visitType, Long clinicId, Pageable pageable);

    // =============================================================================
    // CONFIDENTIAL RECORDS QUERIES
    // =============================================================================

    /**
     * Find confidential medical records
     */
    Page<MedicalRecord> findByIsConfidentialAndClinicIdOrderByVisitDateDescCreatedAtDesc(
            Boolean isConfidential, Long clinicId, Pageable pageable);

    /**
     * Find confidential records for a patient
     */
    Page<MedicalRecord> findByPatientIdAndIsConfidentialAndClinicIdOrderByVisitDateDescCreatedAtDesc(
            Long patientId, Boolean isConfidential, Long clinicId, Pageable pageable);

    // =============================================================================
    // SEARCH QUERIES
    // =============================================================================

    /**
     * Search medical records by chief complaint, diagnosis, or treatment plan
     */
    @Query("SELECT mr FROM MedicalRecord mr " +
            "LEFT JOIN mr.diagnosis d " +
            "WHERE mr.clinicId = :clinicId " +
            "AND (LOWER(mr.chiefComplaint) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(mr.treatmentPlan) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(mr.notes) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(d.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "ORDER BY mr.visitDate DESC, mr.createdAt DESC")
    Page<MedicalRecord> searchMedicalRecords(@Param("clinicId") Long clinicId,
                                             @Param("searchTerm") String searchTerm,
                                             Pageable pageable);

    /**
     * Advanced search with multiple criteria
     */
    @Query("SELECT DISTINCT mr FROM MedicalRecord mr " +
            "LEFT JOIN mr.diagnosis d " +
            "WHERE mr.clinicId = :clinicId " +
            "AND (:patientId IS NULL OR mr.patientId = :patientId) " +
            "AND (:doctorId IS NULL OR mr.doctorId = :doctorId) " +
            "AND (:visitType IS NULL OR mr.visitType = :visitType) " +
            "AND (:status IS NULL OR mr.status = :status) " +
            "AND (:startDate IS NULL OR mr.visitDate >= :startDate) " +
            "AND (:endDate IS NULL OR mr.visitDate <= :endDate) " +
            "AND (:isConfidential IS NULL OR mr.isConfidential = :isConfidential) " +
            "AND (:searchTerm IS NULL OR " +
            "     LOWER(mr.chiefComplaint) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "     LOWER(mr.treatmentPlan) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "     LOWER(mr.notes) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "     LOWER(d.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "ORDER BY mr.visitDate DESC, mr.createdAt DESC")
    Page<MedicalRecord> findBySearchCriteria(
            @Param("clinicId") Long clinicId,
            @Param("patientId") Long patientId,
            @Param("doctorId") Long doctorId,
            @Param("visitType") VisitType visitType,
            @Param("status") RecordStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("isConfidential") Boolean isConfidential,
            @Param("searchTerm") String searchTerm,
            Pageable pageable);

    // =============================================================================
    // STATISTICS QUERIES
    // =============================================================================

    /**
     * Count medical records by clinic
     */
    Long countByClinicId(Long clinicId);

    /**
     * Count medical records by status and clinic
     */
    Long countByStatusAndClinicId(RecordStatus status, Long clinicId);

    /**
     * Count medical records by visit type and clinic
     */
    Long countByVisitTypeAndClinicId(VisitType visitType, Long clinicId);

    /**
     * Count medical records for today
     */
    @Query("SELECT COUNT(mr) FROM MedicalRecord mr WHERE mr.clinicId = :clinicId AND DATE(mr.visitDate) = CURRENT_DATE")
    Long countTodayRecords(@Param("clinicId") Long clinicId);

    /**
     * Count medical records for this week
     */
    @Query("SELECT COUNT(mr) FROM MedicalRecord mr WHERE mr.clinicId = :clinicId AND YEARWEEK(mr.visitDate) = YEARWEEK(CURRENT_DATE)")
    Long countThisWeekRecords(@Param("clinicId") Long clinicId);

    /**
     * Count medical records for this month
     */
    @Query("SELECT COUNT(mr) FROM MedicalRecord mr WHERE mr.clinicId = :clinicId AND YEAR(mr.visitDate) = YEAR(CURRENT_DATE) AND MONTH(mr.visitDate) = MONTH(CURRENT_DATE)")
    Long countThisMonthRecords(@Param("clinicId") Long clinicId);

    /**
     * Count medical records by patient
     */
    Long countByPatientIdAndClinicId(Long patientId, Long clinicId);

    /**
     * Count medical records by doctor
     */
    Long countByDoctorIdAndClinicId(Long doctorId, Long clinicId);

    // =============================================================================
    // RECENT RECORDS QUERIES
    // =============================================================================

    /**
     * Find recent medical records for a patient (last 5)
     */
    @Query("SELECT mr FROM MedicalRecord mr WHERE mr.patientId = :patientId AND mr.clinicId = :clinicId ORDER BY mr.visitDate DESC, mr.createdAt DESC")
    List<MedicalRecord> findTop5ByPatientIdAndClinicIdOrderByVisitDateDescCreatedAtDesc(
            @Param("patientId") Long patientId, @Param("clinicId") Long clinicId, Pageable pageable);

    /**
     * Find recent medical records by doctor (last 10)
     */
    @Query("SELECT mr FROM MedicalRecord mr WHERE mr.doctorId = :doctorId AND mr.clinicId = :clinicId ORDER BY mr.visitDate DESC, mr.createdAt DESC")
    List<MedicalRecord> findTop10ByDoctorIdAndClinicIdOrderByVisitDateDescCreatedAtDesc(
            @Param("doctorId") Long doctorId, @Param("clinicId") Long clinicId, Pageable pageable);

    /**
     * Find records needing follow-up (follow-up date is today or overdue)
     */
    @Query("SELECT mr FROM MedicalRecord mr WHERE mr.clinicId = :clinicId AND mr.followUpDate <= CURRENT_DATE ORDER BY mr.followUpDate ASC")
    List<MedicalRecord> findRecordsNeedingFollowUp(@Param("clinicId") Long clinicId);

    // =============================================================================
    // SYSTEM ADMIN QUERIES (Cross-clinic)
    // =============================================================================

    /**
     * Find all medical records across clinics (SYSTEM_ADMIN only)
     */
    @Query("SELECT mr FROM MedicalRecord mr ORDER BY mr.visitDate DESC, mr.createdAt DESC")
    Page<MedicalRecord> findAllRecordsForSystemAdmin(Pageable pageable);

    /**
     * Search across all clinics (SYSTEM_ADMIN only)
     */
    @Query("SELECT DISTINCT mr FROM MedicalRecord mr " +
            "LEFT JOIN mr.diagnosis d " +
            "WHERE (:clinicId IS NULL OR mr.clinicId = :clinicId) " +
            "AND (:patientId IS NULL OR mr.patientId = :patientId) " +
            "AND (:doctorId IS NULL OR mr.doctorId = :doctorId) " +
            "AND (:visitType IS NULL OR mr.visitType = :visitType) " +
            "AND (:status IS NULL OR mr.status = :status) " +
            "AND (:startDate IS NULL OR mr.visitDate >= :startDate) " +
            "AND (:endDate IS NULL OR mr.visitDate <= :endDate) " +
            "AND (:isConfidential IS NULL OR mr.isConfidential = :isConfidential) " +
            "AND (:searchTerm IS NULL OR " +
            "     LOWER(mr.chiefComplaint) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "     LOWER(mr.treatmentPlan) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "     LOWER(mr.notes) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "     LOWER(d.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "ORDER BY mr.visitDate DESC, mr.createdAt DESC")
    Page<MedicalRecord> findBySearchCriteriaForSystemAdmin(
            @Param("clinicId") Long clinicId,
            @Param("patientId") Long patientId,
            @Param("doctorId") Long doctorId,
            @Param("visitType") VisitType visitType,
            @Param("status") RecordStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("isConfidential") Boolean isConfidential,
            @Param("searchTerm") String searchTerm,
            Pageable pageable);

    /**
     * Global statistics for SYSTEM_ADMIN
     */
    @Query("SELECT COUNT(mr) FROM MedicalRecord mr")
    Long countAllRecords();

    @Query("SELECT COUNT(mr) FROM MedicalRecord mr WHERE mr.status = :status")
    Long countAllRecordsByStatus(@Param("status") RecordStatus status);

    // =============================================================================
    // DIAGNOSIS STATISTICS QUERIES
    // =============================================================================

    /**
     * Get most common diagnoses for a clinic
     */
    @Query("SELECT d.description, d.icdCode, COUNT(d) as count " +
            "FROM Diagnosis d " +
            "JOIN d.medicalRecord mr " +
            "WHERE mr.clinicId = :clinicId " +
            "GROUP BY d.description, d.icdCode " +
            "ORDER BY count DESC")
    List<Object[]> findMostCommonDiagnoses(@Param("clinicId") Long clinicId, Pageable pageable);

    /**
     * Get most prescribed medications for a clinic
     */
    @Query("SELECT p.medicationName, p.genericName, COUNT(p) as count " +
            "FROM Prescription p " +
            "JOIN p.medicalRecord mr " +
            "WHERE mr.clinicId = :clinicId " +
            "GROUP BY p.medicationName, p.genericName " +
            "ORDER BY count DESC")
    List<Object[]> findMostPrescribedMedications(@Param("clinicId") Long clinicId, Pageable pageable);

    /**
     * Get visit type statistics for a clinic
     */
    @Query("SELECT mr.visitType, COUNT(mr) as count " +
            "FROM MedicalRecord mr " +
            "WHERE mr.clinicId = :clinicId " +
            "GROUP BY mr.visitType " +
            "ORDER BY count DESC")
    List<Object[]> findVisitTypeStatistics(@Param("clinicId") Long clinicId);

    // =============================================================================
    // AUDIT AND COMPLIANCE QUERIES
    // =============================================================================

    /**
     * Find records modified in the last N hours
     */
    @Query("SELECT mr FROM MedicalRecord mr " +
            "WHERE mr.clinicId = :clinicId " +
            "AND mr.updatedAt >= :since " +
            "ORDER BY mr.updatedAt DESC")
    List<MedicalRecord> findRecentlyModifiedRecords(@Param("clinicId") Long clinicId,
                                                    @Param("since") LocalDateTime since);

    /**
     * Find locked records that haven't been accessed in a while
     */
    @Query("SELECT mr FROM MedicalRecord mr " +
            "WHERE mr.clinicId = :clinicId " +
            "AND mr.status = 'LOCKED' " +
            "AND mr.updatedAt <= :before " +
            "ORDER BY mr.updatedAt ASC")
    List<MedicalRecord> findOldLockedRecords(@Param("clinicId") Long clinicId,
                                             @Param("before") LocalDateTime before);

    Page<MedicalRecord> findByPatientIdOrderByVisitDateDescCreatedAtDesc(Long patientId, Pageable pageable);

    /**
     * البحث عن السجلات الطبية للطبيب مرتبة حسب التاريخ
     * Find medical records by doctor ID ordered by visit date and creation time
     */
    @Query("SELECT m FROM MedicalRecord m WHERE m.doctor.id = :doctorId " +
            "ORDER BY m.visitDate DESC, m.createdAt DESC")
    Page<MedicalRecord> findByDoctorIdOrderByVisitDateDescCreatedAtDesc(@Param("doctorId") Long doctorId,
                                                                        Pageable pageable);

}