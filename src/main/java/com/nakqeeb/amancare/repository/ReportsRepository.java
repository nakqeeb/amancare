// =============================================================================
// Custom Repository للاستعلامات المعقدة
// =============================================================================

package com.nakqeeb.amancare.repository;

import com.nakqeeb.amancare.entity.Clinic;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * مستودع الاستعلامات المعقدة والتقارير
 */
@Repository
public interface ReportsRepository {

    /**
     * تقرير الإيرادات اليومية
     */
    @Query(value = "SELECT " +
            "DATE(i.invoice_date) as date, " +
            "COUNT(i.id) as invoice_count, " +
            "COALESCE(SUM(i.total_amount), 0) as total_revenue, " +
            "COALESCE(SUM(i.paid_amount), 0) as paid_amount " +
            "FROM invoices i WHERE i.clinic_id = :clinicId AND " +
            "i.invoice_date BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE(i.invoice_date) ORDER BY date",
            nativeQuery = true)
    List<Object[]> getDailyRevenueReport(@Param("clinicId") Long clinicId,
                                         @Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);

    /**
     * تقرير المواعيد الشهرية
     */
    @Query(value = "SELECT " +
            "DATE(a.appointment_date) as date, " +
            "COUNT(a.id) as appointment_count, " +
            "COUNT(CASE WHEN a.status = 'COMPLETED' THEN 1 END) as completed_count, " +
            "COUNT(CASE WHEN a.status = 'CANCELLED' THEN 1 END) as cancelled_count, " +
            "COUNT(CASE WHEN a.status = 'NO_SHOW' THEN 1 END) as no_show_count " +
            "FROM appointments a WHERE a.clinic_id = :clinicId AND " +
            "a.appointment_date BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE(a.appointment_date) ORDER BY date",
            nativeQuery = true)
    List<Object[]> getAppointmentReport(@Param("clinicId") Long clinicId,
                                        @Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate);

    /**
     * تقرير أداء الأطباء
     */
    @Query(value = "SELECT " +
            "CONCAT(u.first_name, ' ', u.last_name) as doctor_name, " +
            "COUNT(a.id) as appointment_count, " +
            "COUNT(CASE WHEN a.status = 'COMPLETED' THEN 1 END) as completed_count, " +
            "COALESCE(SUM(i.total_amount), 0) as revenue " +
            "FROM users u " +
            "LEFT JOIN appointments a ON u.id = a.doctor_id AND a.appointment_date BETWEEN :startDate AND :endDate " +
            "LEFT JOIN invoices i ON a.id = i.appointment_id " +
            "WHERE u.clinic_id = :clinicId AND u.role = 'DOCTOR' AND u.is_active = true " +
            "GROUP BY u.id ORDER BY appointment_count DESC",
            nativeQuery = true)
    List<Object[]> getDoctorPerformanceReport(@Param("clinicId") Long clinicId,
                                              @Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);

    /**
     * تقرير المرضى الجدد
     */
    @Query(value = "SELECT " +
            "DATE(p.created_at) as date, " +
            "COUNT(p.id) as new_patients_count " +
            "FROM patients p WHERE p.clinic_id = :clinicId AND " +
            "DATE(p.created_at) BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE(p.created_at) ORDER BY date",
            nativeQuery = true)
    List<Object[]> getNewPatientsReport(@Param("clinicId") Long clinicId,
                                        @Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate);

    /**
     * تقرير طرق الدفع
     */
    @Query(value = "SELECT " +
            "p.payment_method, " +
            "COUNT(p.id) as payment_count, " +
            "COALESCE(SUM(p.amount), 0) as total_amount " +
            "FROM payments p WHERE p.clinic_id = :clinicId AND " +
            "p.payment_date BETWEEN :startDate AND :endDate " +
            "GROUP BY p.payment_method ORDER BY total_amount DESC",
            nativeQuery = true)
    List<Object[]> getPaymentMethodReport(@Param("clinicId") Long clinicId,
                                          @Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);
}