// src/main/java/com/nakqeeb/amancare/service/GuestBookingService.java

package com.nakqeeb.amancare.service;

import com.nakqeeb.amancare.dto.request.GuestBookingRequest;
import com.nakqeeb.amancare.dto.response.*;
import com.nakqeeb.amancare.entity.*;
import com.nakqeeb.amancare.exception.*;
import com.nakqeeb.amancare.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class GuestBookingService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final ClinicRepository clinicRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorScheduleRepository scheduleRepository;
    private final AppointmentConfirmationTokenRepository confirmationTokenRepository;
    private final DoctorScheduleService doctorScheduleService;
    private final EmailService emailService;
    private final AppointmentTokenService tokenService;

    private static final String PATIENT_NUMBER_PREFIX = "P";
    private static final int TOKEN_VALIDITY_HOURS = 48;

    /**
     * Get all active clinics for guest booking
     */
    @Transactional(readOnly = true)
    public List<ClinicSummaryResponse> getAllActiveClinics() {
        log.info("Fetching all active clinics for guest booking");

        List<Clinic> clinics = clinicRepository.findByIsActiveTrue();

        return clinics.stream()
                .map(this::mapToClinicSummaryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all doctors for a specific clinic with their availability
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "clinicDoctors", key = "#clinicId")
    public List<ClinicDoctorSummary> getClinicDoctors(Long clinicId) {
        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new ResourceNotFoundException("العيادة غير موجودة"));

        List<User> doctors = userRepository.findByClinicAndRoleAndIsActiveTrue(
                clinic, UserRole.DOCTOR);

        return doctors.stream()
                .map(this::mapDoctorToSummary)
                .collect(Collectors.toList());
    }

    /**
     * الحصول على جدول طبيب
     */
    @Transactional(readOnly = true)
    public List<DoctorSchedule> getDoctorSchedule(Long clinicId, Long doctorId) {
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("الطبيب غير موجود"));

        if (!doctor.getClinic().getId().equals(clinicId)) {
            throw new BadRequestException("الطبيب لا ينتمي لهذه العيادة");
        }

        return scheduleRepository.findByDoctorAndIsActiveTrueOrderByDayOfWeek(doctor);
    }

    /**
     * Get available time slots for a doctor on a specific date
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "availableSlots", key = "#clinicId + '-' + #doctorId + '-' + #date")
    public List<LocalTime> getAvailableTimeSlots(Long clinicId, Long doctorId,
                                                 LocalDate date, Integer durationMinutes) {
        // Validate clinic
        if (!clinicRepository.existsById(clinicId)) {
            throw new ResourceNotFoundException("العيادة غير موجودة");
        }

        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("الطبيب غير موجود"));

        if (!doctor.getClinic().getId().equals(clinicId)) {
            throw new BadRequestException("الطبيب لا ينتمي لهذه العيادة");
        }

        // Use existing DoctorScheduleService method
        return doctorScheduleService.getAvailableTimeSlots(clinicId, doctorId, date,
                durationMinutes != null ? durationMinutes : 30);
    }

    /**
     * Book appointment as guest
     */
    @Transactional
    public GuestBookingResponse bookAppointmentAsGuest(GuestBookingRequest request) {
        log.info("Processing guest booking for clinic: {}, doctor: {}",
                request.getClinicId(), request.getDoctorId());

        // 1. Validate clinic and doctor
        Clinic clinic = clinicRepository.findById(request.getClinicId())
                .orElseThrow(() -> new ResourceNotFoundException("العيادة غير موجودة"));

        User doctor = userRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("الطبيب غير موجود"));

        if (!doctor.getClinic().getId().equals(clinic.getId())) {
            throw new BadRequestException("الطبيب لا ينتمي لهذه العيادة");
        }

        // 2. Validate doctor availability
        if (!doctorScheduleService.isDoctorAvailable(doctor, request.getAppointmentDate(),
                request.getAppointmentTime())) {
            throw new BadRequestException("الطبيب غير متاح في هذا الوقت");
        }

        // 3. Check for conflicting appointments
        if (hasConflictingAppointment(doctor, request.getAppointmentDate(),
                request.getAppointmentTime(), request.getDurationMinutes())) {
            throw new BadRequestException("يوجد موعد آخر في هذا الوقت");
        }

        // 4. Create or find existing patient
        Patient patient = findOrCreatePatient(request, clinic);

        // 5. Create appointment with SCHEDULED status (not confirmed yet)
        Appointment appointment = createAppointment(patient, doctor, clinic, request);
        appointment = appointmentRepository.save(appointment);

        // 6. Generate confirmation token
        String token = generateSecureToken();
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(TOKEN_VALIDITY_HOURS);
        AppointmentConfirmationToken confirmationToken = new AppointmentConfirmationToken(
                token, appointment, expiryDate);
        confirmationTokenRepository.save(confirmationToken);

        // 7. Send confirmation email
        sendConfirmationEmail(patient, appointment, token, clinic);

        log.info("Guest booking created successfully. Appointment ID: {}, Patient Number: {}",
                appointment.getId(), patient.getPatientNumber());

        // 8. Return response
        return buildBookingResponse(patient, appointment, doctor, clinic);
    }

    /**
     * Confirm appointment via token
     */
    @Transactional
    public void confirmAppointment(String token) {
        AppointmentConfirmationToken confirmationToken = confirmationTokenRepository
                .findByToken(token)
                .orElseThrow(() -> new BadRequestException("رابط التأكيد غير صحيح"));

        if (!confirmationToken.isValid()) {
            throw new BadRequestException("انتهت صلاحية رابط التأكيد");
        }

        Appointment appointment = confirmationToken.getAppointment();

        // Update appointment status to CONFIRMED
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointmentRepository.save(appointment);

        // Mark token as used
        confirmationToken.markAsUsed();
        confirmationTokenRepository.save(confirmationToken);

        log.info("Appointment confirmed: {}", appointment.getId());
    }

    /**
     * Get patient appointments by patient number
     */
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getPatientAppointments(String patientNumber) {
        Patient patient = patientRepository.findByPatientNumber(patientNumber)
                .orElseThrow(() -> new ResourceNotFoundException("رقم المريض غير موجود"));

        List<Appointment> appointments = appointmentRepository
                .findByPatient(patient);

        return appointments.stream()
                .filter(apt -> apt.getAppointmentDate().isAfter(LocalDate.now()) ||
                        (apt.getAppointmentDate().equals(LocalDate.now()) &&
                                apt.getStatus() != AppointmentStatus.CANCELLED))
                .map(AppointmentResponse::fromAppointment)
                .sorted(Comparator.comparing(AppointmentResponse::getAppointmentDate)
                        .thenComparing(AppointmentResponse::getAppointmentTime))
                .collect(Collectors.toList());
    }

    /**
     * Cancel appointment by patient
     */
    @Transactional
    public void cancelAppointmentByPatient(Long appointmentId, String patientNumber) {
        Patient patient = patientRepository.findByPatientNumber(patientNumber)
                .orElseThrow(() -> new ResourceNotFoundException("رقم المريض غير موجود"));

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("الموعد غير موجود"));

        // Verify patient owns this appointment
        if (!appointment.getPatient().getId().equals(patient.getId())) {
            throw new ForbiddenOperationException("غير مصرح لك بإلغاء هذا الموعد");
        }

        // Validate appointment status
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new BadRequestException("لا يمكن إلغاء موعد مكتمل");
        }

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BadRequestException("الموعد ملغي مسبقاً");
        }

        // **NEW: Free up the token**
        tokenService.freeTokenFromAppointment(appointment);

        // Check if appointment is in the future
        LocalDateTime appointmentDateTime = LocalDateTime.of(
                appointment.getAppointmentDate(), appointment.getAppointmentTime());
        if (appointmentDateTime.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("لا يمكن إلغاء موعد انتهى وقته");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        String currentNotes = appointment.getNotes() != null ? appointment.getNotes() : "";
        appointment.setNotes(currentNotes + "\nتم الإلغاء من قبل المريض في " + LocalDateTime.now());

        appointmentRepository.save(appointment);
        log.info("Appointment cancelled by patient. ID: {}", appointmentId);
    }

    // ===================================================================
    // PRIVATE HELPER METHODS
    // ===================================================================

    private ClinicDoctorSummary mapDoctorToSummary(User doctor) {
        List<DoctorSchedule> schedules = scheduleRepository
                .findByDoctorAndIsActiveTrueOrderByDayOfWeek(doctor);

        List<ClinicDoctorSummary.WorkingDay> workingDays = schedules.stream()
                .map(schedule -> {
                    ClinicDoctorSummary.WorkingDay workingDay = new ClinicDoctorSummary.WorkingDay();
                    workingDay.setDay(schedule.getDayOfWeek());
                    workingDay.setDayArabic(getDayNameInArabic(schedule.getDayOfWeek()));
                    workingDay.setStartTime(schedule.getStartTime());
                    workingDay.setEndTime(schedule.getEndTime());
                    workingDay.setBreakStart(schedule.getBreakStartTime());
                    workingDay.setBreakEnd(schedule.getBreakEndTime());
                    return workingDay;
                })
                .collect(Collectors.toList());

        return ClinicDoctorSummary.fromDoctor(doctor, workingDays);
    }

    private Patient findOrCreatePatient(GuestBookingRequest request, Clinic clinic) {
        // Check if patient exists by phone or email
        Optional<Patient> existingPatient = patientRepository
                .findByPhoneAndClinic(request.getPhone(), clinic);

        if (existingPatient.isEmpty()) {
            existingPatient = patientRepository
                    .findByEmailAndClinic(request.getEmail(), clinic);
        }

        if (existingPatient.isPresent()) {
            log.info("Found existing patient: {}", existingPatient.get().getPatientNumber());
            return existingPatient.get();
        }

        // Create new patient
        Patient patient = new Patient();
        patient.setClinic(clinic);
        patient.setPatientNumber(generatePatientNumber(clinic));
        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setGender(request.getGender());
        patient.setPhone(request.getPhone());
        patient.setEmail(request.getEmail());
        patient.setAddress(request.getAddress());
        patient.setEmergencyContactName(request.getEmergencyContactName());
        patient.setEmergencyContactPhone(request.getEmergencyContactPhone());
        patient.setBloodType(request.getBloodType());
        patient.setAllergies(request.getAllergies());
        patient.setChronicDiseases(request.getChronicDiseases());
        patient.setNotes(request.getNotes());
        patient.setIsActive(true);

        patient = patientRepository.save(patient);
        log.info("Created new patient: {}", patient.getPatientNumber());
        return patient;
    }

    private Appointment createAppointment(Patient patient, User doctor, Clinic clinic,
                                          GuestBookingRequest request) {
        Appointment appointment = new Appointment();
        appointment.setClinic(clinic);
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setAppointmentTime(request.getAppointmentTime());
        appointment.setDurationMinutes(request.getDurationMinutes());
        appointment.setAppointmentType(request.getAppointmentType());
        appointment.setStatus(AppointmentStatus.SCHEDULED); // Will be CONFIRMED after email confirmation
        appointment.setChiefComplaint(request.getChiefComplaint());
        appointment.setNotes(request.getNotes());
        appointment.setCreatedBy(doctor); // Guest bookings are marked as created by the doctor

        // **NEW: Assign token number**
        tokenService.assignTokenToAppointment(appointment, request.getDurationMinutes());

        return appointment;
    }

    private boolean hasConflictingAppointment(User doctor, LocalDate date,
                                              LocalTime time, Integer duration) {
        List<Appointment> existingAppointments = appointmentRepository
                .findByDoctorAndAppointmentDate(doctor, date);

        LocalTime endTime = time.plusMinutes(duration != null ? duration : 30);

        for (Appointment apt : existingAppointments) {
            if (apt.getStatus() == AppointmentStatus.CANCELLED) {
                continue;
            }

            LocalTime aptStart = apt.getAppointmentTime();
            LocalTime aptEnd = aptStart.plusMinutes(apt.getDurationMinutes());

            // Check for time overlap
            if (!(time.isAfter(aptEnd) || time.equals(aptEnd) ||
                    endTime.isBefore(aptStart) || endTime.equals(aptStart))) {
                return true;
            }
        }

        return false;
    }

    private void sendConfirmationEmail(Patient patient, Appointment appointment,
                                       String token, Clinic clinic) {
        try {
            emailService.sendAppointmentConfirmationEmail(
                    patient.getEmail(),
                    token,
                    patient.getFullName(),
                    patient.getPatientNumber(),
                    appointment.getDoctor().getFullName(),
                    clinic.getName(),
                    appointment.getAppointmentDate(),
                    appointment.getAppointmentTime(),
                    appointment.getTokenNumber()
            );
        } catch (Exception e) {
            log.error("Failed to send confirmation email: {}", e.getMessage());
            // Don't fail the booking if email fails
        }
    }

    private GuestBookingResponse buildBookingResponse(Patient patient, Appointment appointment,
                                                      User doctor, Clinic clinic) {
        return new GuestBookingResponse(
                appointment.getId(),
                patient.getPatientNumber(),
                patient.getFullName(),
                doctor.getFullName(),
                doctor.getSpecialization(),
                clinic.getName(),
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime(),
                appointment.getTokenNumber(), // **NEW**
                patient.getEmail(),
                "تم حجز موعدك بنجاح! تم إرسال رسالة تأكيد إلى بريدك الإلكتروني. " +
                        "رقم المريض الخاص بك: " + patient.getPatientNumber()
        );
    }

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

    private String generateSecureToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String getDayNameInArabic(DayOfWeek day) {
        return switch (day) {
            case SUNDAY -> "الأحد";
            case MONDAY -> "الإثنين";
            case TUESDAY -> "الثلاثاء";
            case WEDNESDAY -> "الأربعاء";
            case THURSDAY -> "الخميس";
            case FRIDAY -> "الجمعة";
            case SATURDAY -> "السبت";
        };
    }

    private ClinicSummaryResponse mapToClinicSummaryResponse(Clinic clinic) {
        ClinicSummaryResponse dto = new ClinicSummaryResponse();
        dto.setId(clinic.getId());
        dto.setName(clinic.getName());
        dto.setPhone(clinic.getPhone());
        dto.setEmail(clinic.getEmail());
        dto.setSubscriptionPlan(clinic.getSubscriptionPlan());
        dto.setIsActive(clinic.getIsActive());

        // Count patients and users if relationships exist
        if (clinic.getPatients() != null) {
            dto.setPatientCount((long) clinic.getPatients().size());
        } else {
            dto.setPatientCount(0L);
        }

        if (clinic.getUsers() != null) {
            dto.setUserCount((long) clinic.getUsers().size());
        } else {
            dto.setUserCount(0L);
        }

        return dto;
    }
}