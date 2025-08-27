// =============================================================================
// Invoice Item Repository - مستودع عناصر الفواتير
// =============================================================================

package com.nakqeeb.amancare.repository;

import com.nakqeeb.amancare.entity.Invoice;
import com.nakqeeb.amancare.entity.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * مستودع عناصر الفواتير
 */
@Repository
public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, Long> {

    /**
     * البحث عن عناصر الفاتورة
     */
    List<InvoiceItem> findByInvoice(Invoice invoice);

    /**
     * البحث عن العناصر الأكثر شيوعاً في العيادة
     */
    @Query("SELECT ii.serviceName, COUNT(ii) as count FROM InvoiceItem ii " +
            "JOIN ii.invoice i WHERE i.clinic = :clinic " +
            "GROUP BY ii.serviceName ORDER BY count DESC")
    List<Object[]> findMostCommonServicesByClinic(@Param("clinic") Long clinicId);

    /**
     * حذف جميع عناصر الفاتورة
     */
    void deleteByInvoice(Invoice invoice);
}