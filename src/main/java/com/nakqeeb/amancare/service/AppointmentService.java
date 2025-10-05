// =============================================================================
// Appointment Service - خدمة إدارة المواعيد
// =============================================================================

package com.nakqeeb.amancare.service;

import com.nakqeeb.amancare.controller.PatientController;
import com.nakqeeb.amancare.dto.request.CreateAppointmentRequest;
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

    /**
     * إنشاء موعد جديد
     */
    public AppointmentResponse createAppointment(UserPrincipal currentUser, Long currentUserId, CreateAppointmentRequest request) {
        // Get effective clinic ID - this will throw exception if SYSTEM_ADMIN has no context
        Long effectiveClinicId = clinicContextService.getEffectiveClinicId(currentUser);

        logger.info("Creating patient in clinic {} by user {}",
                effectiveClinicId, currentUser.getUsername());

        // التحقق من وجود العيادة
        Clinic clinic = clinicRepository.findById(effectiveClinicId)
                .orElseThrow(() -> new ResourceNotFoundException("العيادة غير موجودة"));

        // التحقق من وجود المريض وانتماؤه للعيادة
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("المريض غير موجود"));

        if (!patient.getClinic().getId().equals(effectiveClinicId)) {
            throw new BadRequestException("المريض لا ينتمي لهذه العيادة");
        }

        // التحقق من وجود الطبيب وانتماؤه للعيادة
        User doctor = userRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("الطبيب غير موجود"));

        if (!doctor.getClinic().getId().equals(effectiveClinicId)) {
            throw new BadRequestException("الطبيب لا ينتمي لهذه العيادة");
        }

        if (doctor.getRole() != UserRole.DOCTOR) {
            throw new BadRequestException("المستخدم المحدد ليس طبيباً");
        }

        // التحقق من المستخدم الحالي
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("المستخدم الحالي غير موجود"));

        // التحقق من صحة التاريخ والوقت
        validateAppointmentDateTime(request.getAppointmentDate(), request.getAppointmentTime());

        // التحقق من توفر الطبيب حسب الجدول الزمني
        validateDoctorScheduleAvailability(
                doctor,
                request.getAppointmentDate(),
                request.getAppointmentTime(),
                request.getDurationMinutes()
        );

        // التحقق من عدم تعارض المواعيد
        checkForConflicts(doctor, request.getAppointmentDate(), request.getAppointmentTime(),
                request.getDurationMinutes(), null);

        // إنشاء الموعد
        Appointment appointment = new Appointment();
        appointment.setClinic(clinic);
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setAppointmentTime(request.getAppointmentTime());
        appointment.setDurationMinutes(request.getDurationMinutes());
        appointment.setAppointmentType(request.getAppointmentType());
        appointment.setChiefComplaint(request.getChiefComplaint());
        appointment.setNotes(request.getNotes());
        appointment.setCreatedBy(user);
        appointment.setStatus(AppointmentStatus.SCHEDULED);

        Appointment savedAppointment = appointmentRepository.save(appointment);
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
    public AppointmentResponse updateAppointment(Long clinicId, Long appointmentId, UpdateAppointmentRequest request) {
        Appointment appointment = findAppointmentByIdAndClinic(appointmentId, clinicId);

        // لا يمكن تحديث موعد مكتمل أو ملغي
        if (appointment.getStatus() == AppointmentStatus.COMPLETED ||
                appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BadRequestException("لا يمكن تحديث موعد مكتمل أو ملغي");
        }

        // تحديث التاريخ والوقت إذا تم توفيرهم
        boolean dateTimeChanged = false;
        LocalDate newDate = appointment.getAppointmentDate();
        LocalTime newTime = appointment.getAppointmentTime();
        Integer newDuration = appointment.getDurationMinutes();

        if (request.getAppointmentDate() != null) {
            newDate = request.getAppointmentDate();
            dateTimeChanged = true;
        }

        if (request.getAppointmentTime() != null) {
            newTime = request.getAppointmentTime();
            dateTimeChanged = true;
        }

        if (request.getDurationMinutes() != null) {
            newDuration = request.getDurationMinutes();
            dateTimeChanged = true;
        }

        // التحقق من التاريخ والوقت الجديد إذا تم تغييرهم
        if (dateTimeChanged) {
            validateAppointmentDateTime(newDate, newTime);

            // NEW: التحقق من توفر الطبيب حسب الجدول الزمني
            validateDoctorScheduleAvailability( // NEW:
                    appointment.getDoctor(), // NEW:
                    newDate, // NEW:
                    newTime, // NEW:
                    newDuration // NEW:
            ); // NEW:

            checkForConflicts(appointment.getDoctor(), newDate, newTime, newDuration, appointmentId);

            appointment.setAppointmentDate(newDate);
            appointment.setAppointmentTime(newTime);
            appointment.setDurationMinutes(newDuration);
        }

        // تحديث الحقول الأخرى
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
        logger.info("Appointment updated successfully: {}", appointmentId);

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

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new BadRequestException("لا يمكن إلغاء موعد مكتمل");
        }

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
    private void validateAppointmentDateTime(LocalDate date, LocalTime time) {
        if (date.isBefore(LocalDate.now())) {
            throw new BadRequestException("لا يمكن حجز موعد في الماضي");
        }

        if (date.isEqual(LocalDate.now()) && time.isBefore(LocalTime.now())) {
            throw new BadRequestException("لا يمكن حجز موعد في وقت مضى من اليوم");
        }

        // التحقق من ساعات العمل (يمكن تخصيصها لاحقاً)
        if (time.isBefore(LocalTime.of(8, 0)) || time.isAfter(LocalTime.of(18, 0))) {
            throw new BadRequestException("الموعد خارج ساعات العمل (8:00 ص - 6:00 م)");
        }
    }

    /**
     * NEW: التحقق من توفر الطبيب حسب الجدول الزمني
     * يتحقق من:
     * 1. وجود جدول للطبيب في يوم الموعد
     * 2. أن الموعد يقع ضمن ساعات عمل الطبيب
     * 3. أن الموعد لا يتعارض مع فترة الاستراحة
     */
    private void validateDoctorScheduleAvailability(User doctor, LocalDate appointmentDate, // NEW:
                                                    LocalTime appointmentTime, Integer durationMinutes) { // NEW:
        // NEW: الحصول على يوم الأسبوع من تاريخ الموعد
        DayOfWeek dayOfWeek = appointmentDate.getDayOfWeek(); // NEW:

        // NEW: البحث عن جدول الطبيب لهذا اليوم
        Optional<DoctorSchedule> scheduleOpt = doctorScheduleRepository.findDoctorScheduleForDay( // NEW:
                doctor, dayOfWeek, appointmentDate); // NEW:

        // NEW: التحقق من وجود جدول عمل للطبيب في هذا اليوم
        if (scheduleOpt.isEmpty()) { // NEW:
            throw new BadRequestException( // NEW:
                    String.format("الطبيب %s لا يعمل في يوم %s", // NEW:
                            doctor.getFullName(), // NEW:
                            getDayNameInArabic(dayOfWeek))); // NEW:
        } // NEW:

        DoctorSchedule schedule = scheduleOpt.get(); // NEW:

        // NEW: التحقق من أن الموعد يبدأ ضمن ساعات العمل
        if (appointmentTime.isBefore(schedule.getStartTime()) || // NEW:
                appointmentTime.isAfter(schedule.getEndTime())) { // NEW:
            throw new BadRequestException( // NEW:
                    String.format("الموعد خارج ساعات عمل الطبيب. ساعات العمل: من %s إلى %s", // NEW:
                            schedule.getStartTime(), schedule.getEndTime())); // NEW:
        } // NEW:

        // NEW: حساب وقت انتهاء الموعد
        LocalTime appointmentEndTime = appointmentTime.plusMinutes(durationMinutes); // NEW:

        // NEW: التحقق من أن الموعد ينتهي قبل انتهاء ساعات العمل
        if (appointmentEndTime.isAfter(schedule.getEndTime())) { // NEW:
            throw new BadRequestException( // NEW:
                    String.format("الموعد ينتهي بعد نهاية ساعات عمل الطبيب (ينتهي العمل في %s)", // NEW:
                            schedule.getEndTime())); // NEW:
        } // NEW:

        // NEW: التحقق من عدم التعارض مع فترة الاستراحة
        if (schedule.getBreakStartTime() != null && schedule.getBreakEndTime() != null) { // NEW:
            LocalTime breakStart = schedule.getBreakStartTime(); // NEW:
            LocalTime breakEnd = schedule.getBreakEndTime(); // NEW:

            // NEW: التحقق من أن الموعد لا يتداخل مع فترة الاستراحة
            // الموعد يتداخل إذا:
            // - بدأ قبل نهاية الاستراحة AND انتهى بعد بداية الاستراحة
            boolean overlapsWithBreak = appointmentTime.isBefore(breakEnd) && // NEW:
                    appointmentEndTime.isAfter(breakStart); // NEW:

            if (overlapsWithBreak) { // NEW:
                throw new BadRequestException( // NEW:
                        String.format("الموعد يتعارض مع فترة استراحة الطبيب (من %s إلى %s)", // NEW:
                                breakStart, breakEnd)); // NEW:
            } // NEW:
        } // NEW:

        logger.info("Doctor schedule validation passed for doctor {} on {} at {}", // NEW:
                doctor.getFullName(), appointmentDate, appointmentTime); // NEW:
    } // NEW:

    /**
     * NEW: الحصول على اسم اليوم بالعربية
     */
    private String getDayNameInArabic(DayOfWeek dayOfWeek) { // NEW:
        return switch (dayOfWeek) { // NEW:
            case SUNDAY -> "الأحد"; // NEW:
            case MONDAY -> "الاثنين"; // NEW:
            case TUESDAY -> "الثلاثاء"; // NEW:
            case WEDNESDAY -> "الأربعاء"; // NEW:
            case THURSDAY -> "الخميس"; // NEW:
            case FRIDAY -> "الجمعة"; // NEW:
            case SATURDAY -> "السبت"; // NEW:
        }; // NEW:
    } // NEW:


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