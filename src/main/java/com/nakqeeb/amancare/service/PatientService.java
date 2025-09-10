// =============================================================================
// Patient Service - خدمة إدارة المرضى
// =============================================================================

package com.nakqeeb.amancare.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nakqeeb.amancare.dto.request.CreatePatientRequest;
import com.nakqeeb.amancare.dto.request.UpdatePatientRequest;
import com.nakqeeb.amancare.dto.response.*;
import com.nakqeeb.amancare.entity.*;
import com.nakqeeb.amancare.exception.ResourceNotFoundException;
import com.nakqeeb.amancare.exception.BadRequestException;
import com.nakqeeb.amancare.repository.*;
import com.nakqeeb.amancare.security.UserPrincipal;
import com.nakqeeb.amancare.util.DateTimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * خدمة إدارة المرضى
 */
@Service
@Transactional
public class PatientService {
    private static final Logger logger = LoggerFactory.getLogger(PatientService.class);

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private ClinicRepository clinicRepository;

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClinicContextService clinicContextService;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private AuditLogService auditLogService;

    /**
     * إنشاء مريض جديد
     */
    public PatientResponse createPatient(UserPrincipal currentUser, CreatePatientRequest request) {
        // Get effective clinic ID - this will throw exception if SYSTEM_ADMIN has no context
        Long effectiveClinicId = clinicContextService.getEffectiveClinicId(currentUser);

        logger.info("Creating patient in clinic {} by user {}",
                effectiveClinicId, currentUser.getUsername());

        // التحقق من وجود العيادة
        Clinic clinic = clinicRepository.findById(effectiveClinicId)
                .orElseThrow(() -> new ResourceNotFoundException("العيادة غير موجودة"));

        // التحقق من عدم وجود مريض بنفس رقم الهاتف
        if (patientRepository.findByClinicAndPhone(clinic, request.getPhone()).isPresent()) {
            throw new BadRequestException("يوجد مريض آخر بنفس رقم الهاتف في هذه العيادة");
        }

        // إنشاء المريض
        Patient patient = new Patient();
        patient.setClinic(clinic);
        patient.setFirstName(request.getFirstName().trim());
        patient.setLastName(request.getLastName().trim());
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setGender(request.getGender());
        patient.setPhone(request.getPhone().trim());
        patient.setEmail(StringUtils.hasText(request.getEmail()) ? request.getEmail().trim() : null);
        patient.setAddress(request.getAddress());
        patient.setEmergencyContactName(request.getEmergencyContactName());
        patient.setEmergencyContactPhone(request.getEmergencyContactPhone());
        patient.setBloodType(request.getBloodType());
        patient.setAllergies(request.getAllergies());
        patient.setChronicDiseases(request.getChronicDiseases());
        patient.setNotes(request.getNotes());

        // توليد رقم المريض التلقائي
        patient.setPatientNumber(generatePatientNumber(clinic));

        Patient savedPatient = patientRepository.save(patient);

        // Log action if SYSTEM_ADMIN
        if (UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
            auditLogService.logAction(
                    currentUser.getId(),
                    "CREATE_PATIENT",
                    effectiveClinicId,
                    "PATIENT",
                    savedPatient.getId(),
                    "Created patient: " + savedPatient.getFullName()
            );
        }

        return PatientResponse.fromPatient(savedPatient);
    }

