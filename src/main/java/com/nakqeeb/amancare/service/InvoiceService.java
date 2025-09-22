// =============================================================================
// Invoice Service - خدمة إدارة الفواتير
// src/main/java/com/nakqeeb/amancare/service/InvoiceService.java
// =============================================================================

package com.nakqeeb.amancare.service;

import com.nakqeeb.amancare.dto.request.CreateInvoiceRequest;
import com.nakqeeb.amancare.dto.request.UpdateInvoiceRequest;
import com.nakqeeb.amancare.dto.request.CreatePaymentRequest;
import com.nakqeeb.amancare.dto.request.InvoiceSearchCriteria;
import com.nakqeeb.amancare.dto.response.*;
import com.nakqeeb.amancare.entity.*;
import com.nakqeeb.amancare.exception.BadRequestException;
import com.nakqeeb.amancare.exception.ForbiddenOperationException;
import com.nakqeeb.amancare.exception.ResourceNotFoundException;
import com.nakqeeb.amancare.repository.*;
import com.nakqeeb.amancare.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * خدمة إدارة الفواتير والمدفوعات
 * Service for managing invoices and payments
 */
@Service
@Transactional
public class InvoiceService {
    private static final Logger logger = LoggerFactory.getLogger(InvoiceService.class);

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private InvoiceItemRepository invoiceItemRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private ClinicRepository clinicRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClinicContextService clinicContextService;

    @Autowired
    private AuditLogService auditLogService;

    // ===================================================================
    // INVOICE OPERATIONS
    // ===================================================================

    /**
     * Create a new invoice
     */
    public InvoiceResponse createInvoice(CreateInvoiceRequest request, UserPrincipal currentUser) {
        logger.info("Creating new invoice for patient: {} by user: {}", request.getPatientId(), currentUser.getId());

        // Get effective clinic ID
        Long effectiveClinicId = clinicContextService.getEffectiveClinicId(currentUser);

        Clinic clinic = clinicRepository.findById(effectiveClinicId)
                .orElseThrow(() -> new ResourceNotFoundException("العيادة غير موجودة"));

        // Validate patient
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("المريض غير موجود"));

        // Ensure patient belongs to the clinic
        if (!patient.getClinic().getId().equals(effectiveClinicId)) {
            throw new ForbiddenOperationException("المريض لا ينتمي لهذه العيادة");
        }

