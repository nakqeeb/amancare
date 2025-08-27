// =============================================================================
// Medical Record Repository - مستودع السجلات الطبية
// =============================================================================

package com.nakqeeb.amancare.repository;

import com.nakqeeb.amancare.entity.Clinic;
import com.nakqeeb.amancare.entity.MedicalRecord;
import com.nakqeeb.amancare.entity.Patient;
import com.nakqeeb.amancare.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * مستودع السجلات الطبية
 */
@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    /**
     * البحث عن السجلات الطبية للمريض
     */
    List<MedicalRecord> findByPatientOrderByVisitDateDesc(Patient patient);

    /**
     * البحث عن السجلات الطبية للطبيب
     */
    List<MedicalRecord> findByDoctorAndVisitDate(User doctor, LocalDate visitDate);

    /**
     * البحث عن السجلات الطبية للعيادة في تاريخ معين
     */
    List<MedicalRecord> findByClinicAndVisitDate(Clinic clinic, LocalDate visitDate);

    /**
     * البحث عن السجلات الطبية في فترة زمنية
     */
    @Query("SELECT m FROM MedicalRecord m WHERE m.clinic = :clinic AND " +
            "m.visitDate BETWEEN :startDate AND :endDate ORDER BY m.visitDate DESC")
    List<MedicalRecord> findByClinicAndDateRange(@Param("clinic") Clinic clinic,
                                                 @Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate);

    /**
     * آخر سجل طبي للمريض
     */
    @Query("SELECT m FROM MedicalRecord m WHERE m.patient = :patient " +
            "ORDER BY m.visitDate DESC, m.createdAt DESC")
    Page<MedicalRecord> findLastRecordByPatient(@Param("patient") Patient patient, Pageable pageable);

    /**
     * البحث في التشخيصات
     */
    @Query("SELECT m FROM MedicalRecord m WHERE m.clinic = :clinic AND " +
            "LOWER(m.diagnosis) LIKE LOWER(CONCAT('%', :diagnosis, '%'))")
    List<MedicalRecord> findByDiagnosisContaining(@Param("clinic") Clinic clinic,
                                                  @Param("diagnosis") String diagnosis);

    /**
     * السجلات التي تحتاج متابعة
     */
    @Query("SELECT m FROM MedicalRecord m WHERE m.clinic = :clinic AND " +
            "m.nextVisitDate IS NOT NULL AND m.nextVisitDate <= :date")
    List<MedicalRecord> findRecordsNeedingFollowUp(@Param("clinic") Clinic clinic,
                                                   @Param("date") LocalDate date);

    /**
     * إحصائيات السجلات الطبية
     */
    @Query("SELECT COUNT(m) FROM MedicalRecord m WHERE m.clinic = :clinic AND m.visitDate = :date")
    long countRecordsByDate(@Param("clinic") Clinic clinic, @Param("date") LocalDate date);

    /**
     * السجلات الطبية مع ترقيم الصفحات
     */
    Page<MedicalRecord> findByClinicOrderByVisitDateDesc(Clinic clinic, Pageable pageable);

    Page<MedicalRecord> findByPatientOrderByVisitDateDesc(Patient patient, Pageable pageable);
}