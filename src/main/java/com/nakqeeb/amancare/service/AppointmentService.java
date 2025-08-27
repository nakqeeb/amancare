// =============================================================================
// Appointment Service - خدمة إدارة المواعيد
// =============================================================================

package com.nakqeeb.amancare.service;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * خدمة إدارة المواعيد
 */
@Service
@Transactional
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClinicRepository clinicRepository;

    /**
     * إنشاء موعد جديد
     */
    public AppointmentResponse createAppointment(Long clinicId, Long currentUserId, CreateAppointmentRequest request) {
        // التحقق من وجود العيادة
        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new ResourceNotFoundException("العيادة غير موجودة"));

        // التحقق من وجود المريض وانتماؤه للعيادة
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("المريض غير موجود"));

        if (!patient.getClinic().getId().equals(clinicId)) {
            throw new BadRequestException("المريض لا ينتمي لهذه العيادة");
        }

        // التحقق من وجود الطبيب وانتماؤه للعيادة
        User doctor = userRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("الطبيب غير موجود"));

        if (!doctor.getClinic().getId().equals(clinicId)) {
            throw new BadRequestException("الطبيب لا ينتمي لهذه العيادة");
        }

        if (doctor.getRole() != UserRole.DOCTOR) {
            throw new BadRequestException("المستخدم المحدد ليس طبيباً");
        }

        // التحقق من المستخدم الحالي
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("المستخدم الحالي غير موجود"));

        // التحقق من صحة التاريخ والوقت
        validateAppointmentDateTime(request.getAppointmentDate(), request.getAppointmentTime());

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
        appointment.setCreatedBy(currentUser);
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

        List<AppointmentSummaryResponse> appointmentSummaries = appointmentsPage.getContent()
                .stream()
                .map(AppointmentSummaryResponse::fromAppointment)
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
    public List<AppointmentSummaryResponse> getTodayAppointments(Long clinicId) {
        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new ResourceNotFoundException("العيادة غير موجودة"));

        List<Appointment> todayAppointments = appointmentRepository.findTodayAppointments(clinic);

        return todayAppointments.stream()
                .map(AppointmentSummaryResponse::fromAppointment)
                .collect(Collectors.toList());
    }

    /**
     * مواعيد طبيب معين
     */
    @Transactional(readOnly = true)
    public List<AppointmentSummaryResponse> getDoctorAppointments(Long clinicId, Long doctorId, LocalDate date) {
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
                .map(AppointmentSummaryResponse::fromAppointment)
                .collect(Collectors.toList());
    }

    /**
     * تحديث موعد
     */
    public AppointmentResponse updateAppointment(Long clinicId, Long appointmentId, UpdateAppointmentRequest request) {
        Appointment appointment = findAppointmentByIdAndClinic(appointmentId, clinicId);

        // التحقق من إمكانية التحديث
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new BadRequestException("لا يمكن تعديل موعد مكتمل");
        }

        // تحديث التاريخ والوقت إذا تم تغييرهما
        if (request.getAppointmentDate() != null || request.getAppointmentTime() != null) {
            LocalDate newDate = request.getAppointmentDate() != null ?
                    request.getAppointmentDate() : appointment.getAppointmentDate();
            LocalTime newTime = request.getAppointmentTime() != null ?
                    request.getAppointmentTime() : appointment.getAppointmentTime();
            Integer newDuration = request.getDurationMinutes() != null ?
                    request.getDurationMinutes() : appointment.getDurationMinutes();

            validateAppointmentDateTime(newDate, newTime);
            checkForConflicts(appointment.getDoctor(), newDate, newTime, newDuration, appointmentId);

            appointment.setAppointmentDate(newDate);
            appointment.setAppointmentTime(newTime);
        }

        // تحديث باقي البيانات
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
    public AppointmentResponse updateAppointmentStatus(Long clinicId, Long appointmentId, AppointmentStatus newStatus) {
        Appointment appointment = findAppointmentByIdAndClinic(appointmentId, clinicId);

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
    public List<AppointmentSummaryResponse> getUpcomingAppointmentsByPatient(Long clinicId, Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("المريض غير موجود"));

        if (!patient.getClinic().getId().equals(clinicId)) {
            throw new BadRequestException("المريض لا ينتمي لهذه العيادة");
        }

        List<Appointment> upcomingAppointments = appointmentRepository.findUpcomingAppointmentsByPatient(patient);

        return upcomingAppointments.stream()
                .map(AppointmentSummaryResponse::fromAppointment)
                .collect(Collectors.toList());
    }

    /**
     * المواعيد المتأخرة
     */
    @Transactional(readOnly = true)
    public List<AppointmentSummaryResponse> getOverdueAppointments(Long clinicId) {
        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new ResourceNotFoundException("العيادة غير موجودة"));

        List<Appointment> overdueAppointments = appointmentRepository.findOverdueAppointments(clinic);

        return overdueAppointments.stream()
                .map(AppointmentSummaryResponse::fromAppointment)
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