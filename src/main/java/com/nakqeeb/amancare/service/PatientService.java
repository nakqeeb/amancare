// =============================================================================
// Patient Service - خدمة إدارة المرضى
// =============================================================================

package com.nakqeeb.amancare.service;

import com.nakqeeb.amancare.dto.request.CreatePatientRequest;
import com.nakqeeb.amancare.dto.request.UpdatePatientRequest;
import com.nakqeeb.amancare.dto.response.PatientPageResponse;
import com.nakqeeb.amancare.dto.response.PatientResponse;
import com.nakqeeb.amancare.dto.response.PatientStatistics;
import com.nakqeeb.amancare.dto.response.PatientSummaryResponse;
import com.nakqeeb.amancare.entity.Clinic;
import com.nakqeeb.amancare.entity.Patient;
import com.nakqeeb.amancare.exception.ResourceNotFoundException;
import com.nakqeeb.amancare.exception.BadRequestException;
import com.nakqeeb.amancare.repository.ClinicRepository;
import com.nakqeeb.amancare.repository.PatientRepository;
import com.nakqeeb.amancare.util.DateTimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * خدمة إدارة المرضى
 */
@Service
@Transactional
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private ClinicRepository clinicRepository;

    /**
     * إنشاء مريض جديد
     */
    public PatientResponse createPatient(Long clinicId, CreatePatientRequest request) {
        // التحقق من وجود العيادة
        Clinic clinic = clinicRepository.findById(clinicId)
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