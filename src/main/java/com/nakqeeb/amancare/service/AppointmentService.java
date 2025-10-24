// =============================================================================
// Appointment Service - خدمة إدارة المواعيد
// =============================================================================

package com.nakqeeb.amancare.service;

import com.nakqeeb.amancare.controller.PatientController;
import com.nakqeeb.amancare.dto.request.CreateAppointmentRequest;
import com.nakqeeb.amancare.dto.request.OverrideDurationRequest;
import com.nakqeeb.amancare.dto.request.UpdateAppointmentRequest;
import com.nakqeeb.amancare.dto.response.AppointmentPageResponse;
import com.nakqeeb.amancare.dto.response.AppointmentResponse;
import com.nakqeeb.amancare.dto.response.AppointmentStatistics;
import com.nakqeeb.amancare.dto.response.AppointmentSummaryResponse;
import com.nakqeeb.amancare.entity.*;
import com.nakqeeb.amancare.exception.BadRequestException;
import com.nakqeeb.amancare.exception.ConflictException;
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
import org.springframework.util.StringUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * خدمة إدارة المواعيد
 */
@Service
@Transactional
public class AppointmentService {
    private static final Logger logger = LoggerFactory.getLogger(AppointmentService.class);

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClinicRepository clinicRepository;

    @Autowired
    private ClinicContextService clinicContextService;

    @Autowired
    private DoctorScheduleRepository doctorScheduleRepository;

    @Autowired
    private AppointmentTokenService tokenService;

    @Autowired
    private DoctorScheduleService doctorScheduleService;

    @Autowired
    private DoctorScheduleRepository scheduleRepository;

    /**
     * إنشاء موعد جديد
     */
    @Transactional
    public AppointmentResponse createAppointment(UserPrincipal currentUser, CreateAppointmentRequest request) {
        // Get effective clinic ID - this will throw exception if SYSTEM_ADMIN has no context
        Long effectiveClinicId = clinicContextService.getEffectiveClinicId(currentUser);

        // 1. Validate clinic
        Clinic clinic = clinicRepository.findById(effectiveClinicId)
                .orElseThrow(() -> new ResourceNotFoundException("العيادة غير موجودة"));

        // 2. Validate patient
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("المريض غير موجود"));

        if (!patient.getClinic().getId().equals(effectiveClinicId)) {
            throw new BadRequestException("المريض لا ينتمي لهذه العيادة");
        }

