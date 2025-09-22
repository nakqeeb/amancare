package com.nakqeeb.amancare.dto.request;

import com.nakqeeb.amancare.entity.InvoiceStatus;
import com.nakqeeb.amancare.entity.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class InvoiceSearchCriteria {
    private Long patientId;
    private Long doctorId;
    private Long clinicId;
    private InvoiceStatus status;
    private PaymentStatus paymentStatus;
    private LocalDate fromDate;
    private LocalDate toDate;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private String searchQuery;
    private String sortBy = "invoiceDate";
    private String sortDirection = "DESC";
}
