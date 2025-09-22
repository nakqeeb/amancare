// =============================================================================
// Medical Record Service - خدمة السجلات الطبية
// src/main/java/com/nakqeeb/amancare/service/MedicalRecordService.java
// =============================================================================

package com.nakqeeb.amancare.service;

import com.nakqeeb.amancare.dto.request.healthrecords.*;
import com.nakqeeb.amancare.dto.response.healthrecords.*;
import com.nakqeeb.amancare.entity.healthrecords.*;
import com.nakqeeb.amancare.entity.*;
import com.nakqeeb.amancare.exception.ResourceNotFoundException;
import com.nakqeeb.amancare.exception.BusinessLogicException;
import com.nakqeeb.amancare.exception.UnauthorizedAccessException;
import com.nakqeeb.amancare.repository.*;
import com.nakqeeb.amancare.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MedicalRecordService {

    @Autowired
    private final MedicalRecordRepository medicalRecordRepository;

    @Autowired
    private final PatientRepository patientRepository;

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final ClinicRepository clinicRepository;

    @Autowired
    private final AppointmentRepository appointmentRepository;

    @Autowired
    private final AuditLogService auditLogService;

    @Autowired
    private ClinicContextService clinicContextService;

    // =============================================================================
    // CREATE OPERATIONS
    // =============================================================================

    /**
     * إنشاء سجل طبي جديد
     */
    public MedicalRecordResponse createMedicalRecord(CreateMedicalRecordRequest request, UserPrincipal currentUser) {
        log.info("إنشاء سجل طبي جديد للمريض {} بواسطة الطبيب {}", request.getPatientId(), request.getDoctorId());

        // Get effective clinic ID - this will throw exception if SYSTEM_ADMIN has no context
        Long effectiveClinicId = clinicContextService.getEffectiveClinicId(currentUser);
        // Validate request
        validateCreateRequest(request, currentUser);

        Clinic clinic = clinicRepository.findById(effectiveClinicId)
                .orElseThrow(() -> new ResourceNotFoundException("العيادة غير موجودة"));

        Patient patient = patientRepository.findByIdAndClinic(request.getPatientId(), clinic)
                .orElseThrow(() -> new ResourceNotFoundException("المريض غير موجود"));

        User doctor = userRepository.findByIdAndClinicIdAndRole(request.getDoctorId(), effectiveClinicId, UserRole.DOCTOR)
                .orElseThrow(() -> new ResourceNotFoundException("الطبيب غير موجود"));


        // Check appointment if provided
        Appointment appointment = null;
        if (request.getAppointmentId() != null) {
            appointment = appointmentRepository.findByIdAndClinic(request.getAppointmentId(), clinic)
                    .orElseThrow(() -> new ResourceNotFoundException("الموعد غير موجود"));

            // Check if appointment already has a medical record
            if (medicalRecordRepository.findByAppointmentIdAndClinicId(request.getAppointmentId(), currentUser.getClinicId()).isPresent()) {
                throw new BusinessLogicException("يوجد سجل طبي مرتبط بهذا الموعد بالفعل");
            }
        }

        // Create medical record
        MedicalRecord medicalRecord = buildMedicalRecordFromRequest(request, patient, doctor, clinic, appointment, currentUser);

        // Save medical record
        MedicalRecord savedRecord = medicalRecordRepository.save(medicalRecord);

        // Log audit
        auditLogService.logActivity(
                "إنشاء سجل طبي",
                "MEDICAL_RECORD",
                savedRecord.getId(),
                currentUser.getId(),
                currentUser.getClinicId(),
                "تم إنشاء سجل طبي جديد للمريض: " + patient.getFirstName() + " " + patient.getLastName()
        );

        log.info("تم إنشاء السجل الطبي {} بنجاح", savedRecord.getId());
        return MedicalRecordResponse.fromEntity(savedRecord);
    }

    // =============================================================================
    // READ OPERATIONS
    // =============================================================================

    /**
     * الحصول على سجل طبي بالمعرف
     */
    @Transactional(readOnly = true)
    public MedicalRecordResponse getMedicalRecordById(Long id, UserPrincipal currentUser) {
        log.info("البحث عن السجل الطبي {} بواسطة المستخدم {}", id, currentUser.getUsername());

        MedicalRecord medicalRecord;

        if (UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
            // SYSTEM_ADMIN can access records from any clinic
            medicalRecord = medicalRecordRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("السجل الطبي غير موجود"));
        } else {
            medicalRecord = medicalRecordRepository.findByIdAndClinicId(id, currentUser.getClinicId())
                    .orElseThrow(() -> new ResourceNotFoundException("السجل الطبي غير موجود"));
        }

        // Check confidentiality permissions
        if (medicalRecord.getIsConfidential() && !canAccessConfidentialRecord(medicalRecord, currentUser)) {
            throw new UnauthorizedAccessException("غير مصرح بالوصول لهذا السجل السري");
        }

        return MedicalRecordResponse.fromEntity(medicalRecord);
    }

    /**
     * الحصول على جميع السجلات الطبية مع الترقيم
     */
    @Transactional(readOnly = true)
    public Page<MedicalRecordSummaryResponse> getAllMedicalRecords(Pageable pageable, UserPrincipal currentUser) {
        log.info("البحث عن السجلات الطبية - الصفحة {}", pageable.getPageNumber());

        Page<MedicalRecord> medicalRecords;

        if (UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
            medicalRecords = medicalRecordRepository.findAllRecordsForSystemAdmin(pageable);
        } else {
            medicalRecords = medicalRecordRepository.findByClinicIdOrderByVisitDateDescCreatedAtDesc(
                    currentUser.getClinicId(), pageable);
        }

        return medicalRecords.map(this::filterConfidentialData)
                .map(MedicalRecordSummaryResponse::fromEntity);
    }

    /**
     * البحث في السجلات الطبية
     */
    @Transactional(readOnly = true)
    public Page<MedicalRecordSummaryResponse> searchMedicalRecords(
            MedicalRecordSearchCriteria criteria, Pageable pageable, UserPrincipal currentUser) {

        log.info("البحث المتقدم في السجلات الطبية: {}", criteria);

        Page<MedicalRecord> medicalRecords;

        if (UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
            medicalRecords = medicalRecordRepository.findBySearchCriteriaForSystemAdmin(
                    criteria.getClinicId(),
                    criteria.getPatientId(),
                    criteria.getDoctorId(),
                    criteria.getVisitType(),
                    criteria.getStatus(),
                    criteria.getVisitDateFrom(),
                    criteria.getVisitDateTo(),
                    criteria.getIsConfidential(),
                    criteria.getSearchTerm(),
                    pageable
            );
        } else {
            medicalRecords = medicalRecordRepository.findBySearchCriteria(
                    currentUser.getClinicId(),
                    criteria.getPatientId(),
                    criteria.getDoctorId(),
                    criteria.getVisitType(),
                    criteria.getStatus(),
                    criteria.getVisitDateFrom(),
                    criteria.getVisitDateTo(),
                    criteria.getIsConfidential(),
                    criteria.getSearchTerm(),
                    pageable
            );
        }

        return medicalRecords.map(this::filterConfidentialData)
                .map(MedicalRecordSummaryResponse::fromEntity);
    }

    /**
     * الحصول على السجلات الطبية للمريض
     */
    @Transactional(readOnly = true)
    public Page<MedicalRecordSummaryResponse> getPatientMedicalHistory(
            Long patientId, Pageable pageable, UserPrincipal currentUser) {

        log.info("البحث عن التاريخ الطبي للمريض {}", patientId);


        Clinic clinic = clinicRepository.findById(currentUser.getClinicId())
                .orElseThrow(() -> new ResourceNotFoundException("العيادة غير موجودة"));

        // Verify patient exists and belongs to clinic
        if (!UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
            patientRepository.findByIdAndClinic(patientId, clinic)
                    .orElseThrow(() -> new ResourceNotFoundException("المريض غير موجود"));
        }

        Page<MedicalRecord> medicalRecords;

        if (UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
            medicalRecords = medicalRecordRepository.findByPatientIdOrderByVisitDateDescCreatedAtDesc(
                    patientId, pageable);
        } else {
            medicalRecords = medicalRecordRepository.findByPatientIdAndClinicIdOrderByVisitDateDescCreatedAtDesc(
                    patientId, currentUser.getClinicId(), pageable);
        }

        return medicalRecords.map(this::filterConfidentialData)
                .map(MedicalRecordSummaryResponse::fromEntity);
    }

    /**
     * الحصول على السجلات الطبية للطبيب
     */
    @Transactional(readOnly = true)
    public Page<MedicalRecordSummaryResponse> getDoctorMedicalRecords(
            Long doctorId, Pageable pageable, UserPrincipal currentUser) {

        log.info("البحث عن السجلات الطبية للطبيب {}", doctorId);

        // Verify doctor exists and belongs to clinic
        if (!UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
            userRepository.findByIdAndClinicIdAndRole(doctorId, currentUser.getClinicId(), UserRole.DOCTOR)
                    .orElseThrow(() -> new ResourceNotFoundException("الطبيب غير موجود"));
        }

        Page<MedicalRecord> medicalRecords;

        if (UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
            medicalRecords = medicalRecordRepository.findByDoctorIdOrderByVisitDateDescCreatedAtDesc(
                    doctorId, pageable);
        } else {
            medicalRecords = medicalRecordRepository.findByDoctorIdAndClinicIdOrderByVisitDateDescCreatedAtDesc(
                    doctorId, currentUser.getClinicId(), pageable);
        }

        return medicalRecords.map(this::filterConfidentialData)
                .map(MedicalRecordSummaryResponse::fromEntity);
    }

    /**
     * الحصول على السجل الطبي بالموعد
     */
    @Transactional(readOnly = true)
    public MedicalRecordResponse getMedicalRecordByAppointment(Long appointmentId, UserPrincipal currentUser) {
        log.info("البحث عن السجل الطبي للموعد {}", appointmentId);

        MedicalRecord medicalRecord = medicalRecordRepository.findByAppointmentIdAndClinicId(
                        appointmentId, currentUser.getClinicId())
                .orElseThrow(() -> new ResourceNotFoundException("لا يوجد سجل طبي مرتبط بهذا الموعد"));

        // Check confidentiality permissions
        if (medicalRecord.getIsConfidential() && !canAccessConfidentialRecord(medicalRecord, currentUser)) {
            throw new UnauthorizedAccessException("غير مصرح بالوصول لهذا السجل السري");
        }

        return MedicalRecordResponse.fromEntity(medicalRecord);
    }

    // =============================================================================
    // UPDATE OPERATIONS
    // =============================================================================

    /**
     * تحديث السجل الطبي
     */
    public MedicalRecordResponse updateMedicalRecord(
            Long id, UpdateMedicalRecordRequest request, UserPrincipal currentUser) {

        log.info("تحديث السجل الطبي {} بواسطة المستخدم {}", id, currentUser.getUsername());

        // Get existing record
        MedicalRecord existingRecord = medicalRecordRepository.findByIdAndClinicId(id, currentUser.getClinicId())
                .orElseThrow(() -> new ResourceNotFoundException("السجل الطبي غير موجود"));

        // Check if record can be modified
        if (!canModifyRecord(existingRecord, currentUser)) {
            throw new BusinessLogicException("لا يمكن تعديل هذا السجل الطبي");
        }

        // Update record
        updateMedicalRecordFromRequest(existingRecord, request, currentUser);

        // Save changes
        MedicalRecord updatedRecord = medicalRecordRepository.save(existingRecord);

        // Log audit
        auditLogService.logActivity(
                "تحديث سجل طبي",
                "MEDICAL_RECORD",
                updatedRecord.getId(),
                currentUser.getId(),
                currentUser.getClinicId(),
                "تم تحديث السجل الطبي للمريض: " + updatedRecord.getPatient().getFirstName() + " " +
                        updatedRecord.getPatient().getLastName()
        );

        log.info("تم تحديث السجل الطبي {} بنجاح", updatedRecord.getId());
        return MedicalRecordResponse.fromEntity(updatedRecord);
    }

    /**
     * تحديث حالة السجل الطبي
     */
    public MedicalRecordResponse updateRecordStatus(
            Long id, UpdateRecordStatusRequest request, UserPrincipal currentUser) {

        log.info("تحديث حالة السجل الطبي {} إلى {}", id, request.getStatus());

        MedicalRecord medicalRecord = medicalRecordRepository.findByIdAndClinicId(id, currentUser.getClinicId())
                .orElseThrow(() -> new ResourceNotFoundException("السجل الطبي غير موجود"));

        // Check if status can be changed
        if (!canChangeStatus(medicalRecord, request.getStatus(), currentUser)) {
            throw new BusinessLogicException("لا يمكن تغيير حالة السجل إلى " + request.getStatus().getArabicName());
        }

        RecordStatus oldStatus = medicalRecord.getStatus();
        medicalRecord.setStatus(request.getStatus());
        medicalRecord.setUpdatedBy(currentUser.getUsername());

        // Add notes if provided
        if (request.getNotes() != null && !request.getNotes().trim().isEmpty()) {
            String statusNote = "\n--- تغيير الحالة من " + oldStatus.getArabicName() +
                    " إلى " + request.getStatus().getArabicName() + " ---\n" + request.getNotes();
            medicalRecord.setNotes((medicalRecord.getNotes() != null ? medicalRecord.getNotes() : "") + statusNote);
        }

        MedicalRecord updatedRecord = medicalRecordRepository.save(medicalRecord);

        // Log audit
        auditLogService.logActivity(
                "تحديث حالة سجل طبي",
                "MEDICAL_RECORD",
                updatedRecord.getId(),
                currentUser.getId(),
                currentUser.getClinicId(),
                "تم تغيير حالة السجل من " + oldStatus.getArabicName() + " إلى " + request.getStatus().getArabicName()
        );

        return MedicalRecordResponse.fromEntity(updatedRecord);
    }

    // =============================================================================
    // DELETE OPERATIONS
    // =============================================================================

    /**
     * حذف السجل الطبي (منطقي)
     */
    public void deleteMedicalRecord(Long id, UserPrincipal currentUser) {
        log.info("حذف السجل الطبي {} بواسطة المستخدم {}", id, currentUser.getUsername());

        MedicalRecord medicalRecord = medicalRecordRepository.findByIdAndClinicId(id, currentUser.getClinicId())
                .orElseThrow(() -> new ResourceNotFoundException("السجل الطبي غير موجود"));

        // Check if record can be deleted
        if (!canDeleteRecord(medicalRecord, currentUser)) {
            throw new BusinessLogicException("لا يمكن حذف هذا السجل الطبي");
        }

        // Mark as cancelled instead of hard delete for audit purposes
        medicalRecord.setStatus(RecordStatus.CANCELLED);
        medicalRecord.setUpdatedBy(currentUser.getUsername());
        medicalRecordRepository.save(medicalRecord);

        // Log audit
        auditLogService.logActivity(
                "حذف سجل طبي",
                "MEDICAL_RECORD",
                medicalRecord.getId(),
                currentUser.getId(),
                currentUser.getClinicId(),
                "تم حذف السجل الطبي للمريض: " + medicalRecord.getPatient().getFirstName() + " " +
                        medicalRecord.getPatient().getLastName()
        );

        log.info("تم حذف السجل الطبي {} بنجاح", id);
    }

    /**
     * الحذف النهائي للسجل الطبي (SYSTEM_ADMIN فقط)
     */
    public void permanentlyDeleteMedicalRecord(Long id, UserPrincipal currentUser) {
        if (!UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
            throw new UnauthorizedAccessException("غير مصرح بالحذف النهائي للسجلات الطبية");
        }

        log.info("الحذف النهائي للسجل الطبي {} بواسطة مدير النظام {}", id, currentUser.getUsername());

        MedicalRecord medicalRecord = medicalRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("السجل الطبي غير موجود"));

        String patientInfo = medicalRecord.getPatient().getFirstName() + " " + medicalRecord.getPatient().getLastName();

        medicalRecordRepository.delete(medicalRecord);

        // Log audit
        auditLogService.logActivity(
                "الحذف النهائي لسجل طبي",
                "MEDICAL_RECORD",
                id,
                currentUser.getId(),
                medicalRecord.getClinicId(),
                "تم الحذف النهائي للسجل الطبي للمريض: " + patientInfo
        );

        log.info("تم الحذف النهائي للسجل الطبي {} بنجاح", id);
    }

    // =============================================================================
    // STATISTICS OPERATIONS
    // =============================================================================

    /**
     * الحصول على إحصائيات السجلات الطبية
     */
    @Transactional(readOnly = true)
    public MedicalRecordStatisticsResponse getMedicalRecordStatistics(UserPrincipal currentUser) {
        log.info("الحصول على إحصائيات السجلات الطبية للمستخدم {}", currentUser.getUsername());

        Long clinicId = UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole()) ? null : currentUser.getClinicId();

        // Get basic counts
        Long totalRecords = clinicId != null ?
                medicalRecordRepository.countByClinicId(clinicId) :
                medicalRecordRepository.countAllRecords();

        Long completedRecords = clinicId != null ?
                medicalRecordRepository.countByStatusAndClinicId(RecordStatus.COMPLETED, clinicId) :
                medicalRecordRepository.countAllRecordsByStatus(RecordStatus.COMPLETED);

        Long draftRecords = clinicId != null ?
                medicalRecordRepository.countByStatusAndClinicId(RecordStatus.DRAFT, clinicId) :
                medicalRecordRepository.countAllRecordsByStatus(RecordStatus.DRAFT);

        Long reviewedRecords = clinicId != null ?
                medicalRecordRepository.countByStatusAndClinicId(RecordStatus.REVIEWED, clinicId) :
                medicalRecordRepository.countAllRecordsByStatus(RecordStatus.REVIEWED);

        Long lockedRecords = clinicId != null ?
                medicalRecordRepository.countByStatusAndClinicId(RecordStatus.LOCKED, clinicId) :
                medicalRecordRepository.countAllRecordsByStatus(RecordStatus.LOCKED);

        // Time-based statistics
        Long recordsToday = clinicId != null ?
                medicalRecordRepository.countTodayRecords(clinicId) : 0L;
        Long recordsThisWeek = clinicId != null ?
                medicalRecordRepository.countThisWeekRecords(clinicId) : 0L;
        Long recordsThisMonth = clinicId != null ?
                medicalRecordRepository.countThisMonthRecords(clinicId) : 0L;

        // Get common diagnoses and medications
        List<DiagnosisFrequencyResponse> commonDiagnoses = getCommonDiagnoses(clinicId);
        List<MedicationFrequencyResponse> commonMedications = getCommonMedications(clinicId);
        List<VisitTypeStatsResponse> visitTypeStats = getVisitTypeStatistics(clinicId);

        return MedicalRecordStatisticsResponse.builder()
                .totalRecords(totalRecords)
                .completedRecords(completedRecords)
                .draftRecords(draftRecords)
                .reviewedRecords(reviewedRecords)
                .lockedRecords(lockedRecords)
                .recordsToday(recordsToday)
                .recordsThisWeek(recordsThisWeek)
                .recordsThisMonth(recordsThisMonth)
                .commonDiagnoses(commonDiagnoses)
                .commonMedications(commonMedications)
                .visitTypeStats(visitTypeStats)
                .build();
    }

    // =============================================================================
    // PRIVATE HELPER METHODS
    // =============================================================================

    private void validateCreateRequest(CreateMedicalRecordRequest request, UserPrincipal currentUser) {
        // Validate at least one primary diagnosis
        if (request.getDiagnosis() == null || request.getDiagnosis().isEmpty()) {
            throw new BusinessLogicException("يجب إدراج تشخيص واحد على الأقل");
        }

        boolean hasPrimaryDiagnosis = request.getDiagnosis().stream()
                .anyMatch(d -> d.getIsPrimary());

        if (!hasPrimaryDiagnosis) {
            throw new BusinessLogicException("يجب تحديد تشخيص أساسي واحد على الأقل");
        }

        // Validate vital signs if provided
        if (request.getVitalSigns() != null) {
            validateVitalSigns(request.getVitalSigns());
        }

        // Only doctors can create medical records
        if (!UserRole.DOCTOR.name().equals(currentUser.getRole()) &&
                !UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
            throw new UnauthorizedAccessException("فقط الأطباء يمكنهم إنشاء السجلات الطبية");
        }
    }

    private void validateVitalSigns(VitalSignsDto vitalSigns) {
        // Calculate BMI if weight and height are provided
        if (vitalSigns.getWeight() != null && vitalSigns.getHeight() != null) {
            BigDecimal heightInMeters = vitalSigns.getHeight().divide(BigDecimal.valueOf(100));
            BigDecimal bmi = vitalSigns.getWeight().divide(
                    heightInMeters.multiply(heightInMeters), 2, BigDecimal.ROUND_HALF_UP);
            vitalSigns.setBmi(bmi);
        }
    }

    private MedicalRecord buildMedicalRecordFromRequest(
            CreateMedicalRecordRequest request, Patient patient, User doctor, Clinic clinic,
            Appointment appointment, UserPrincipal currentUser) {

        MedicalRecord medicalRecord = MedicalRecord.builder()
                .patient(patient)
                .appointment(appointment)
                .doctor(doctor)
                .clinic(clinic)
                .visitDate(request.getVisitDate())
                .visitType(request.getVisitType())
                .vitalSigns(mapVitalSigns(request.getVitalSigns()))
                .chiefComplaint(request.getChiefComplaint())
                .presentIllness(request.getPresentIllness())
                .pastMedicalHistory(request.getPastMedicalHistory())
                .familyHistory(request.getFamilyHistory())
                .socialHistory(request.getSocialHistory())
                .allergies(request.getAllergies() != null ? new ArrayList<>(request.getAllergies()) : new ArrayList<>())
                .currentMedications(request.getCurrentMedications() != null ? new ArrayList<>(request.getCurrentMedications()) : new ArrayList<>())
                .physicalExamination(request.getPhysicalExamination())
                .systemicExamination(request.getSystemicExamination())
                .treatmentPlan(request.getTreatmentPlan())
                .followUpDate(request.getFollowUpDate())
                .followUpInstructions(request.getFollowUpInstructions())
                .notes(request.getNotes())
                .isConfidential(request.getIsConfidential())
                .status(request.getStatus())
                .createdBy(currentUser.getUsername())
                .updatedBy(currentUser.getUsername())
                .build();

        // Add diagnosis
        if (request.getDiagnosis() != null) {
            request.getDiagnosis().forEach(diagnosisDto -> {
                Diagnosis diagnosis = mapDiagnosis(diagnosisDto);
                medicalRecord.addDiagnosis(diagnosis);
            });
        }

        // Add prescriptions
        if (request.getPrescriptions() != null) {
            request.getPrescriptions().forEach(prescriptionDto -> {
                Prescription prescription = mapPrescription(prescriptionDto);
                medicalRecord.addPrescription(prescription);
            });
        }

        // Add lab tests
        if (request.getLabTests() != null) {
            request.getLabTests().forEach(labTestDto -> {
                LabTest labTest = mapLabTest(labTestDto);
                medicalRecord.addLabTest(labTest);
            });
        }

        // Add radiology tests
        if (request.getRadiologyTests() != null) {
            request.getRadiologyTests().forEach(radiologyTestDto -> {
                RadiologyTest radiologyTest = mapRadiologyTest(radiologyTestDto);
                medicalRecord.addRadiologyTest(radiologyTest);
            });
        }

        // Add procedures
        if (request.getProcedures() != null) {
            request.getProcedures().forEach(procedureDto -> {
                MedicalProcedure procedure = mapMedicalProcedure(procedureDto);
                medicalRecord.addProcedure(procedure);
            });
        }

        // Add referrals
        if (request.getReferrals() != null) {
            request.getReferrals().forEach(referralDto -> {
                Referral referral = mapReferral(referralDto);
                medicalRecord.addReferral(referral);
            });
        }

        return medicalRecord;
    }

    private boolean canAccessConfidentialRecord(MedicalRecord medicalRecord, UserPrincipal currentUser) {
        // System admin can access all records
        if (UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
            return true;
        }

        // Record creator can always access
        if (medicalRecord.getDoctorId().equals(currentUser.getId())) {
            return true;
        }

        // Admins can access confidential records in their clinic
        if (UserRole.ADMIN.name().equals(currentUser.getRole())) {
            return true;
        }

        return false;
    }

    private boolean canModifyRecord(MedicalRecord medicalRecord, UserPrincipal currentUser) {
        // Cannot modify locked or cancelled records
        if (medicalRecord.getStatus() == RecordStatus.LOCKED ||
                medicalRecord.getStatus() == RecordStatus.CANCELLED) {
            return false;
        }

        // System admin can modify all records
        if (UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
            return true;
        }

        // Record creator can modify their own records
        if (medicalRecord.getDoctorId().equals(currentUser.getId())) {
            return true;
        }

        // Admins can modify records in their clinic (except locked ones)
        if (UserRole.ADMIN.name().equals(currentUser.getRole())) {
            return medicalRecord.getStatus() != RecordStatus.LOCKED;
        }

        return false;
    }

    private boolean canChangeStatus(MedicalRecord medicalRecord, RecordStatus newStatus, UserPrincipal currentUser) {
        RecordStatus currentStatus = medicalRecord.getStatus();

        // System admin can change any status
        if (UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
            return true;
        }

        // Cannot change from/to cancelled
        if (currentStatus == RecordStatus.CANCELLED || newStatus == RecordStatus.CANCELLED) {
            return UserRole.ADMIN.name().equals(currentUser.getRole()) ||
                    UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole());
        }

        // Cannot unlock records unless admin
        if (currentStatus == RecordStatus.LOCKED &&
                !UserRole.ADMIN.name().equals(currentUser.getRole())) {
            return false;
        }

        // Record creator can change status of their own records
        if (medicalRecord.getDoctorId().equals(currentUser.getId())) {
            return true;
        }

        // Admins can change status
        if (UserRole.ADMIN.name().equals(currentUser.getRole())) {
            return true;
        }

        return false;
    }

    private boolean canDeleteRecord(MedicalRecord medicalRecord, UserPrincipal currentUser) {
        // Cannot delete locked records
        if (medicalRecord.getStatus() == RecordStatus.LOCKED) {
            return false;
        }

        // System admin can delete any record
        if (UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
            return true;
        }

        // Only admins can delete records (not even the creator)
        return UserRole.ADMIN.name().equals(currentUser.getRole());
    }

    private MedicalRecord filterConfidentialData(MedicalRecord medicalRecord) {
        // This method would filter sensitive data based on user permissions
        // For now, we return the full record, but this could be enhanced
        return medicalRecord;
    }

    // Additional mapping and helper methods would go here...

    private VitalSigns mapVitalSigns(VitalSignsDto dto) {
        if (dto == null) return null;

        return VitalSigns.builder()
                .temperature(dto.getTemperature())
                .bloodPressureSystolic(dto.getBloodPressureSystolic())
                .bloodPressureDiastolic(dto.getBloodPressureDiastolic())
                .heartRate(dto.getHeartRate())
                .respiratoryRate(dto.getRespiratoryRate())
                .oxygenSaturation(dto.getOxygenSaturation())
                .weight(dto.getWeight())
                .height(dto.getHeight())
                .bmi(dto.getBmi())
                .bloodSugar(dto.getBloodSugar())
                .painScale(dto.getPainScale())
                .build();
    }

    private Diagnosis mapDiagnosis(CreateDiagnosisDto dto) {
        return Diagnosis.builder()
                .icdCode(dto.getIcdCode())
                .description(dto.getDescription())
                .type(dto.getType())
                .isPrimary(dto.getIsPrimary())
                .notes(dto.getNotes())
                .build();
    }

    private Prescription mapPrescription(CreatePrescriptionDto dto) {
        return Prescription.builder()
                .medicationName(dto.getMedicationName())
                .genericName(dto.getGenericName())
                .dosage(dto.getDosage())
                .frequency(dto.getFrequency())
                .duration(dto.getDuration())
                .route(dto.getRoute())
                .instructions(dto.getInstructions())
                .quantity(dto.getQuantity())
                .refills(dto.getRefills())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .isPrn(dto.getIsPrn())
                .build();
    }

    private LabTest mapLabTest(CreateLabTestDto dto) {
        return LabTest.builder()
                .testName(dto.getTestName())
                .testCode(dto.getTestCode())
                .category(dto.getCategory())
                .urgency(dto.getUrgency())
                .specimenType(dto.getSpecimenType())
                .instructions(dto.getInstructions())
                .orderedDate(LocalDateTime.now().toLocalDate())
                .build();
    }

    private RadiologyTest mapRadiologyTest(CreateRadiologyTestDto dto) {
        return RadiologyTest.builder()
                .testName(dto.getTestName())
                .testType(dto.getTestType())
                .bodyPart(dto.getBodyPart())
                .urgency(dto.getUrgency())
                .instructions(dto.getInstructions())
                .orderedDate(LocalDateTime.now().toLocalDate())
                .build();
    }

    private MedicalProcedure mapMedicalProcedure(CreateMedicalProcedureDto dto) {
        return MedicalProcedure.builder()
                .procedureName(dto.getProcedureName())
                .procedureCode(dto.getProcedureCode())
                .category(dto.getCategory())
                .description(dto.getDescription())
                .performedDate(dto.getPerformedDate())
                .performedBy(dto.getPerformedBy())
                .complications(dto.getComplications())
                .outcome(dto.getOutcome())
                .notes(dto.getNotes())
                .build();
    }

    private Referral mapReferral(CreateReferralDto dto) {
        return Referral.builder()
                .referralType(dto.getReferralType())
                .referredTo(dto.getReferredTo())
                .specialty(dto.getSpecialty())
                .priority(dto.getPriority())
                .reason(dto.getReason())
                .notes(dto.getNotes())
                .referralDate(dto.getReferralDate())
                .appointmentDate(dto.getAppointmentDate())
                .build();
    }

    private void updateMedicalRecordFromRequest(
            MedicalRecord existingRecord, UpdateMedicalRecordRequest request, UserPrincipal currentUser) {

        if (request.getVisitType() != null) {
            existingRecord.setVisitType(request.getVisitType());
        }

        if (request.getVitalSigns() != null) {
            existingRecord.setVitalSigns(mapVitalSigns(request.getVitalSigns()));
        }

        if (request.getChiefComplaint() != null) {
            existingRecord.setChiefComplaint(request.getChiefComplaint());
        }

        if (request.getPresentIllness() != null) {
            existingRecord.setPresentIllness(request.getPresentIllness());
        }

        if (request.getPastMedicalHistory() != null) {
            existingRecord.setPastMedicalHistory(request.getPastMedicalHistory());
        }

        if (request.getFamilyHistory() != null) {
            existingRecord.setFamilyHistory(request.getFamilyHistory());
        }

        if (request.getSocialHistory() != null) {
            existingRecord.setSocialHistory(request.getSocialHistory());
        }

        if (request.getAllergies() != null) {
            existingRecord.setAllergies(new ArrayList<>(request.getAllergies()));
        }

        if (request.getCurrentMedications() != null) {
            existingRecord.setCurrentMedications(new ArrayList<>(request.getCurrentMedications()));
        }

        if (request.getPhysicalExamination() != null) {
            existingRecord.setPhysicalExamination(request.getPhysicalExamination());
        }

        if (request.getSystemicExamination() != null) {
            existingRecord.setSystemicExamination(request.getSystemicExamination());
        }

        if (request.getTreatmentPlan() != null) {
            existingRecord.setTreatmentPlan(request.getTreatmentPlan());
        }

        if (request.getFollowUpDate() != null) {
            existingRecord.setFollowUpDate(request.getFollowUpDate());
        }

        if (request.getFollowUpInstructions() != null) {
            existingRecord.setFollowUpInstructions(request.getFollowUpInstructions());
        }

        if (request.getNotes() != null) {
            existingRecord.setNotes(request.getNotes());
        }

        if (request.getIsConfidential() != null) {
            existingRecord.setIsConfidential(request.getIsConfidential());
        }

        if (request.getStatus() != null) {
            existingRecord.setStatus(request.getStatus());
        }

        // Update related entities if provided
        if (request.getDiagnosis() != null) {
            // Clear existing diagnosis and add new ones
            existingRecord.getDiagnosis().clear();
            request.getDiagnosis().forEach(diagnosisDto -> {
                Diagnosis diagnosis = mapDiagnosis(diagnosisDto);
                existingRecord.addDiagnosis(diagnosis);
            });
        }

        // Similar updates for prescriptions, lab tests, etc...

        existingRecord.setUpdatedBy(currentUser.getUsername());
    }

    private List<DiagnosisFrequencyResponse> getCommonDiagnoses(Long clinicId) {
        Pageable topTen = PageRequest.of(0, 10);
        List<Object[]> results = clinicId != null ?
                medicalRecordRepository.findMostCommonDiagnoses(clinicId, topTen) :
                new ArrayList<>();

        return results.stream()
                .map(result -> DiagnosisFrequencyResponse.builder()
                        .diagnosis((String) result[0])
                        .icdCode((String) result[1])
                        .count((Long) result[2])
                        .build())
                .collect(Collectors.toList());
    }

    private List<MedicationFrequencyResponse> getCommonMedications(Long clinicId) {
        Pageable topTen = PageRequest.of(0, 10);
        List<Object[]> results = clinicId != null ?
                medicalRecordRepository.findMostPrescribedMedications(clinicId, topTen) :
                new ArrayList<>();

        return results.stream()
                .map(result -> MedicationFrequencyResponse.builder()
                        .medicationName((String) result[0])
                        .genericName((String) result[1])
                        .count((Long) result[2])
                        .build())
                .collect(Collectors.toList());
    }

    private List<VisitTypeStatsResponse> getVisitTypeStatistics(Long clinicId) {
        List<Object[]> results = clinicId != null ?
                medicalRecordRepository.findVisitTypeStatistics(clinicId) :
                new ArrayList<>();

        return results.stream()
                .map(result -> VisitTypeStatsResponse.builder()
                        .visitType((VisitType) result[0])
                        .visitTypeArabic(((VisitType) result[0]).getArabicName())
                        .count((Long) result[1])
                        .build())
                .collect(Collectors.toList());
    }
}