    /**
     * الحصول على مريض بالمعرف
     */
    @Transactional(readOnly = true)
    public PatientResponse getPatientById(Long clinicId, Long patientId) {
        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new ResourceNotFoundException("العيادة غير موجودة"));

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("المريض غير موجود"));

        // التأكد من أن المريض ينتمي لهذه العيادة
        if (!patient.getClinic().getId().equals(clinicId)) {
            throw new ResourceNotFoundException("المريض غير موجود في هذه العيادة");
        }

        return PatientResponse.fromPatient(patient);
    }

    /**
     * الحصول على مريض برقم المريض
     */
    @Transactional(readOnly = true)
    public PatientResponse getPatientByNumber(Long clinicId, String patientNumber) {
        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new ResourceNotFoundException("العيادة غير موجودة"));

        Patient patient = patientRepository.findByClinicAndPatientNumber(clinic, patientNumber)
                .orElseThrow(() -> new ResourceNotFoundException("المريض برقم " + patientNumber + " غير موجود"));

        return PatientResponse.fromPatient(patient);
    }

    /**
     * الحصول على جميع المرضى مع ترقيم الصفحات
     */
    @Transactional(readOnly = true)
    public PatientPageResponse getAllPatients(Long clinicId, int page, int size, String sortBy, String sortDirection) {
        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new ResourceNotFoundException("العيادة غير موجودة"));

        // إعداد الترتيب
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        // جلب المرضى النشطين
        Page<Patient> patientsPage = patientRepository.findByClinicAndIsActiveTrue(clinic, pageable);

        List<PatientSummaryResponse> patientSummaries = patientsPage.getContent()
                .stream()
                .map(PatientSummaryResponse::fromPatient)
                .collect(Collectors.toList());

        return new PatientPageResponse(
                patientSummaries,
                patientsPage.getTotalElements(),
                patientsPage.getTotalPages(),
                patientsPage.getNumber(),
                patientsPage.getSize(),
                patientsPage.hasPrevious(),
                patientsPage.hasNext()
        );
    }

    /**
     * البحث في المرضى
     */
    @Transactional(readOnly = true)
    public PatientPageResponse searchPatients(Long clinicId, String searchTerm, int page, int size) {
        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new ResourceNotFoundException("العيادة غير موجودة"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("firstName", "lastName"));

        Page<Patient> patientsPage;
        if (StringUtils.hasText(searchTerm)) {
            patientsPage = patientRepository.searchPatients(clinic, searchTerm.trim(), pageable);
        } else {
            patientsPage = patientRepository.findByClinicAndIsActiveTrue(clinic, pageable);
        }

        List<PatientSummaryResponse> patientSummaries = patientsPage.getContent()
                .stream()
                .map(PatientSummaryResponse::fromPatient)
                .collect(Collectors.toList());

        return new PatientPageResponse(
                patientSummaries,
                patientsPage.getTotalElements(),
                patientsPage.getTotalPages(),
                patientsPage.getNumber(),
                patientsPage.getSize(),
                patientsPage.hasPrevious(),
                patientsPage.hasNext()
        );
    }

    /**
     * تحديث بيانات مريض
     */
    public PatientResponse updatePatient(Long clinicId, Long patientId, UpdatePatientRequest request) {
        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new ResourceNotFoundException("العيادة غير موجودة"));

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("المريض غير موجود"));

        // التأكد من أن المريض ينتمي لهذه العيادة
        if (!patient.getClinic().getId().equals(clinicId)) {
            throw new ResourceNotFoundException("المريض غير موجود في هذه العيادة");
        }

        // التحقق من رقم الهاتف إذا تم تغييره
        if (StringUtils.hasText(request.getPhone()) && !request.getPhone().equals(patient.getPhone())) {
            patientRepository.findByClinicAndPhone(clinic, request.getPhone()).ifPresent(existingPatient -> {
                if (!existingPatient.getId().equals(patientId)) {
                    throw new BadRequestException("يوجد مريض آخر بنفس رقم الهاتف في هذه العيادة");
                }
            });
        }

        // تحديث البيانات
        updatePatientFields(patient, request);

        Patient updatedPatient = patientRepository.save(patient);
        return PatientResponse.fromPatient(updatedPatient);
    }

    /**
     * حذف مريض (إلغاء تفعيل)
     */
    public void deletePatient(Long clinicId, Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("المريض غير موجود"));

        // التأكد من أن المريض ينتمي لهذه العيادة
        if (!patient.getClinic().getId().equals(clinicId)) {
            throw new ResourceNotFoundException("المريض غير موجود في هذه العيادة");
        }

        // إلغاء تفعيل المريض بدلاً من حذفه نهائياً
        patient.setIsActive(false);
        patientRepository.save(patient);
    }

    /**
     * إعادة تفعيل مريض
     */
    public PatientResponse reactivatePatient(Long clinicId, Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("المريض غير موجود"));

        // التأكد من أن المريض ينتمي لهذه العيادة
        if (!patient.getClinic().getId().equals(clinicId)) {
            throw new ResourceNotFoundException("المريض غير موجود في هذه العيادة");
        }

        patient.setIsActive(true);
        Patient reactivatedPatient = patientRepository.save(patient);
        return PatientResponse.fromPatient(reactivatedPatient);
    }

    /**
     * الحصول على إحصائيات المرضى
     */