        // Get current user as creator
        User createdBy = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("المستخدم غير موجود"));

        // Create invoice
        Invoice invoice = new Invoice();
        invoice.setClinic(clinic);
        invoice.setPatient(patient);
        invoice.setInvoiceNumber(generateInvoiceNumber(clinic));
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setDueDate(request.getDueDate());
        invoice.setStatus(InvoiceStatus.DRAFT);
        invoice.setPaymentMethod(PaymentMethod.CASH);
        invoice.setNotes(request.getNotes());
        invoice.setCreatedBy(createdBy);

        // Set appointment if provided
        if (request.getAppointmentId() != null) {
            Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("الموعد غير موجود"));

            if (!appointment.getPatient().getId().equals(patient.getId())) {
                throw new BadRequestException("الموعد لا يخص هذا المريض");
            }

            invoice.setAppointment(appointment);
        }

        // Calculate tax and discount
        BigDecimal subtotal = BigDecimal.ZERO;
        List<InvoiceItem> items = new ArrayList<>();

        for (CreateInvoiceRequest.InvoiceItemRequest itemRequest : request.getItems()) {
            InvoiceItem item = new InvoiceItem();
            item.setInvoice(invoice);
            item.setServiceName(itemRequest.getServiceName());
            item.setServiceCode(itemRequest.getServiceCode());
            item.setDescription(itemRequest.getDescription());
            item.setCategory(itemRequest.getCategory());
            item.setQuantity(itemRequest.getQuantity());
            item.setUnitPrice(itemRequest.getUnitPrice());
            item.setDiscountAmount(itemRequest.getDiscountAmount());
            item.setTaxable(itemRequest.isTaxable());
            item.setNotes(itemRequest.getNotes());

            // Calculate item total
            BigDecimal itemTotal = itemRequest.getUnitPrice()
                    .multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

            // Apply item discount
            if (itemRequest.getDiscountAmount() != null && itemRequest.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
                itemTotal = itemTotal.subtract(itemRequest.getDiscountAmount());
            } else if (itemRequest.getDiscountPercentage() != null && itemRequest.getDiscountPercentage().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal discountAmount = itemTotal.multiply(itemRequest.getDiscountPercentage()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                item.setDiscountAmount(discountAmount);
                itemTotal = itemTotal.subtract(discountAmount);
            }

            item.setTotalPrice(itemTotal);
            items.add(item);
            subtotal = subtotal.add(itemTotal);
        }

        invoice.setItems(items);
        invoice.setSubtotal(subtotal);

        // Apply invoice-level discount
        BigDecimal discountAmount = request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO;
        if (request.getDiscountPercentage() != null && request.getDiscountPercentage().compareTo(BigDecimal.ZERO) > 0) {
            discountAmount = discountAmount.add(
                    subtotal.multiply(request.getDiscountPercentage()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
            );
        }
        invoice.setDiscountAmount(discountAmount);

        // Calculate tax
        BigDecimal taxAmount = BigDecimal.ZERO;
        if (request.getTaxPercentage() != null && request.getTaxPercentage().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal taxableAmount = subtotal.subtract(discountAmount);
            taxAmount = taxableAmount.multiply(request.getTaxPercentage()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        invoice.setTaxAmount(taxAmount);

        // Calculate totals
        invoice.calculateTotals();

        // Save invoice
        Invoice savedInvoice = invoiceRepository.save(invoice);

        // Log the action
        auditLogService.logAction(
                currentUser.getId(),
                AuditLogService.ACTION_CREATE,
                effectiveClinicId,
                "INVOICE",
                savedInvoice.getId(),
                "Created invoice " + savedInvoice.getInvoiceNumber() + " for patient " + patient.getFirstName() + " " + patient.getLastName()
        );

        logger.info("Successfully created invoice: {}", savedInvoice.getInvoiceNumber());
        return InvoiceResponse.fromEntity(savedInvoice);
    }

    /**
     * Get invoice by ID
     */
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceById(Long id, UserPrincipal currentUser) {
        logger.info("Fetching invoice: {} by user: {}", id, currentUser.getId());

        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("الفاتورة غير موجودة"));

        // Check permissions
        if (!UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
            Long userClinicId = currentUser.getClinicId();
            if (!invoice.getClinic().getId().equals(userClinicId)) {
                throw new ForbiddenOperationException("ليس لديك صلاحية لعرض هذه الفاتورة");
            }
        }

        // Mark as viewed if it was sent
        if (invoice.getStatus() == InvoiceStatus.SENT) {
            invoice.setStatus(InvoiceStatus.VIEWED);
            invoiceRepository.save(invoice);
        }

        return InvoiceResponse.fromEntity(invoice);
    }

    /**
     * Get all invoices with pagination
     */
    @Transactional(readOnly = true)
    public Page<InvoiceResponse> getAllInvoices(Pageable pageable, InvoiceSearchCriteria criteria, UserPrincipal currentUser) {
        logger.info("Fetching invoices with criteria by user: {}", currentUser.getId());

        // Determine clinic scope
        Long effectiveClinicId;
        if (UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
            effectiveClinicId = criteria.getClinicId(); // Can be null for all clinics
        } else {
            effectiveClinicId = currentUser.getClinicId();
        }

        Page<Invoice> invoices;

        if (effectiveClinicId != null) {
            Clinic clinic = clinicRepository.findById(effectiveClinicId)
                    .orElseThrow(() -> new ResourceNotFoundException("العيادة غير موجودة"));

            // Apply filters based on criteria
            if (criteria.getStatus() != null) {
                invoices = invoiceRepository.findByClinicAndStatus(clinic, criteria.getStatus(), pageable);
            } else if (criteria.getFromDate() != null && criteria.getToDate() != null) {
                invoices = invoiceRepository.findByClinicAndDateRange(clinic, criteria.getFromDate(), criteria.getToDate(), pageable);
            } else {
                invoices = invoiceRepository.findByClinic(clinic, pageable);
            }
        } else {
            invoices = invoiceRepository.findAll(pageable);
        }

        return invoices.map(InvoiceResponse::fromEntity);
    }

    /**
     * Update invoice
     */
    @Transactional
    public InvoiceResponse updateInvoice(Long id, UpdateInvoiceRequest request, UserPrincipal currentUser) {
        logger.info("Updating invoice: {} by user: {}", id, currentUser.getId());

        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("الفاتورة غير موجودة"));

        // Check permissions
        Long effectiveClinicId = clinicContextService.getEffectiveClinicId(currentUser);
        if (!invoice.getClinic().getId().equals(effectiveClinicId)) {
            throw new ForbiddenOperationException("ليس لديك صلاحية لتعديل هذه الفاتورة");
        }

        // Don't allow editing paid or cancelled invoices
        if (invoice.getStatus() == InvoiceStatus.PAID || invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new BadRequestException("لا يمكن تعديل فاتورة " + invoice.getStatus().getArabicName());
        }

        // Update fields
        if (request.getDueDate() != null) {
            invoice.setDueDate(request.getDueDate());
        }
        if (request.getNotes() != null) {
            invoice.setNotes(request.getNotes());
        }
        if (request.getTerms() != null) {
            // Invoice entity doesn't have terms field, so we might need to add it or store in notes
            invoice.setNotes(invoice.getNotes() + "\n\nالشروط: " + request.getTerms());
        }
        if (request.getStatus() != null) {
            invoice.setStatus(request.getStatus());
        }

        // Update items if provided
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            // Delete existing items
            invoiceItemRepository.deleteByInvoice(invoice);
            invoice.getItems().clear();

            // Add new items
            BigDecimal subtotal = BigDecimal.ZERO;
            for (CreateInvoiceRequest.InvoiceItemRequest itemRequest : request.getItems()) {
                InvoiceItem item = new InvoiceItem();
                item.setInvoice(invoice);
                item.setServiceName(itemRequest.getServiceName());
                item.setServiceCode(itemRequest.getServiceCode());
                item.setDescription(itemRequest.getDescription());
                item.setCategory(itemRequest.getCategory());
                item.setQuantity(itemRequest.getQuantity());
                item.setUnitPrice(itemRequest.getUnitPrice());
                item.setDiscountAmount(itemRequest.getDiscountAmount());
                item.setTaxable(itemRequest.isTaxable());
                item.setNotes(itemRequest.getNotes());

                // Calculate item total
                BigDecimal itemTotal = itemRequest.getUnitPrice()
                        .multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

                if (itemRequest.getDiscountAmount() != null && itemRequest.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
                    itemTotal = itemTotal.subtract(itemRequest.getDiscountAmount());
                }

                item.setTotalPrice(itemTotal);
                invoice.getItems().add(item);
                subtotal = subtotal.add(itemTotal);
            }

            invoice.setSubtotal(subtotal);
        }

        // Update financial details
        if (request.getDiscountAmount() != null || request.getDiscountPercentage() != null) {
            BigDecimal discountAmount = request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO;
            if (request.getDiscountPercentage() != null && request.getDiscountPercentage().compareTo(BigDecimal.ZERO) > 0) {
                discountAmount = invoice.getSubtotal().multiply(request.getDiscountPercentage()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            }
            invoice.setDiscountAmount(discountAmount);
        }

        if (request.getTaxPercentage() != null) {
            BigDecimal taxableAmount = invoice.getSubtotal().subtract(invoice.getDiscountAmount());
            BigDecimal taxAmount = taxableAmount.multiply(request.getTaxPercentage()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            invoice.setTaxAmount(taxAmount);
        }

        // Recalculate totals
        invoice.calculateTotals();

        // Check if overdue
        if (LocalDate.now().isAfter(invoice.getDueDate()) && invoice.getBalanceDue().compareTo(BigDecimal.ZERO) > 0) {
            invoice.setStatus(InvoiceStatus.OVERDUE);
        }

        Invoice updatedInvoice = invoiceRepository.save(invoice);

        // Log the action
        auditLogService.logAction(
                currentUser.getId(),
                AuditLogService.ACTION_UPDATE,
                effectiveClinicId,
                "INVOICE",
                updatedInvoice.getId(),
                "Updated invoice " + updatedInvoice.getInvoiceNumber()
        );

        logger.info("Successfully updated invoice: {}", updatedInvoice.getInvoiceNumber());
        return InvoiceResponse.fromEntity(updatedInvoice);
    }

    /**
     * Cancel invoice
     */
    @Transactional
    public InvoiceResponse cancelInvoice(Long id, String reason, UserPrincipal currentUser) {
        logger.info("Cancelling invoice: {} by user: {} - Reason: {}", id, currentUser.getId(), reason);

        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("الفاتورة غير موجودة"));

        // Check permissions
        Long effectiveClinicId = clinicContextService.getEffectiveClinicId(currentUser);
        if (!invoice.getClinic().getId().equals(effectiveClinicId)) {
            throw new ForbiddenOperationException("ليس لديك صلاحية لإلغاء هذه الفاتورة");
        }

        // Don't allow cancelling paid invoices
        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new BadRequestException("لا يمكن إلغاء فاتورة مدفوعة");
        }

        invoice.setStatus(InvoiceStatus.CANCELLED);
        invoice.setNotes((invoice.getNotes() != null ? invoice.getNotes() + "\n" : "") + "سبب الإلغاء: " + reason);

        Invoice cancelledInvoice = invoiceRepository.save(invoice);

        // Log the action
        auditLogService.logAction(
                currentUser.getId(),
                AuditLogService.ACTION_CANCEL,
                effectiveClinicId,
                "INVOICE",
                cancelledInvoice.getId(),
                "Cancelled invoice " + cancelledInvoice.getInvoiceNumber() + " - Reason: " + reason
        );

        logger.info("Successfully cancelled invoice: {}", cancelledInvoice.getInvoiceNumber());
        return InvoiceResponse.fromEntity(cancelledInvoice);
    }

    /**
     * Send invoice to patient
     */
    @Transactional
    public InvoiceResponse sendInvoice(Long id, UserPrincipal currentUser) {
        logger.info("Sending invoice: {} by user: {}", id, currentUser.getId());

        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("الفاتورة غير موجودة"));

        // Check permissions
        Long effectiveClinicId = clinicContextService.getEffectiveClinicId(currentUser);
        if (!invoice.getClinic().getId().equals(effectiveClinicId)) {
            throw new ForbiddenOperationException("ليس لديك صلاحية لإرسال هذه الفاتورة");
        }

        // Update status to sent
        if (invoice.getStatus() == InvoiceStatus.DRAFT || invoice.getStatus() == InvoiceStatus.PENDING) {
            invoice.setStatus(InvoiceStatus.SENT);
        }

        Invoice sentInvoice = invoiceRepository.save(invoice);

        // TODO: Implement actual email/SMS sending logic here

        logger.info("Successfully sent invoice: {}", sentInvoice.getInvoiceNumber());
        return InvoiceResponse.fromEntity(sentInvoice);
    }

    // ===================================================================
    // PAYMENT OPERATIONS
    // ===================================================================

    /**
     * Add payment to invoice
     */
    @Transactional
    public PaymentResponse addPayment(CreatePaymentRequest request, UserPrincipal currentUser) {
        logger.info("Adding payment to invoice: {} by user: {}", request.getInvoiceId(), currentUser.getId());

        Invoice invoice = invoiceRepository.findById(request.getInvoiceId())
                .orElseThrow(() -> new ResourceNotFoundException("الفاتورة غير موجودة"));

        // Check permissions
        Long effectiveClinicId = clinicContextService.getEffectiveClinicId(currentUser);
        if (!invoice.getClinic().getId().equals(effectiveClinicId)) {
            throw new ForbiddenOperationException("ليس لديك صلاحية لإضافة دفعة لهذه الفاتورة");
        }

        // Validate payment amount
        if (request.getAmount().compareTo(invoice.getBalanceDue()) > 0) {
            throw new BadRequestException("مبلغ الدفع أكبر من المبلغ المستحق");
        }

        // Get current user as creator
        User createdBy = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("المستخدم غير موجود"));

        // Create payment
        Payment payment = new Payment();
        payment.setClinic(invoice.getClinic());
        payment.setInvoice(invoice);
        payment.setPatient(invoice.getPatient());
        payment.setPaymentDate(LocalDate.now());
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setReferenceNumber(request.getReferenceNumber());
        payment.setNotes(request.getNotes());
        payment.setCreatedBy(createdBy);

        Payment savedPayment = paymentRepository.save(payment);

        // Update invoice paid amount and status
        BigDecimal newPaidAmount = invoice.getPaidAmount().add(request.getAmount());
        invoice.setPaidAmount(newPaidAmount);
        invoice.setBalanceDue(invoice.getTotalAmount().subtract(newPaidAmount));

        if (invoice.getBalanceDue().compareTo(BigDecimal.ZERO) == 0) {
            invoice.setStatus(InvoiceStatus.PAID);
        } else {
            invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
        }

        invoiceRepository.save(invoice);

        // Log the action
        auditLogService.logAction(
                currentUser.getId(),
                AuditLogService.ACTION_PAYMENT,
                effectiveClinicId,
                "PAYMENT",
                savedPayment.getId(),
                "Added payment of " + request.getAmount() + " to invoice " + invoice.getInvoiceNumber()
        );

        logger.info("Successfully added payment to invoice: {}", invoice.getInvoiceNumber());
        return PaymentResponse.fromEntity(savedPayment);
    }

    /**
     * Get payments for invoice
     */
    @Transactional(readOnly = true)
    public List<PaymentResponse> getInvoicePayments(Long invoiceId, UserPrincipal currentUser) {
        logger.info("Fetching payments for invoice: {} by user: {}", invoiceId, currentUser.getId());

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("الفاتورة غير موجودة"));

        // Check permissions
        if (!UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
            Long userClinicId = currentUser.getClinicId();
            if (!invoice.getClinic().getId().equals(userClinicId)) {
                throw new ForbiddenOperationException("ليس لديك صلاحية لعرض مدفوعات هذه الفاتورة");
            }
        }

        List<Payment> payments = paymentRepository.findByInvoiceOrderByPaymentDateDesc(invoice);
        return payments.stream()
                .map(PaymentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ===================================================================
    // STATISTICS AND REPORTS
    // ===================================================================

    /**
     * Get invoice statistics for clinic
     */
    @Transactional(readOnly = true)
    public InvoiceStatisticsResponse getInvoiceStatistics(Long clinicId, UserPrincipal currentUser) {
        logger.info("Fetching invoice statistics for clinic: {} by user: {}", clinicId, currentUser.getId());

        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new ResourceNotFoundException("العيادة غير موجودة"));

        // Check permissions
        if (!UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole()) &&
                !clinic.getId().equals(currentUser.getClinicId())) {
            throw new ForbiddenOperationException("ليس لديك صلاحية لعرض إحصائيات هذه العيادة");
        }

        InvoiceStatisticsResponse stats = new InvoiceStatisticsResponse();
        stats.setClinicId(clinicId);

        // Get counts by status
        stats.setTotalInvoices(invoiceRepository.countByClinic(clinic));
        stats.setPaidInvoices(invoiceRepository.countByClinicAndStatus(clinic, InvoiceStatus.PAID));
        stats.setPendingInvoices(invoiceRepository.countByClinicAndStatus(clinic, InvoiceStatus.PENDING));
        stats.setOverdueInvoices(invoiceRepository.countOverdueInvoices(clinic));
        stats.setCancelledInvoices(invoiceRepository.countByClinicAndStatus(clinic, InvoiceStatus.CANCELLED));

        // Get financial totals
        BigDecimal totalRevenue = invoiceRepository.getTotalRevenueByClinic(clinic);
        stats.setTotalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO);

        BigDecimal totalPaid = paymentRepository.getTotalPaymentsByClinic(clinic);
        stats.setTotalPaid(totalPaid != null ? totalPaid : BigDecimal.ZERO);

        BigDecimal totalPending = invoiceRepository.getTotalPendingAmount(clinic);
        stats.setTotalPending(totalPending != null ? totalPending : BigDecimal.ZERO);

        BigDecimal totalOverdue = invoiceRepository.getTotalOverdueAmount(clinic);
        stats.setTotalOverdue(totalOverdue != null ? totalOverdue : BigDecimal.ZERO);

        // Calculate average and collection rate
        if (stats.getTotalInvoices() > 0) {
            BigDecimal avgValue = stats.getTotalRevenue().divide(
                    BigDecimal.valueOf(stats.getTotalInvoices()), 2, RoundingMode.HALF_UP);
            stats.setAverageInvoiceValue(avgValue);

            if (stats.getTotalRevenue().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal collectionRate = stats.getTotalPaid()
                        .divide(stats.getTotalRevenue(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                stats.setCollectionRate(collectionRate);
            }
        }

        // Period-specific stats
        LocalDate today = LocalDate.now();
        stats.setTodayRevenue(paymentRepository.getTotalPaymentsByDate(clinic, today));
        stats.setMonthlyRevenue(paymentRepository.getMonthlyPayments(clinic, today.getYear(), today.getMonthValue()));

        stats.setTodayInvoices(invoiceRepository.countByClinicAndInvoiceDate(clinic, today));
        stats.setMonthlyInvoices(invoiceRepository.countByClinicAndMonth(clinic, today.getYear(), today.getMonthValue()));

        return stats;
    }

    /**
     * Get overdue invoices
     */
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getOverdueInvoices(Long clinicId, UserPrincipal currentUser) {
        logger.info("Fetching overdue invoices for clinic: {} by user: {}", clinicId, currentUser.getId());

        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new ResourceNotFoundException("العيادة غير موجودة"));

        // Check permissions
        if (!UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole()) &&
                !clinic.getId().equals(currentUser.getClinicId())) {
            throw new ForbiddenOperationException("ليس لديك صلاحية لعرض فواتير هذه العيادة");
        }

        List<Invoice> overdueInvoices = invoiceRepository.findOverdueInvoices(clinic, LocalDate.now());

        return overdueInvoices.stream()
                .map(InvoiceResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ===================================================================
    // HELPER METHODS
    // ===================================================================

    /**
     * Generate unique invoice number
     */
    private String generateInvoiceNumber(Clinic clinic) {
        String prefix = "INV-" + clinic.getId() + "-";
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));

        // Get last invoice number for this clinic and month
        String lastNumber = invoiceRepository.findLastInvoiceNumberByClinic(clinic)
                .orElse(prefix + datePart + "-0000");

        // Extract and increment the counter
        String[] parts = lastNumber.split("-");
        int counter = 1;
        if (parts.length > 3 && parts[2].equals(datePart)) {
            counter = Integer.parseInt(parts[3]) + 1;
        }

        return String.format("%s%s-%04d", prefix, datePart, counter);
    }
}