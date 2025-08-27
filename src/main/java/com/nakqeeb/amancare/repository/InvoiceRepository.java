// =============================================================================
// Invoice Repository - مستودع الفواتير
// =============================================================================

package com.nakqeeb.amancare.repository;

import com.nakqeeb.amancare.entity.Clinic;
import com.nakqeeb.amancare.entity.Invoice;
import com.nakqeeb.amancare.entity.InvoiceStatus;
import com.nakqeeb.amancare.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * مستودع الفواتير
 */
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    /**
     * البحث عن الفواتير حسب العيادة
     */
    List<Invoice> findByClinic(Clinic clinic);

    /**
     * البحث عن الفواتير حسب المريض
     */
    List<Invoice> findByPatientOrderByInvoiceDateDesc(Patient patient);

    /**
     * البحث عن فاتورة حسب رقم الفاتورة في العيادة
     */
    Optional<Invoice> findByClinicAndInvoiceNumber(Clinic clinic, String invoiceNumber);

    /**
     * البحث عن الفواتير حسب الحالة
     */
    List<Invoice> findByClinicAndStatus(Clinic clinic, InvoiceStatus status);

    /**
     * البحث عن الفواتير في فترة زمنية
     */
    @Query("SELECT i FROM Invoice i WHERE i.clinic = :clinic AND " +
            "i.invoiceDate BETWEEN :startDate AND :endDate ORDER BY i.invoiceDate DESC")
    List<Invoice> findByClinicAndDateRange(@Param("clinic") Clinic clinic,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

    /**
     * الفواتير المستحقة الدفع
     */
    @Query("SELECT i FROM Invoice i WHERE i.clinic = :clinic AND " +
            "i.status IN ('SENT', 'OVERDUE') AND i.balanceDue > 0")
    List<Invoice> findOutstandingInvoices(@Param("clinic") Clinic clinic);

    /**
     * الفواتير المتأخرة
     */
    @Query("SELECT i FROM Invoice i WHERE i.clinic = :clinic AND " +
            "i.dueDate < CURRENT_DATE AND i.status != 'PAID' AND i.balanceDue > 0")
    List<Invoice> findOverdueInvoices(@Param("clinic") Clinic clinic);

    /**
     * فواتير اليوم
     */
    List<Invoice> findByClinicAndInvoiceDate(Clinic clinic, LocalDate date);

    /**
     * البحث في الفواتير حسب رقم الفاتورة أو اسم المريض
     */
    @Query("SELECT i FROM Invoice i WHERE i.clinic = :clinic AND " +
            "(i.invoiceNumber LIKE %:search% OR " +
            "LOWER(i.patient.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(i.patient.lastName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Invoice> searchInvoices(@Param("clinic") Clinic clinic,
                                 @Param("search") String search,
                                 Pageable pageable);

    /**
     * الفواتير مع ترقيم الصفحات
     */
    Page<Invoice> findByClinicOrderByInvoiceDateDesc(Clinic clinic, Pageable pageable);

    Page<Invoice> findByClinicAndStatusOrderByInvoiceDateDesc(Clinic clinic, InvoiceStatus status, Pageable pageable);

    /**
     * إحصائيات الفواتير
     */
    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.clinic = :clinic AND i.invoiceDate = :date")
    long countInvoicesByDate(@Param("clinic") Clinic clinic, @Param("date") LocalDate date);

    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.clinic = :clinic AND i.status = :status")
    long countInvoicesByStatus(@Param("clinic") Clinic clinic, @Param("status") InvoiceStatus status);

    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i WHERE i.clinic = :clinic AND " +
            "i.invoiceDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalRevenueByDateRange(@Param("clinic") Clinic clinic,
                                          @Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(i.paidAmount), 0) FROM Invoice i WHERE i.clinic = :clinic AND " +
            "i.invoiceDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalPaidAmountByDateRange(@Param("clinic") Clinic clinic,
                                             @Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(i.balanceDue), 0) FROM Invoice i WHERE i.clinic = :clinic AND " +
            "i.status IN ('SENT', 'PARTIALLY_PAID', 'OVERDUE')")
    BigDecimal getTotalOutstandingAmount(@Param("clinic") Clinic clinic);

    /**
     * التحقق من وجود رقم فاتورة في العيادة
     */
    boolean existsByClinicAndInvoiceNumber(Clinic clinic, String invoiceNumber);

    /**
     * آخر رقم فاتورة تم إنشاؤه في العيادة
     */
    @Query("SELECT i.invoiceNumber FROM Invoice i WHERE i.clinic = :clinic " +
            "ORDER BY i.createdAt DESC LIMIT 1")
    Optional<String> findLastInvoiceNumberByClinic(@Param("clinic") Clinic clinic);

    /**
     * الإيرادات اليومية
     */
    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i WHERE i.clinic = :clinic AND " +
            "i.invoiceDate = :date AND i.status = 'PAID'")
    BigDecimal getDailyRevenue(@Param("clinic") Clinic clinic, @Param("date") LocalDate date);

    /**
     * الإيرادات الشهرية
     */
    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i WHERE i.clinic = :clinic AND " +
            "YEAR(i.invoiceDate) = :year AND MONTH(i.invoiceDate) = :month AND i.status = 'PAID'")
    BigDecimal getMonthlyRevenue(@Param("clinic") Clinic clinic,
                                 @Param("year") int year,
                                 @Param("month") int month);
}