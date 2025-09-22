package com.nakqeeb.amancare.dto.response;

import com.nakqeeb.amancare.entity.Payment;
import com.nakqeeb.amancare.entity.PaymentMethod;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PaymentResponse {
    private Long id;
    private Long invoiceId;
    private String invoiceNumber;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private PaymentMethod paymentMethod;
    private String referenceNumber;
    private String notes;
    private String processedBy;
    private String createdAt;

    public static PaymentResponse fromEntity(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());

        if (payment.getInvoice() != null) {
            response.setInvoiceId(payment.getInvoice().getId());
            response.setInvoiceNumber(payment.getInvoice().getInvoiceNumber());
        }

        response.setAmount(payment.getAmount());
        response.setPaymentDate(payment.getPaymentDate());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setReferenceNumber(payment.getReferenceNumber());
        response.setNotes(payment.getNotes());

        if (payment.getCreatedBy() != null) {
            response.setProcessedBy(payment.getCreatedBy().getUsername());
        }

        if (payment.getCreatedAt() != null) {
            response.setCreatedAt(payment.getCreatedAt().toString());
        }

        return response;
    }
}
