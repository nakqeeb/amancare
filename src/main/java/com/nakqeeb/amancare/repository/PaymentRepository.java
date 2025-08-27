// =============================================================================
// Payment Repository - مستودع المدفوعات
// =============================================================================

package com.nakqeeb.amancare.repository;

import com.nakqeeb.amancare.entity.Clinic;
import com.nakqeeb.amancare.entity.Invoice;
import com.nakqeeb.amancare.entity.Payment;
import com.nakqeeb.amancare.entity.PaymentMethod;
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

/**
 * مستودع المدفوعات
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * البحث عن المدفوعات حسب الفاتورة
     */
    List<Payment> findByInvoiceOrderByPaymentDateDesc(Invoice invoice);

    /**
     * البحث عن المدفوعات حسب المريض
     */
    List<Payment> findByPatientOrderByPaymentDateDesc(Patient patient);

    /**
     * البحث عن المدفوعات حسب العيادة والتاريخ
     */
    List<Payment> findByClinicAndPaymentDate(Clinic clinic, LocalDate date);

    /**
     * البحث عن المدفوعات في فترة زمنية
     */
    @Query("SELECT p FROM Payment p WHERE p.clinic = :clinic AND " +
            "p.paymentDate BETWEEN :startDate AND :endDate ORDER BY p.paymentDate DESC")
    List<Payment> findByClinicAndDateRange(@Param("clinic") Clinic clinic,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

    /**
     * البحث عن المدفوعات حسب طريقة الدفع
     */
    List<Payment> findByClinicAndPaymentMethod(Clinic clinic, PaymentMethod paymentMethod);

    /**
     * مدفوعات اليوم
     */
    @Query("SELECT p FROM Payment p WHERE p.clinic = :clinic AND p.paymentDate = CURRENT_DATE " +
            "ORDER BY p.createdAt DESC")
    List<Payment> findTodayPayments(@Param("clinic") Clinic clinic);

    /**
     * البحث في المدفوعات
     */
    @Query("SELECT p FROM Payment p WHERE p.clinic = :clinic AND " +
            "(p.invoice.invoiceNumber LIKE %:search% OR " +
            "LOWER(p.patient.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.patient.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "p.referenceNumber LIKE %:search%)")
    Page<Payment> searchPayments(@Param("clinic") Clinic clinic,
                                 @Param("search") String search,
                                 Pageable pageable);

    /**
     * المدفوعات مع ترقيم الصفحات
     */
    Page<Payment> findByClinicOrderByPaymentDateDesc(Clinic clinic, Pageable pageable);

    /**
     * إحصائيات المدفوعات
     */
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.clinic = :clinic AND p.paymentDate = :date")
    long countPaymentsByDate(@Param("clinic") Clinic clinic, @Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.clinic = :clinic AND " +
            "p.paymentDate = :date")
    BigDecimal getTotalPaymentsByDate(@Param("clinic") Clinic clinic, @Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.clinic = :clinic AND " +
            "p.paymentDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalPaymentsByDateRange(@Param("clinic") Clinic clinic,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

    @Query("SELECT p.paymentMethod, COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.clinic = :clinic AND " +
            "p.paymentDate BETWEEN :startDate AND :endDate GROUP BY p.paymentMethod")
    List<Object[]> getPaymentsByMethodAndDateRange(@Param("clinic") Clinic clinic,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);

    /**
     * المدفوعات الشهرية
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.clinic = :clinic AND " +
            "YEAR(p.paymentDate) = :year AND MONTH(p.paymentDate) = :month")
    BigDecimal getMonthlyPayments(@Param("clinic") Clinic clinic,
                                  @Param("year") int year,
                                  @Param("month") int month);

    /**
     * المدفوعات اليومية
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.clinic = :clinic AND " +
            "p.paymentDate = CURRENT_DATE")
    BigDecimal getTodayTotalPayments(@Param("clinic") Clinic clinic);
}