//    @Transactional(readOnly = true)
//    public PatientStatistics getPatientStatistics(Long clinicId) {
//        Clinic clinic = clinicRepository.findById(clinicId)
//                .orElseThrow(() -> new ResourceNotFoundException("العيادة غير موجودة"));
//
//        long totalPatients = patientRepository.countActivePatientsByClinic(clinic);
//
//        LocalDate oneMonthAgo = LocalDate.now().minusMonths(1);
//        long newPatientsThisMonth = patientRepository.countPatientsCreatedBetween(
//                clinic, oneMonthAgo, LocalDate.now()
//        );
//
//        return new PatientStatistics(totalPatients, newPatientsThisMonth);
//    }
    @Transactional(readOnly = true)
    public PatientStatistics getPatientStatistics(Long clinicId) {
        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new ResourceNotFoundException("العيادة غير موجودة"));

        long totalPatients = patientRepository.countActivePatientsByClinic(clinic);

        // Convert LocalDate to LocalDateTime range for proper comparison
        LocalDate oneMonthAgo = LocalDate.now().minusMonths(1);
        LocalDate today = LocalDate.now();

        LocalDateTime startDateTime = DateTimeUtil.getStartOfDay(oneMonthAgo);
        LocalDateTime endDateTime = DateTimeUtil.getEndOfDay(today);

        long newPatientsThisMonth = patientRepository.countPatientsCreatedBetween(
                clinic, startDateTime, endDateTime
        );

        return new PatientStatistics(totalPatients, newPatientsThisMonth);
    }

    /**
     * المرضى الذين لديهم مواعيد اليوم
     */
    @Transactional(readOnly = true)
    public List<PatientSummaryResponse> getTodayPatients(Long clinicId) {
        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new ResourceNotFoundException("العيادة غير موجودة"));

        List<Patient> todayPatients = patientRepository.findPatientsWithAppointmentsOnDate(clinic, LocalDate.now());

        return todayPatients.stream()
                .map(PatientSummaryResponse::fromPatient)
                .collect(Collectors.toList());
    }

    /**
     * حذف المريض نهائياً من قاعدة البيانات
     * WARNING: This operation cannot be undone
     */
    @Transactional
    public PermanentDeleteResponse permanentlyDeletePatient(Long clinicId, Long patientId, Long deletedByUserId) {
        logger.info("Starting permanent deletion of patient {} by user {}", patientId, deletedByUserId);

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("المريض غير موجود"));

        // FIX: Check user role to allow SYSTEM_ADMIN cross-clinic deletion
        User deletingUser = userRepository.findById(deletedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // SYSTEM_ADMIN can delete patients from any clinic
        if (deletingUser.getRole() != UserRole.SYSTEM_ADMIN) {
            // Only check clinic ID for non-SYSTEM_ADMIN users
            if (!patient.getClinic().getId().equals(clinicId)) {
                logger.error("Patient {} does not belong to clinic {}", patientId, clinicId);
                throw new ResourceNotFoundException("المريض غير موجود في هذه العيادة");
            }
        } else {
            // Log cross-clinic deletion for SYSTEM_ADMIN
            if (!patient.getClinic().getId().equals(clinicId)) {
                logger.warn("CROSS-CLINIC DELETION: SYSTEM_ADMIN {} (clinic {}) deleting patient {} from clinic {}",
                        deletingUser.getUsername(),
                        clinicId,
                        patientId,
                        patient.getClinic().getId());
            }
        }

        // Check for future appointments
        LocalDate today = LocalDate.now();
        LocalTime currentTime = LocalTime.now();

        long futureAppointments = patient.getAppointments().stream()
                .filter(apt -> {
                    LocalDate aptDate = apt.getAppointmentDate();
                    LocalTime aptTime = apt.getAppointmentTime();

                    boolean isFuture = aptDate.isAfter(today) ||
                            (aptDate.equals(today) && aptTime.isAfter(currentTime));

                    return isFuture && apt.getStatus() != AppointmentStatus.CANCELLED;
                })
                .count();

        if (futureAppointments > 0) {
            throw new IllegalStateException(
                    String.format("لا يمكن حذف المريض - يوجد %d موعد مستقبلي", futureAppointments));
        }

        // Check for unpaid invoices
        BigDecimal totalUnpaid = patient.getInvoices().stream()
                .filter(inv -> inv.getStatus() != InvoiceStatus.PAID)
                .map(Invoice::getBalanceDue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalUnpaid.compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalStateException(
                    String.format("لا يمكن حذف المريض - يوجد مبلغ مستحق: %.2f", totalUnpaid));
        }

        // Create comprehensive audit log
        AuditLog auditLog = new AuditLog.Builder()
                .action("PERMANENT_DELETE_PATIENT")
                .entityType("PATIENT")
                .entityId(patient.getId())
                .userId(deletingUser.getId())
                .username(deletingUser.getUsername())
                .userRole(deletingUser.getRole().toString())
                .clinicId(patient.getClinic().getId())  // Use patient's clinic for audit
                .clinicName(patient.getClinic().getName())
                .severity(AuditLog.AuditSeverity.CRITICAL)
                .success(true)
                .build();

        // Store deletion details
        Map<String, Object> deletedData = new HashMap<>();
        deletedData.put("patientNumber", patient.getPatientNumber());
        deletedData.put("fullName", patient.getFullName());
        deletedData.put("phone", patient.getPhone());
        deletedData.put("patientClinicId", patient.getClinic().getId());
        deletedData.put("deletedByUserId", deletingUser.getId());
        deletedData.put("deletedByUserClinicId", deletingUser.getClinic().getId());
        deletedData.put("crossClinicDeletion", !patient.getClinic().getId().equals(deletingUser.getClinic().getId()));

        try {
            auditLog.setData(new ObjectMapper().writeValueAsString(deletedData));
        } catch (Exception e) {
            logger.error("Error serializing audit data", e);
        }

        auditLogRepository.save(auditLog);

        // Delete related records
        if (patient.getMedicalRecords() != null && !patient.getMedicalRecords().isEmpty()) {
            medicalRecordRepository.deleteAll(patient.getMedicalRecords());
        }

        if (patient.getAppointments() != null && !patient.getAppointments().isEmpty()) {
            appointmentRepository.deleteAll(patient.getAppointments());
        }

        if (patient.getInvoices() != null && !patient.getInvoices().isEmpty()) {
            for (Invoice invoice : patient.getInvoices()) {
                if (invoice.getPayments() != null) {
                    paymentRepository.deleteAll(invoice.getPayments());
                }
            }
            invoiceRepository.deleteAll(patient.getInvoices());
        }

        // Delete the patient
        patientRepository.delete(patient);

        logger.warn("Patient {} permanently deleted by {} (role: {})",
                patientId, deletingUser.getUsername(), deletingUser.getRole());

        // Create response
        PermanentDeleteResponse response = new PermanentDeleteResponse();
        response.setPatientId(patient.getId());
        response.setPatientNumber(patient.getPatientNumber());
        response.setPatientName(patient.getFullName());
        response.setDeletedAt(LocalDateTime.now());
        response.setDeletedByUserId(deletingUser.getId());
        response.setRecordsDeleted(Map.of(
                "appointments", patient.getAppointments() != null ? patient.getAppointments().size() : 0,
                "medicalRecords", patient.getMedicalRecords() != null ? patient.getMedicalRecords().size() : 0,
                "invoices", patient.getInvoices() != null ? patient.getInvoices().size() : 0
        ));

        return response;
    }

    /**
     * Validate that patient can be permanently deleted
     */
    private void validatePatientCanBeDeleted(Patient patient) {
        // Check for future appointments
        /*LocalDateTime now = LocalDateTime.now();
        long futureAppointments = patient.getAppointments().stream()
                .filter(apt -> apt.getAppointmentDateTime().isAfter(now)
                        && apt.getStatus() != AppointmentStatus.CANCELLED)
                .count();*/
        LocalDate today = LocalDate.now();
        LocalTime currentTime = LocalTime.now();

        long futureAppointments = patient.getAppointments().stream()
                .filter(apt -> {
                    LocalDate aptDate = apt.getAppointmentDate();
                    LocalTime aptTime = apt.getAppointmentTime();

                    boolean isFuture = aptDate.isAfter(today) ||
                            (aptDate.equals(today) && aptTime.isAfter(currentTime));

                    return isFuture && apt.getStatus() != AppointmentStatus.CANCELLED;
                })
                .count();

        if (futureAppointments > 0) {
            throw new IllegalStateException(
                    String.format("لا يمكن حذف المريض - يوجد %d موعد مستقبلي", futureAppointments));
        }

        // Check for unpaid invoices
        BigDecimal totalUnpaid = patient.getInvoices().stream()
                .filter(inv -> inv.getStatus() != InvoiceStatus.PAID)
                .map(Invoice::getBalanceDue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalUnpaid.compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalStateException(
                    String.format("لا يمكن حذف المريض - يوجد مبلغ مستحق: %.2f", totalUnpaid));
        }
    }

    /**
     * Delete all patient-related data
     */
    private void deletePatientRelatedData(Patient patient) {
        // Delete medical records
        if (patient.getMedicalRecords() != null && !patient.getMedicalRecords().isEmpty()) {
            medicalRecordRepository.deleteAll(patient.getMedicalRecords());
            logger.debug("Deleted {} medical records for patient {}",
                    patient.getMedicalRecords().size(), patient.getId());
        }

        // Delete appointments
        if (patient.getAppointments() != null && !patient.getAppointments().isEmpty()) {
            appointmentRepository.deleteAll(patient.getAppointments());
            logger.debug("Deleted {} appointments for patient {}",
                    patient.getAppointments().size(), patient.getId());
        }

        // Delete invoices and payments
        if (patient.getInvoices() != null && !patient.getInvoices().isEmpty()) {
            for (Invoice invoice : patient.getInvoices()) {
                // Delete payments first
                if (invoice.getPayments() != null) {
                    paymentRepository.deleteAll(invoice.getPayments());
                }
            }
            invoiceRepository.deleteAll(patient.getInvoices());
            logger.debug("Deleted {} invoices for patient {}",
                    patient.getInvoices().size(), patient.getId());
        }

        // Delete any patient documents/files
        // deletePatientFiles(patient.getId());
    }

    /**
     * Create audit log for permanent deletion
     */
    private PermanentDeleteResponse createDeletionAuditLog(Patient patient, Long deletedByUserId) throws JsonProcessingException {
        // Create audit entry
        AuditLog auditLog = new AuditLog();
        auditLog.setAction("PERMANENT_DELETE_PATIENT");
        auditLog.setEntityType("PATIENT");
        auditLog.setEntityId(patient.getId());
        auditLog.setUserId(deletedByUserId);
        auditLog.setClinicId(patient.getClinic().getId());
        auditLog.setTimestamp(LocalDateTime.now());

        // Store patient summary in audit log
        Map<String, Object> deletedData = new HashMap<>();
        deletedData.put("patientNumber", patient.getPatientNumber());
        deletedData.put("fullName", patient.getFullName());
        deletedData.put("phone", patient.getPhone());
        // deletedData.put("nationalId", patient.getNationalId());
        deletedData.put("dateOfBirth", patient.getDateOfBirth());
        deletedData.put("totalAppointments", patient.getAppointments().size());
        deletedData.put("totalMedicalRecords", patient.getMedicalRecords().size());
        deletedData.put("totalInvoices", patient.getInvoices().size());

        auditLog.setData(new ObjectMapper().writeValueAsString(deletedData));
        auditLogRepository.save(auditLog);

        // Create response
        PermanentDeleteResponse response = new PermanentDeleteResponse();
        response.setPatientId(patient.getId());
        response.setPatientNumber(patient.getPatientNumber());
        response.setPatientName(patient.getFullName());
        response.setDeletedAt(LocalDateTime.now());
        response.setDeletedByUserId(deletedByUserId);
        response.setRecordsDeleted(Map.of(
                "appointments", patient.getAppointments().size(),
                "medicalRecords", patient.getMedicalRecords().size(),
                "invoices", patient.getInvoices().size()
        ));

        return response;
    }

    /**
     * Delete patient files from storage
     */
    /* private void deletePatientFiles(Long patientId) {
        try {
            // Delete from file storage service
            String patientFolder = "patients/" + patientId;
            fileStorageService.deleteFolder(patientFolder);
            logger.debug("Deleted files for patient {}", patientId);
        } catch (Exception e) {
            logger.error("Error deleting files for patient {}: {}", patientId, e.getMessage());
        }
    }*/

    // =============================================================================
    // Private Helper Methods
    // =============================================================================

    /**
     * توليد رقم مريض تلقائي
     */
    private String generatePatientNumber(Clinic clinic) {
        LocalDate currentDate = LocalDate.now();
        String prefix = "P" + currentDate.getYear() + String.format("%02d", currentDate.getMonthValue());

        // البحث عن آخر رقم مريض بنفس البادئة
        long count = patientRepository.countActivePatientsByClinic(clinic);

        // توليد رقم فريد
        String patientNumber;
        do {
            count++;
            patientNumber = prefix + String.format("%04d", count);
        } while (patientRepository.existsByClinicAndPatientNumber(clinic, patientNumber));

        return patientNumber;
    }

    /**
     * تحديث حقول المريض
     */
    private void updatePatientFields(Patient patient, UpdatePatientRequest request) {
        if (StringUtils.hasText(request.getFirstName())) {
            patient.setFirstName(request.getFirstName().trim());
        }
        if (StringUtils.hasText(request.getLastName())) {
            patient.setLastName(request.getLastName().trim());
        }
        if (request.getDateOfBirth() != null) {
            patient.setDateOfBirth(request.getDateOfBirth());
        }
        if (StringUtils.hasText(request.getPhone())) {
            patient.setPhone(request.getPhone().trim());
        }
        if (request.getEmail() != null) {
            patient.setEmail(StringUtils.hasText(request.getEmail()) ? request.getEmail().trim() : null);
        }
        if (request.getAddress() != null) {
            patient.setAddress(request.getAddress());
        }
        if (request.getEmergencyContactName() != null) {
            patient.setEmergencyContactName(request.getEmergencyContactName());
        }
        if (request.getEmergencyContactPhone() != null) {
            patient.setEmergencyContactPhone(request.getEmergencyContactPhone());
        }
        if (request.getBloodType() != null) {
            patient.setBloodType(request.getBloodType());
        }
        if (request.getAllergies() != null) {
            patient.setAllergies(request.getAllergies());
        }
        if (request.getChronicDiseases() != null) {
            patient.setChronicDiseases(request.getChronicDiseases());
        }
        if (request.getNotes() != null) {
            patient.setNotes(request.getNotes());
        }
    }
}