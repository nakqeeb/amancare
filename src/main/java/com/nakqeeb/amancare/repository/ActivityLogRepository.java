// =============================================================================
// Activity Log Repository - مستودع سجلات الأنشطة
// src/main/java/com/nakqeeb/amancare/repository/ActivityLogRepository.java
// =============================================================================

package com.nakqeeb.amancare.repository;

import com.nakqeeb.amancare.entity.ActivityLog;
import com.nakqeeb.amancare.entity.ActionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * مستودع سجلات الأنشطة
 * Repository for Activity Logs
 */
@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    // =============================================================================
    // BASIC QUERIES
    // =============================================================================

    /**
     * Find all activities for a specific clinic
     */
    Page<ActivityLog> findByClinicIdOrderByTimestampDesc(Long clinicId, Pageable pageable);

    /**
     * Find recent activities for a clinic (limit)
     */
    List<ActivityLog> findTop50ByClinicIdOrderByTimestampDesc(Long clinicId);

    /**
     * Find activities by user
     */
    Page<ActivityLog> findByClinicIdAndUserIdOrderByTimestampDesc(
            Long clinicId, Long userId, Pageable pageable);

    /**
     * Find activities by action type
     */
    Page<ActivityLog> findByClinicIdAndActionTypeOrderByTimestampDesc(
            Long clinicId, ActionType actionType, Pageable pageable);

    /**
     * Find activities by entity type
     */
    Page<ActivityLog> findByClinicIdAndEntityTypeOrderByTimestampDesc(
            Long clinicId, String entityType, Pageable pageable);

    // =============================================================================
    // DATE RANGE QUERIES
    // =============================================================================

    /**
     * Find activities within a date range
     */
    Page<ActivityLog> findByClinicIdAndTimestampBetweenOrderByTimestampDesc(
            Long clinicId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Find activities after a specific date
     */
    Page<ActivityLog> findByClinicIdAndTimestampAfterOrderByTimestampDesc(
            Long clinicId, LocalDateTime date, Pageable pageable);

    // =============================================================================
    // COMPLEX SEARCH QUERIES
    // =============================================================================

    /**
     * Search activities with multiple filters
     */
    @Query("SELECT a FROM ActivityLog a WHERE a.clinicId = :clinicId " +
            "AND (:userId IS NULL OR a.userId = :userId) " +
            "AND (:actionType IS NULL OR a.actionType = :actionType) " +
            "AND (:entityType IS NULL OR a.entityType = :entityType) " +
            "AND (:startDate IS NULL OR a.timestamp >= :startDate) " +
            "AND (:endDate IS NULL OR a.timestamp <= :endDate) " +
            "AND (:searchTerm IS NULL OR LOWER(a.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "    OR LOWER(a.entityName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "ORDER BY a.timestamp DESC")
    Page<ActivityLog> searchActivities(
            @Param("clinicId") Long clinicId,
            @Param("userId") Long userId,
            @Param("actionType") ActionType actionType,
            @Param("entityType") String entityType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("searchTerm") String searchTerm,
            Pageable pageable);

    // =============================================================================
    // STATISTICS QUERIES
    // =============================================================================

    /**
     * Count activities by clinic
     */
    long countByClinicId(Long clinicId);

    /**
     * Count activities by clinic in date range
     */
    long countByClinicIdAndTimestampBetween(
            Long clinicId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Count activities by action type
     */
    @Query("SELECT a.actionType, COUNT(a) FROM ActivityLog a " +
            "WHERE a.clinicId = :clinicId " +
            "AND a.timestamp >= :since " +
            "GROUP BY a.actionType")
    List<Object[]> countByActionType(
            @Param("clinicId") Long clinicId,
            @Param("since") LocalDateTime since);

    /**
     * Count activities by entity type
     */
    @Query("SELECT a.entityType, COUNT(a) FROM ActivityLog a " +
            "WHERE a.clinicId = :clinicId " +
            "AND a.timestamp >= :since " +
            "GROUP BY a.entityType " +
            "ORDER BY COUNT(a) DESC")
    List<Object[]> countByEntityType(
            @Param("clinicId") Long clinicId,
            @Param("since") LocalDateTime since);

    /**
     * Count activities by user
     */
    @Query("SELECT a.userId, a.userFullName, COUNT(a) FROM ActivityLog a " +
            "WHERE a.clinicId = :clinicId " +
            "AND a.timestamp >= :since " +
            "GROUP BY a.userId, a.userFullName " +
            "ORDER BY COUNT(a) DESC")
    List<Object[]> countByUser(
            @Param("clinicId") Long clinicId,
            @Param("since") LocalDateTime since);

    /**
     * Get daily activity count
     */
    @Query("SELECT CAST(a.timestamp AS date), COUNT(a) FROM ActivityLog a " +
            "WHERE a.clinicId = :clinicId " +
            "AND a.timestamp >= :since " +
            "GROUP BY CAST(a.timestamp AS date) " +
            "ORDER BY CAST(a.timestamp AS date) DESC")
    List<Object[]> getDailyActivityCount(
            @Param("clinicId") Long clinicId,
            @Param("since") LocalDateTime since);

    /**
     * Get most active entities (most frequently modified)
     */
    @Query("SELECT a.entityType, a.entityId, a.entityName, COUNT(a) as activityCount " +
            "FROM ActivityLog a " +
            "WHERE a.clinicId = :clinicId " +
            "AND a.timestamp >= :since " +
            "GROUP BY a.entityType, a.entityId, a.entityName " +
            "ORDER BY activityCount DESC")
    List<Object[]> getMostActiveEntities(
            @Param("clinicId") Long clinicId,
            @Param("since") LocalDateTime since,
            Pageable pageable);

    // =============================================================================
    // ENTITY-SPECIFIC QUERIES
    // =============================================================================

    /**
     * Get activity trail for a specific entity
     */
    @Query("SELECT a FROM ActivityLog a " +
            "WHERE a.clinicId = :clinicId " +
            "AND a.entityType = :entityType " +
            "AND a.entityId = :entityId " +
            "ORDER BY a.timestamp DESC")
    List<ActivityLog> getEntityActivityTrail(
            @Param("clinicId") Long clinicId,
            @Param("entityType") String entityType,
            @Param("entityId") Long entityId);

    // =============================================================================
    // CLEANUP QUERIES
    // =============================================================================

    /**
     * Delete old activity logs (for maintenance)
     */
    @Query("DELETE FROM ActivityLog a WHERE a.timestamp < :before")
    void deleteOldLogs(@Param("before") LocalDateTime before);

    /**
     * Delete all logs for a specific clinic (when clinic is deleted)
     */
    void deleteByClinicId(Long clinicId);
}