        // 3. Validate doctor
        User doctor = userRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("الطبيب غير موجود"));

        if (!doctor.getClinic().getId().equals(effectiveClinicId)) {
            throw new BadRequestException("الطبيب لا ينتمي لهذه العيادة");
        }

        // 4. Get current user
        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("المستخدم غير موجود"));

        // **UPDATED: Single comprehensive validation (replaces old validateDoctorScheduleAvailability)**
        // This validates: past dates, working hours, break times, schedule existence, etc.
        validateAppointmentDateTime(doctor, request.getAppointmentDate(), request.getAppointmentTime());

        // 5. Get duration from doctor's schedule
        Integer scheduledDuration;
        try {
            scheduledDuration = doctorScheduleService.getDurationForDoctor(
                    doctor, request.getAppointmentDate()
            );
        } catch (Exception e) {
            logger.error("Failed to get duration for doctor {} on date {}: {}",
                    doctor.getId(), request.getAppointmentDate(), e.getMessage());
            throw new BadRequestException("فشل في الحصول على مدة الموعد من جدول الطبيب. " +
                    "يرجى التأكد من وجود جدول للطبيب في هذا التاريخ.");
        }

        if (scheduledDuration == null || scheduledDuration <= 0) {
            throw new BadRequestException("لم يتم تكوين مدة المواعيد لجدول الطبيب في هذا التاريخ");
        }

        // 6. Check if override is requested
        Integer actualDuration = scheduledDuration;
        boolean isOverridden = false;
        String overrideReason = null;

        if (request.getOverrideDurationMinutes() != null) {
            // Validate override
            if (request.getOverrideReason() == null || request.getOverrideReason().trim().isEmpty()) {
                throw new BadRequestException("سبب تجاوز المدة مطلوب");
            }

            if (request.getOverrideDurationMinutes() < 5 || request.getOverrideDurationMinutes() > 240) {
                throw new BadRequestException("المدة المجاوزة يجب أن تكون بين 5 و 240 دقيقة");
            }

            actualDuration = request.getOverrideDurationMinutes();
            isOverridden = true;
            overrideReason = request.getOverrideReason();

            logger.info("Duration override requested: {} -> {} minutes. Reason: {}",
                    scheduledDuration, actualDuration, overrideReason);
        }

        // 7. Validate no conflicts with existing appointments
        checkForConflicts(doctor, request.getAppointmentDate(),
                request.getAppointmentTime(), actualDuration, null);

        // 8. Create the appointment
        Appointment appointment = new Appointment();
        appointment.setClinic(clinic);
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setAppointmentTime(request.getAppointmentTime());
        appointment.setDurationMinutes(actualDuration);
        appointment.setOriginalDurationMinutes(scheduledDuration);
        appointment.setIsDurationOverridden(isOverridden);
        appointment.setOverrideReason(overrideReason);
        appointment.setAppointmentType(request.getAppointmentType());
        appointment.setChiefComplaint(request.getChiefComplaint());
        appointment.setNotes(request.getNotes());
        appointment.setCreatedBy(user);
        appointment.setStatus(AppointmentStatus.SCHEDULED);

        // 9. Assign token number
        try {
            tokenService.assignTokenToAppointment(appointment);
        } catch (Exception e) {
            logger.error("Failed to assign token to appointment: {}", e.getMessage());
            throw new BadRequestException("فشل في تعيين رقم الرمز للموعد: " + e.getMessage());
        }

        // 10. Validate that all required fields are set
        if (appointment.getDurationMinutes() == null) {
            throw new BadRequestException("مدة الموعد غير محددة");
        }
        if (appointment.getTokenNumber() == null) {
            throw new BadRequestException("رقم الرمز غير محدد");
        }

        // 11. Save appointment
        Appointment savedAppointment = appointmentRepository.save(appointment);
        logger.info("Appointment created successfully: ID={}, Token={}, Duration={}",
                savedAppointment.getId(), savedAppointment.getTokenNumber(),
                savedAppointment.getDurationMinutes());

        return AppointmentResponse.fromAppointment(savedAppointment);
    }

    // **NEW: Add method to override duration for existing appointment**

    @Transactional
    public AppointmentResponse overrideAppointmentDuration(Long clinicId, Long appointmentId,
                                                           OverrideDurationRequest request) {
        Appointment appointment = findAppointmentByIdAndClinic(appointmentId, clinicId);

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BadRequestException("لا يمكن تجاوز مدة موعد ملغى");
        }

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new BadRequestException("لا يمكن تجاوز مدة موعد مكتمل");
        }

        // Validate new duration doesn't conflict with next appointment
        LocalTime endTime = appointment.getAppointmentTime().plusMinutes(request.getNewDurationMinutes());
        List<Appointment> nextAppointments = appointmentRepository.findByDoctorAndAppointmentDate(
                appointment.getDoctor(), appointment.getAppointmentDate()
        );

        for (Appointment next : nextAppointments) {
            if (next.getId().equals(appointment.getId())) continue;
            if (next.getStatus() == AppointmentStatus.CANCELLED) continue;

            if (next.getAppointmentTime().isBefore(endTime) &&
                    next.getAppointmentTime().isAfter(appointment.getAppointmentTime())) {
                throw new BadRequestException(
                        "المدة الجديدة تتعارض مع الموعد التالي في " + next.getAppointmentTime()
                );
            }
        }

        // Apply override
        appointment.setDurationMinutes(request.getNewDurationMinutes());
        appointment.setIsDurationOverridden(true);
        appointment.setOverrideReason(request.getReason());

        Appointment savedAppointment = appointmentRepository.save(appointment);

        logger.info("Duration overridden for appointment {}: {} -> {} minutes. Reason: {}",
                appointmentId, appointment.getOriginalDurationMinutes(),
                request.getNewDurationMinutes(), request.getReason());

        return AppointmentResponse.fromAppointment(savedAppointment);
    }

    /**
     * الحصول على موعد بالمعرف
     */
    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentById(Long clinicId, Long appointmentId) {
        Appointment appointment = findAppointmentByIdAndClinic(appointmentId, clinicId);
        return AppointmentResponse.fromAppointment(appointment);
    }

    /**
     * الحصول على جميع المواعيد مع ترقيم الصفحات والتصفية
     */
    @Transactional(readOnly = true)
    public AppointmentPageResponse getAllAppointments(Long clinicId, LocalDate date, Long doctorId,
                                                      AppointmentStatus status, int page, int size,
                                                      String sortBy, String sortDirection) {
        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new ResourceNotFoundException("العيادة غير موجودة"));

        // إعداد الترتيب
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Appointment> appointmentsPage;

        // التصفية حسب المعايير
        if (date != null && doctorId != null && status != null) {
            // تصفية بالتاريخ والطبيب والحالة
            User doctor = userRepository.findById(doctorId).orElse(null);
            if (doctor == null || !doctor.getClinic().getId().equals(clinicId)) {
                throw new BadRequestException("الطبيب غير موجود في هذه العيادة");
            }
            appointmentsPage = appointmentRepository.findByDoctorAndAppointmentDateAndStatus(doctor, date, status, pageable);
        } else if (date != null && doctorId != null) {
            // تصفية بالتاريخ والطبيب
            User doctor = userRepository.findById(doctorId).orElse(null);
            if (doctor == null || !doctor.getClinic().getId().equals(clinicId)) {
                throw new BadRequestException("الطبيب غير موجود في هذه العيادة");
            }
            appointmentsPage = appointmentRepository.findByDoctorAndAppointmentDate(doctor, date, pageable);
        } else if (date != null) {
            // تصفية بالتاريخ فقط
            appointmentsPage = appointmentRepository.findByClinicAndAppointmentDate(clinic, date, pageable);
        } else if (doctorId != null) {
            // تصفية بالطبيب فقط
            User doctor = userRepository.findById(doctorId).orElse(null);
            if (doctor == null || !doctor.getClinic().getId().equals(clinicId)) {
                throw new BadRequestException("الطبيب غير موجود في هذه العيادة");
            }
            appointmentsPage = appointmentRepository.findByDoctor(doctor, pageable);
        } else if (status != null) {
            // تصفية بالحالة فقط
            appointmentsPage = appointmentRepository.findByClinicAndStatus(clinic, status, pageable);
        } else {
            // جميع المواعيد
            appointmentsPage = appointmentRepository.findByClinic(clinic, pageable);
        }

        List<AppointmentResponse> appointmentSummaries = appointmentsPage.getContent()
                .stream()
                .map(AppointmentResponse::fromAppointment)
                .collect(Collectors.toList());

        return new AppointmentPageResponse(
                appointmentSummaries,
                appointmentsPage.getTotalElements(),
                appointmentsPage.getTotalPages(),
                appointmentsPage.getNumber(),
                appointmentsPage.getSize(),
                appointmentsPage.hasPrevious(),
                appointmentsPage.hasNext()
        );
    }

    /**
     * مواعيد اليوم
     */
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getTodayAppointments(Long clinicId) {
        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new ResourceNotFoundException("العيادة غير موجودة"));

        List<Appointment> todayAppointments = appointmentRepository.findTodayAppointments(clinic);

        return todayAppointments.stream()
                .map(AppointmentResponse::fromAppointment)
                .collect(Collectors.toList());
    }

    /**
     * مواعيد طبيب معين
     */
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getDoctorAppointments(Long clinicId, Long doctorId, LocalDate date) {
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("الطبيب غير موجود"));

        if (!doctor.getClinic().getId().equals(clinicId)) {
            throw new BadRequestException("الطبيب لا ينتمي لهذه العيادة");
        }

        List<Appointment> doctorAppointments;
        if (date != null) {
            doctorAppointments = appointmentRepository.findByDoctorAndAppointmentDate(doctor, date);
        } else {
            doctorAppointments = appointmentRepository.findTodayAppointmentsByDoctor(doctor);
        }

        return doctorAppointments.stream()
                .map(AppointmentResponse::fromAppointment)
                .collect(Collectors.toList());
    }

    /**
     * تحديث موعد
     */
    @Transactional
    public AppointmentResponse updateAppointment(UserPrincipal currentUser, Long appointmentId,
                                                 UpdateAppointmentRequest request) {// Get effective clinic ID - this will throw exception if SYSTEM_ADMIN has no context
        Long effectiveClinicId = clinicContextService.getEffectiveClinicId(currentUser);

        Appointment appointment = findAppointmentByIdAndClinic(appointmentId, effectiveClinicId);

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BadRequestException("لا يمكن تحديث موعد ملغى");
        }

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new BadRequestException("لا يمكن تحديث موعد مكتمل");
        }

        // **Use new validation method**
        if (request.getAppointmentDate() != null || request.getAppointmentTime() != null) {
            LocalDate newDate = request.getAppointmentDate() != null ?
                    request.getAppointmentDate() : appointment.getAppointmentDate();
            LocalTime newTime = request.getAppointmentTime() != null ?
                    request.getAppointmentTime() : appointment.getAppointmentTime();

            // Use the new comprehensive validation
            validateAppointmentDateTime(appointment.getDoctor(), newDate, newTime);

            // Also check for conflicts with other appointments
            if (request.getDurationMinutes() != null) {
                checkForConflicts(appointment.getDoctor(), newDate, newTime,
                        request.getDurationMinutes(), appointmentId);
            }
        }

        // Update fields
        if (request.getAppointmentDate() != null) {
            appointment.setAppointmentDate(request.getAppointmentDate());
        }
        if (request.getAppointmentTime() != null) {
            appointment.setAppointmentTime(request.getAppointmentTime());
        }
        if (request.getDurationMinutes() != null) {
            appointment.setDurationMinutes(request.getDurationMinutes());
        }
        if (request.getAppointmentType() != null) {
            appointment.setAppointmentType(request.getAppointmentType());
        }
        if (request.getChiefComplaint() != null) {
            appointment.setChiefComplaint(request.getChiefComplaint());
        }
        if (request.getNotes() != null) {
            appointment.setNotes(request.getNotes());
        }

        Appointment updatedAppointment = appointmentRepository.save(appointment);
        return AppointmentResponse.fromAppointment(updatedAppointment);
    }

    /**
     * تحديث حالة الموعد
     */
    public AppointmentResponse updateAppointmentStatus(UserPrincipal currentUser, Long appointmentId, AppointmentStatus newStatus) {
        // Get effective clinic ID - this will throw exception if SYSTEM_ADMIN has no context
        Long effectiveClinicId = clinicContextService.getEffectiveClinicId(currentUser);

        logger.info("Updating appointment's status in clinic {} by user {}",
                effectiveClinicId, currentUser.getUsername());

        Appointment appointment = findAppointmentByIdAndClinic(appointmentId, effectiveClinicId);

        // التحقق من صحة انتقال الحالة
        validateStatusTransition(appointment.getStatus(), newStatus);

        appointment.setStatus(newStatus);
        Appointment updatedAppointment = appointmentRepository.save(appointment);
        return AppointmentResponse.fromAppointment(updatedAppointment);
    }

    /**
     * إلغاء موعد
     */
    public void cancelAppointment(Long clinicId, Long appointmentId, String reason) {
        Appointment appointment = findAppointmentByIdAndClinic(appointmentId, clinicId);

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BadRequestException("الموعد ملغى بالفعل");
        }

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new BadRequestException("لا يمكن إلغاء موعد مكتمل");
        }

        // **NEW: Free up the token**
        tokenService.freeTokenFromAppointment(appointment);

        appointment.setStatus(AppointmentStatus.CANCELLED);
        if (StringUtils.hasText(reason)) {
            String currentNotes = appointment.getNotes() != null ? appointment.getNotes() + "\n" : "";
            appointment.setNotes(currentNotes + "سبب الإلغاء: " + reason);
        }

        appointmentRepository.save(appointment);
    }

    /**
     * المواعيد القادمة للمريض
     */
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getUpcomingAppointmentsByPatient(Long clinicId, Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("المريض غير موجود"));

        if (!patient.getClinic().getId().equals(clinicId)) {
            throw new BadRequestException("المريض لا ينتمي لهذه العيادة");
        }

        List<Appointment> upcomingAppointments = appointmentRepository.findUpcomingAppointmentsByPatient(patient);

        return upcomingAppointments.stream()
                .map(AppointmentResponse::fromAppointment)
                .collect(Collectors.toList());
    }

    /**
     * المواعيد المتأخرة
     */
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getOverdueAppointments(Long clinicId) {
        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new ResourceNotFoundException("العيادة غير موجودة"));

        List<Appointment> overdueAppointments = appointmentRepository.findOverdueAppointments(clinic);

        return overdueAppointments.stream()
                .map(AppointmentResponse::fromAppointment)
                .collect(Collectors.toList());
    }

    /**
     * إحصائيات المواعيد
     */
    @Transactional(readOnly = true)
    public AppointmentStatistics getAppointmentStatistics(Long clinicId, LocalDate date) {
        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new ResourceNotFoundException("العيادة غير موجودة"));

        LocalDate targetDate = date != null ? date : LocalDate.now();

        long totalAppointments = appointmentRepository.countAppointmentsByDate(clinic, targetDate);
        long completedAppointments = appointmentRepository.countAppointmentsByDateAndStatus(
                clinic, targetDate, AppointmentStatus.COMPLETED);
        long cancelledAppointments = appointmentRepository.countAppointmentsByDateAndStatus(
                clinic, targetDate, AppointmentStatus.CANCELLED);
        long noShowAppointments = appointmentRepository.countAppointmentsByDateAndStatus(
                clinic, targetDate, AppointmentStatus.NO_SHOW);

        return new AppointmentStatistics(totalAppointments, completedAppointments,
                cancelledAppointments, noShowAppointments, targetDate);
    }

    // =============================================================================
    // Private Helper Methods
    // =============================================================================

    /**
     * البحث عن موعد بالمعرف والعيادة
     */
    private Appointment findAppointmentByIdAndClinic(Long appointmentId, Long clinicId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("الموعد غير موجود"));

        if (!appointment.getClinic().getId().equals(clinicId)) {
            throw new ResourceNotFoundException("الموعد غير موجود في هذه العيادة");
        }

        return appointment;
    }

    /**
     * التحقق من صحة التاريخ والوقت
     */
    /**
     * Validate appointment date and time
     * Now validates against doctor's actual schedule instead of hardcoded hours
     */
    private void validateAppointmentDateTime(User doctor, LocalDate date, LocalTime time) {
        // 1. Check if date is in the past
        if (date.isBefore(LocalDate.now())) {
            throw new BadRequestException("لا يمكن حجز موعد في الماضي");
        }

        // 2. Check if time is in the past (for today's appointments)
        if (date.isEqual(LocalDate.now()) && time.isBefore(LocalTime.now())) {
            throw new BadRequestException("لا يمكن حجز موعد في وقت مضى من اليوم");
        }

        // 3. Get doctor's schedule for this day
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        Optional<DoctorSchedule> scheduleOpt = scheduleRepository.findDoctorScheduleForDay(
                doctor, dayOfWeek, date
        );

        if (scheduleOpt.isEmpty()) {
            throw new BadRequestException(
                    String.format("الطبيب لا يعمل في يوم %s. يرجى اختيار يوم عمل آخر",
                            getDayOfWeekArabic(dayOfWeek))
            );
        }

        DoctorSchedule schedule = scheduleOpt.get();

        // 4. Check if schedule is active
        if (!schedule.getIsActive()) {
            throw new BadRequestException("جدول الطبيب غير نشط في هذا اليوم");
        }

        // 5. Check if time is within working hours
        if (time.isBefore(schedule.getStartTime())) {
            throw new BadRequestException(
                    String.format("الوقت المحدد قبل بداية ساعات عمل الطبيب. ساعات العمل: %s - %s",
                            schedule.getStartTime(), schedule.getEndTime())
            );
        }

        if (time.isAfter(schedule.getEndTime()) || time.equals(schedule.getEndTime())) {
            throw new BadRequestException(
                    String.format("الوقت المحدد بعد نهاية ساعات عمل الطبيب. ساعات العمل: %s - %s",
                            schedule.getStartTime(), schedule.getEndTime())
            );
        }

        // 6. Check if time falls within break time
        if (schedule.getBreakStartTime() != null && schedule.getBreakEndTime() != null) {
            if (!time.isBefore(schedule.getBreakStartTime()) &&
                    time.isBefore(schedule.getBreakEndTime())) {
                throw new BadRequestException(
                        String.format("الوقت المحدد يقع ضمن فترة استراحة الطبيب (%s - %s)",
                                schedule.getBreakStartTime(), schedule.getBreakEndTime())
                );
            }
        }

        // 7. Validate that appointment time + duration doesn't exceed working hours
        Integer duration = schedule.getEffectiveDuration();
        if (duration != null) {
            LocalTime appointmentEndTime = time.plusMinutes(duration);

            if (appointmentEndTime.isAfter(schedule.getEndTime())) {
                throw new BadRequestException(
                        String.format("مدة الموعد (%d دقيقة) تتجاوز ساعات عمل الطبيب. " +
                                        "آخر موعد متاح: %s",
                                duration,
                                schedule.getEndTime().minusMinutes(duration))
                );
            }

            // Check if appointment overlaps with break time
            if (schedule.getBreakStartTime() != null && schedule.getBreakEndTime() != null) {
                if (time.isBefore(schedule.getBreakStartTime()) &&
                        appointmentEndTime.isAfter(schedule.getBreakStartTime())) {
                    throw new BadRequestException(
                            String.format("الموعد يتداخل مع فترة الاستراحة. " +
                                            "يرجى اختيار وقت قبل %s أو بعد %s",
                                    schedule.getBreakStartTime(),
                                    schedule.getBreakEndTime())
                    );
                }
            }
        }

        logger.debug("Appointment time validation passed for doctor {} on {} at {}",
                doctor.getId(), date, time);
    }

    /**
     * Helper method to get day of week in Arabic
     */
    private String getDayOfWeekArabic(DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
            case SUNDAY: return "الأحد";
            case MONDAY: return "الاثنين";
            case TUESDAY: return "الثلاثاء";
            case WEDNESDAY: return "الأربعاء";
            case THURSDAY: return "الخميس";
            case FRIDAY: return "الجمعة";
            case SATURDAY: return "السبت";
            default: return dayOfWeek.name();
        }
    }

    /**
     * NEW: التحقق من توفر الطبيب حسب الجدول الزمني
     * يتحقق من:
     * 1. وجود جدول للطبيب في يوم الموعد
     * 2. أن الموعد يقع ضمن ساعات عمل الطبيب
     * 3. أن الموعد لا يتعارض مع فترة الاستراحة
     */
    /* private void validateDoctorScheduleAvailability(User doctor, LocalDate appointmentDate,
                                                    LocalTime appointmentTime, Integer durationMinutes) {
        // الحصول على يوم الأسبوع من تاريخ الموعد
        DayOfWeek dayOfWeek = appointmentDate.getDayOfWeek();

        // البحث عن جدول الطبيب لهذا اليوم
        Optional<DoctorSchedule> scheduleOpt = doctorScheduleRepository.findDoctorScheduleForDay(
                doctor, dayOfWeek, appointmentDate);

        // التحقق من وجود جدول عمل للطبيب في هذا اليوم
        if (scheduleOpt.isEmpty()) {
            throw new BadRequestException(
                    String.format("الطبيب %s لا يعمل في يوم %s",
                            doctor.getFullName(),
                            getDayNameInArabic(dayOfWeek)));
        }

        DoctorSchedule schedule = scheduleOpt.get();

        // التحقق من أن الموعد يبدأ ضمن ساعات العمل
        if (appointmentTime.isBefore(schedule.getStartTime()) ||
                appointmentTime.isAfter(schedule.getEndTime())) {
            throw new BadRequestException(
                    String.format("الموعد خارج ساعات عمل الطبيب. ساعات العمل: من %s إلى %s",
                            schedule.getStartTime(), schedule.getEndTime()));
        }

        // حساب وقت انتهاء الموعد
        LocalTime appointmentEndTime = appointmentTime.plusMinutes(durationMinutes);

        // التحقق من أن الموعد ينتهي قبل انتهاء ساعات العمل
        if (appointmentEndTime.isAfter(schedule.getEndTime())) {
            throw new BadRequestException(
                    String.format("الموعد ينتهي بعد نهاية ساعات عمل الطبيب (ينتهي العمل في %s)",
                            schedule.getEndTime()));
        }

        // التحقق من عدم التعارض مع فترة الاستراحة
        if (schedule.getBreakStartTime() != null && schedule.getBreakEndTime() != null) {
            LocalTime breakStart = schedule.getBreakStartTime();
            LocalTime breakEnd = schedule.getBreakEndTime();

            // التحقق من أن الموعد لا يتداخل مع فترة الاستراحة
            // الموعد يتداخل إذا:
            // - بدأ قبل نهاية الاستراحة AND انتهى بعد بداية الاستراحة
            boolean overlapsWithBreak = appointmentTime.isBefore(breakEnd) &&
                    appointmentEndTime.isAfter(breakStart);

            if (overlapsWithBreak) {
                throw new BadRequestException(
                        String.format("الموعد يتعارض مع فترة استراحة الطبيب (من %s إلى %s)",
                                breakStart, breakEnd));
            }
        }

        logger.info("Doctor schedule validation passed for doctor {} on {} at {}",
                doctor.getFullName(), appointmentDate, appointmentTime);
    } */

    /**
     * NEW: الحصول على اسم اليوم بالعربية
     */
    /* private String getDayNameInArabic(DayOfWeek dayOfWeek) { // NEW:
        return switch (dayOfWeek) { // NEW:
            case SUNDAY -> "الأحد"; // NEW:
            case MONDAY -> "الاثنين"; // NEW:
            case TUESDAY -> "الثلاثاء"; // NEW:
            case WEDNESDAY -> "الأربعاء"; // NEW:
            case THURSDAY -> "الخميس"; // NEW:
            case FRIDAY -> "الجمعة"; // NEW:
            case SATURDAY -> "السبت"; // NEW:
        }; // NEW:
    }*/ // NEW:


    /**
     * التحقق من تعارض المواعيد
     */
    private void checkForConflicts(User doctor, LocalDate date, LocalTime time,
                                   Integer duration, Long excludeAppointmentId) {

        // الحصول على جميع مواعيد الطبيب في ذلك التاريخ
        List<Appointment> doctorAppointments = appointmentRepository.findDoctorAppointmentsForConflictCheck(doctor, date);

        // استبعاد الموعد الحالي في حالة التحديث
        if (excludeAppointmentId != null) {
            doctorAppointments = doctorAppointments.stream()
                    .filter(apt -> !apt.getId().equals(excludeAppointmentId))
                    .collect(Collectors.toList());
        }

        // حساب وقت انتهاء الموعد الجديد
        LocalTime newEndTime = time.plusMinutes(duration);

        // التحقق من التعارض مع كل موعد موجود
        for (Appointment existingAppointment : doctorAppointments) {
            LocalTime existingStartTime = existingAppointment.getAppointmentTime();
            LocalTime existingEndTime = existingStartTime.plusMinutes(existingAppointment.getDurationMinutes());

            // التحقق من التداخل:
            // الموعد الجديد يبدأ قبل انتهاء الموعد الموجود
            // AND الموعد الجديد ينتهي بعد بداية الموعد الموجود
            boolean hasOverlap = time.isBefore(existingEndTime) && newEndTime.isAfter(existingStartTime);

            if (hasOverlap) {
                throw new ConflictException("يوجد تعارض مع موعد آخر للطبيب من " +
                        existingStartTime + " إلى " + existingEndTime);
            }
        }
    }

    /**
     * التحقق من صحة انتقال الحالة
     */
    private void validateStatusTransition(AppointmentStatus currentStatus, AppointmentStatus newStatus) {
        switch (currentStatus) {
            case SCHEDULED:
                // يمكن الانتقال لأي حالة من SCHEDULED
                break;
            case CONFIRMED:
                // يمكن الانتقال من CONFIRMED إلى IN_PROGRESS, COMPLETED, CANCELLED, NO_SHOW
                if (newStatus == AppointmentStatus.SCHEDULED) {
                    throw new BadRequestException("لا يمكن إرجاع الموعد المؤكد إلى مجدول");
                }
                break;
            case IN_PROGRESS:
                // يمكن الانتقال من IN_PROGRESS إلى COMPLETED فقط
                if (newStatus != AppointmentStatus.COMPLETED) {
                    throw new BadRequestException("الموعد قيد التنفيذ يمكن تحويله إلى مكتمل فقط");
                }
                break;
            case COMPLETED:
                throw new BadRequestException("لا يمكن تغيير حالة الموعد المكتمل");
            case CANCELLED:
            case NO_SHOW:
                throw new BadRequestException("لا يمكن تغيير حالة الموعد الملغي أو غير الحاضر");
        }
    }
}