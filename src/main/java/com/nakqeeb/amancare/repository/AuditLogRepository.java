package com.nakqeeb.amancare.repository;

import com.nakqeeb.amancare.entity.AuditLog;
import com.nakqeeb.amancare.entity.AuditLog.AuditSeverity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // Find by user
    Page<AuditLog> findByUserId(Long userId, Pageable pageable);

    // Find by clinic
    Page<AuditLog> findByClinicId(Long clinicId, Pageable pageable);

    // Find by entity
    Page<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId, Pageable pageable);

    // Find by action
    Page<AuditLog> findByAction(String action, Pageable pageable);

    // Find by severity
    Page<AuditLog> findBySeverity(AuditSeverity severity, Pageable pageable);

    // Find by date range
    Page<AuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    // Find critical actions
    @Query("SELECT a FROM AuditLog a WHERE a.severity = 'CRITICAL' " +
            "AND a.clinicId = :clinicId " +
            "ORDER BY a.timestamp DESC")
    List<AuditLog> findCriticalActionsByClinic(@Param("clinicId") Long clinicId, Pageable pageable);

    // Find permanent deletions
    @Query("SELECT a FROM AuditLog a WHERE a.action LIKE 'PERMANENT_DELETE_%' " +
            "AND a.clinicId = :clinicId " +
            "ORDER BY a.timestamp DESC")
    List<AuditLog> findPermanentDeletionsByClinic(@Param("clinicId") Long clinicId);

    // Find failed operations
    @Query("SELECT a FROM AuditLog a WHERE a.success = false " +
            "AND a.clinicId = :clinicId " +
            "AND a.timestamp >= :since " +
            "ORDER BY a.timestamp DESC")
    List<AuditLog> findFailedOperations(
            @Param("clinicId") Long clinicId,
            @Param("since") LocalDateTime since);

    // Count actions by type for a clinic
    @Query("SELECT a.action, COUNT(a) FROM AuditLog a " +
            "WHERE a.clinicId = :clinicId " +
            "AND a.timestamp >= :since " +
            "GROUP BY a.action")
    List<Object[]> countActionsByType(
            @Param("clinicId") Long clinicId,
            @Param("since") LocalDateTime since);

    // Find user activity
    @Query("SELECT a FROM AuditLog a WHERE a.userId = :userId " +
            "AND a.clinicId = :clinicId " +
            "AND a.timestamp >= :since " +
            "ORDER BY a.timestamp DESC")
    Page<AuditLog> findUserActivity(
            @Param("userId") Long userId,
            @Param("clinicId") Long clinicId,
            @Param("since") LocalDateTime since,
            Pageable pageable);

    // Search logs
    @Query("SELECT a FROM AuditLog a WHERE " +
            "(a.clinicId = :clinicId) AND " +
            "(:action IS NULL OR a.action = :action) AND " +
            "(:entityType IS NULL OR a.entityType = :entityType) AND " +
            "(:userId IS NULL OR a.userId = :userId) AND " +
            "(:severity IS NULL OR a.severity = :severity) AND " +
            "(:startDate IS NULL OR a.timestamp >= :startDate) AND " +
            "(:endDate IS NULL OR a.timestamp <= :endDate) " +
            "ORDER BY a.timestamp DESC")
    Page<AuditLog> searchLogs(
            @Param("clinicId") Long clinicId,
            @Param("action") String action,
            @Param("entityType") String entityType,
            @Param("userId") Long userId,
            @Param("severity") AuditSeverity severity,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    // Cleanup old logs (for maintenance)
    @Query("DELETE FROM AuditLog a WHERE a.timestamp < :before " +
            "AND a.severity != 'CRITICAL'")
    void deleteOldLogs(@Param("before") LocalDateTime before);

    // Get audit summary for dashboard
    @Query("SELECT " +
            "COUNT(a) as totalActions, " +
            "COUNT(CASE WHEN a.severity = 'CRITICAL' THEN 1 END) as criticalActions, " +
            "COUNT(CASE WHEN a.success = false THEN 1 END) as failedActions, " +
            "COUNT(DISTINCT a.userId) as uniqueUsers " +
            "FROM AuditLog a " +
            "WHERE a.clinicId = :clinicId " +
            "AND a.timestamp >= :since")
    Object getAuditSummary(
            @Param("clinicId") Long clinicId,
            @Param("since") LocalDateTime since);
}