package com.nakqeeb.amancare.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class InvoiceStatisticsResponse {
    private Long clinicId;
    private Long totalInvoices;
    private Long paidInvoices;
    private Long pendingInvoices;
    private Long overdueInvoices;
    private Long cancelledInvoices;

    private BigDecimal totalRevenue;
    private BigDecimal totalPaid;
    private BigDecimal totalPending;
    private BigDecimal totalOverdue;

    private BigDecimal averageInvoiceValue;
    private BigDecimal collectionRate;

    // Period specific stats
    private BigDecimal todayRevenue;
    private BigDecimal monthlyRevenue;
    private BigDecimal yearlyRevenue;

    private Long todayInvoices;
    private Long monthlyInvoices;
}
