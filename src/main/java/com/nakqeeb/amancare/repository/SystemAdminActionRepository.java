// =============================================================================
// SystemAdminActionRepository - مستودع إجراءات مدير النظام
// =============================================================================

package com.nakqeeb.amancare.repository;

import com.nakqeeb.amancare.entity.SystemAdminAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * مستودع تسجيل إجراءات مدير النظام
 */
@Repository
public interface SystemAdminActionRepository extends JpaRepository<SystemAdminAction, Long> {

    /**
     * البحث عن الإجراءات حسب معرف المسؤول
     */
    Page<SystemAdminAction> findByAdminUserId(Long adminUserId, Pageable pageable);

    /**
     * Find actions by admin user ID and target clinic ID
     */
    Page<SystemAdminAction> findByAdminUserIdAndTargetClinicId(
            Long adminUserId, Long clinicId, Pageable pageable);

    /**
     * البحث عن الإجراءات حسب العيادة المستهدفة
     */
    Page<SystemAdminAction> findByTargetClinicId(Long clinicId, Pageable pageable);

    /**
     * البحث عن الإجراءات في فترة زمنية
     */
    @Query("SELECT a FROM SystemAdminAction a WHERE a.createdAt BETWEEN :startDate AND :endDate")
    Page<SystemAdminAction> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    /**
     * البحث عن الإجراءات حسب النوع
     */
    List<SystemAdminAction> findByActionType(String actionType);

    /**
     * إحصائيات إجراءات المسؤول
     */
    @Query("SELECT a.actionType, COUNT(a) FROM SystemAdminAction a " +
            "WHERE a.adminUserId = :adminUserId " +
            "GROUP BY a.actionType")
    List<Object[]> getActionStatsByAdmin(@Param("adminUserId") Long adminUserId);

    /**
     * آخر الإجراءات للمسؤول
     */
    List<SystemAdminAction> findTop10ByAdminUserIdOrderByCreatedAtDesc(Long adminUserId);

    /**
     * البحث عن إجراءات مورد معين
     */
    List<SystemAdminAction> findByTargetResourceTypeAndTargetResourceId(
            String resourceType,
            Long resourceId
    );
}