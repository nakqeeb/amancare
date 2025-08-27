// =============================================================================
// Clinic Repository - مستودع العيادات
// =============================================================================

package com.nakqeeb.amancare.repository;

import com.nakqeeb.amancare.entity.Clinic;
import com.nakqeeb.amancare.entity.SubscriptionPlan;
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
 * مستودع العيادات
 */
@Repository
public interface ClinicRepository extends JpaRepository<Clinic, Long> {

    /**
     * البحث عن العيادات النشطة
     */
    List<Clinic> findByIsActiveTrue();

    /**
     * البحث عن العيادات حسب خطة الاشتراك
     */
    List<Clinic> findBySubscriptionPlan(SubscriptionPlan subscriptionPlan);

    /**
     * البحث عن العيادات التي تنتهي اشتراكاتها قريباً
     */
    @Query("SELECT c FROM Clinic c WHERE c.subscriptionEndDate BETWEEN :startDate AND :endDate")
    List<Clinic> findClinicsWithExpiringSoonSubscription(@Param("startDate") LocalDate startDate,
                                                         @Param("endDate") LocalDate endDate);

    /**
     * البحث عن العيادات حسب الاسم (مع دعم البحث الجزئي)
     */
    @Query("SELECT c FROM Clinic c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Clinic> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);

    /**
     * التحقق من وجود عيادة بنفس الاسم
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * العيادات النشطة مع ترقيم الصفحات
     */
    Page<Clinic> findByIsActiveTrue(Pageable pageable);

    /**
     * إحصائيات العيادات
     */
    @Query("SELECT COUNT(c) FROM Clinic c WHERE c.isActive = true")
    long countActiveClinics();

    @Query("SELECT COUNT(c) FROM Clinic c WHERE c.subscriptionPlan = :plan")
    long countBySubscriptionPlan(@Param("plan") SubscriptionPlan plan);
}
