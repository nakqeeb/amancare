package com.nakqeeb.amancare.dto.response;

import com.nakqeeb.amancare.entity.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class InvoiceResponse {
    private Long id;
    private String invoiceNumber;
    private Long patientId;
    private String patientName;
    private String patientPhone;
    private Long appointmentId;
    private Long clinicId;
    private String clinicName;
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private InvoiceStatus status;
    private PaymentStatus paymentStatus;

    // Financial details
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal balanceDue;

    // Items and payments
    private List<InvoiceItemResponse> items = new ArrayList<>();
    private List<PaymentResponse> payments = new ArrayList<>();

    // Additional info
    private String notes;
    private String terms;
    private String createdBy;
    private String createdAt;
    private String updatedAt;

    // Computed fields
    private boolean isOverdue;
    private int daysOverdue;

    /**
     * Convert entity to response DTO
     */
    public static InvoiceResponse fromEntity(Invoice invoice) {
        InvoiceResponse response = new InvoiceResponse();

        // Basic info
        response.setId(invoice.getId());
        response.setInvoiceNumber(invoice.getInvoiceNumber());
        response.setInvoiceDate(invoice.getInvoiceDate());
        response.setDueDate(invoice.getDueDate());
        response.setStatus(invoice.getStatus());

        // Patient info
        if (invoice.getPatient() != null) {
            response.setPatientId(invoice.getPatient().getId());
            response.setPatientName(invoice.getPatient().getFirstName() + " " + invoice.getPatient().getLastName());
            response.setPatientPhone(invoice.getPatient().getPhone());
        }

        // Appointment info
        if (invoice.getAppointment() != null) {
            response.setAppointmentId(invoice.getAppointment().getId());
        }

        // Clinic info
        if (invoice.getClinic() != null) {
            response.setClinicId(invoice.getClinic().getId());
            response.setClinicName(invoice.getClinic().getName());
        }

        // Financial details
        response.setSubtotal(invoice.getSubtotal());
        response.setTaxAmount(invoice.getTaxAmount());
        response.setDiscountAmount(invoice.getDiscountAmount());
        response.setTotalAmount(invoice.getTotalAmount());
        response.setPaidAmount(invoice.getPaidAmount());
        response.setBalanceDue(invoice.getBalanceDue());

        // Payment status
        if (invoice.getBalanceDue().compareTo(BigDecimal.ZERO) == 0) {
            response.setPaymentStatus(PaymentStatus.COMPLETED);
        } else if (invoice.getPaidAmount().compareTo(BigDecimal.ZERO) > 0) {
            response.setPaymentStatus(PaymentStatus.PENDING);
        } else {
            response.setPaymentStatus(PaymentStatus.PENDING);
        }

        // Items
        if (invoice.getItems() != null) {
            response.setItems(invoice.getItems().stream()
                    .map(InvoiceItemResponse::fromEntity)
                    .collect(Collectors.toList()));
        }

        // Payments
        if (invoice.getPayments() != null) {
            response.setPayments(invoice.getPayments().stream()
                    .map(PaymentResponse::fromEntity)
                    .collect(Collectors.toList()));
        }

        // Additional info
        response.setNotes(invoice.getNotes());

        // Created by
        if (invoice.getCreatedBy() != null) {
            response.setCreatedBy(invoice.getCreatedBy().getUsername());
        }

        // Timestamps
        if (invoice.getCreatedAt() != null) {
            response.setCreatedAt(invoice.getCreatedAt().toString());
        }
        if (invoice.getUpdatedAt() != null) {
            response.setUpdatedAt(invoice.getUpdatedAt().toString());
        }

        // Calculate overdue
        if (invoice.getDueDate() != null && LocalDate.now().isAfter(invoice.getDueDate())
                && invoice.getBalanceDue().compareTo(BigDecimal.ZERO) > 0) {
            response.setOverdue(true);
            response.setDaysOverdue((int) java.time.temporal.ChronoUnit.DAYS.between(invoice.getDueDate(), LocalDate.now()));
        }

        return response;
    }
}