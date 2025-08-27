// =============================================================================
// Clinic Service Repository - مستودع خدمات العيادة
// =============================================================================

package com.nakqeeb.amancare.repository;

import com.nakqeeb.amancare.entity.Clinic;
import com.nakqeeb.amancare.entity.ClinicService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * مستودع خدمات العيادة
 */
@Repository
public interface ClinicServiceRepository extends JpaRepository<ClinicService, Long> {

    /**
     * البحث عن الخدمات حسب العيادة
     */
    List<ClinicService> findByClinic(Clinic clinic);

    /**
     * البحث عن الخدمات النشطة حسب العيادة
     */
    List<ClinicService> findByClinicAndIsActiveTrue(Clinic clinic);

    /**
     * البحث عن خدمة حسب الاسم في العيادة
     */
    Optional<ClinicService> findByClinicAndServiceName(Clinic clinic, String serviceName);

    /**
     * البحث في أسماء الخدمات
     */
    @Query("SELECT cs FROM ClinicService cs WHERE cs.clinic = :clinic AND " +
            "LOWER(cs.serviceName) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<ClinicService> findByClinicAndServiceNameContaining(@Param("clinic") Clinic clinic,
                                                             @Param("name") String name,
                                                             Pageable pageable);

    /**
     * الخدمات النشطة مع ترقيم الصفحات
     */
    Page<ClinicService> findByClinicAndIsActiveTrueOrderByServiceName(Clinic clinic, Pageable pageable);

    /**
     * جميع الخدمات مع ترقيم الصفحات
     */
    Page<ClinicService> findByClinicOrderByServiceName(Clinic clinic, Pageable pageable);

    /**
     * التحقق من وجود خدمة بنفس الاسم في العيادة
     */
    boolean existsByClinicAndServiceNameIgnoreCase(Clinic clinic, String serviceName);

    /**
     * إحصائيات الخدمات
     */
    @Query("SELECT COUNT(cs) FROM ClinicService cs WHERE cs.clinic = :clinic AND cs.isActive = true")
    long countActiveServicesByClinic(@Param("clinic") Clinic clinic);

    /**
     * الخدمات الأكثر استخداماً
     */
    @Query("SELECT cs.serviceName, COUNT(ii) as usage FROM ClinicService cs " +
            "LEFT JOIN InvoiceItem ii ON cs.serviceName = ii.serviceName " +
            "WHERE cs.clinic = :clinic AND cs.isActive = true " +
            "GROUP BY cs.serviceName ORDER BY usage DESC")
    List<Object[]> findMostUsedServices(@Param("clinic") Clinic clinic);
}