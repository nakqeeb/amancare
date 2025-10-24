// =============================================================================
// Doctor Schedule Service - خدمة جدولة الأطباء
// =============================================================================

package com.nakqeeb.amancare.service;

import com.nakqeeb.amancare.dto.request.CreateDoctorScheduleRequest;
import com.nakqeeb.amancare.dto.request.CreateUnavailabilityRequest;
import com.nakqeeb.amancare.dto.request.UpdateDoctorScheduleRequest;
import com.nakqeeb.amancare.entity.*;
import com.nakqeeb.amancare.exception.BadRequestException;
import com.nakqeeb.amancare.exception.ResourceNotFoundException;
import com.nakqeeb.amancare.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * خدمة جدولة الأطباء
 */
@Service
@Transactional
public class DoctorScheduleService {
    private static final Logger logger = LoggerFactory.getLogger(DoctorScheduleService.class);


    @Autowired
    private DoctorScheduleRepository scheduleRepository;

    @Autowired
    private DoctorUnavailabilityRepository unavailabilityRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClinicRepository clinicRepository;

    @Autowired
    private DurationCalculationService durationCalculationService;

    /**
     * إنشاء جدولة جديدة للطبيب
     */
    public List<DoctorSchedule> createDoctorSchedule(Long clinicId, CreateDoctorScheduleRequest request) {
        // التحقق من وجود الطبيب
        User doctor = userRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("الطبيب غير موجود"));

        if (!doctor.getClinic().getId().equals(clinicId)) {
            throw new BadRequestException("الطبيب لا ينتمي لهذه العيادة");
        }

        if (doctor.getRole() != UserRole.DOCTOR) {
            throw new BadRequestException("المستخدم المحدد ليس طبيباً");
        }

        // التحقق من صحة الأوقات
        validateScheduleTimes(request.getStartTime(), request.getEndTime(),
                request.getBreakStartTime(), request.getBreakEndTime());

        List<DoctorSchedule> createdSchedules = new ArrayList<>();

        // إنشاء جدول لكل يوم عمل
        for (DayOfWeek workingDay : request.getWorkingDays()) {
            // حذف الجداول القديمة لهذا اليوم (إذا كانت موجودة)
            scheduleRepository.deleteByDoctorAndDayOfWeekAndIsActiveTrue(doctor, workingDay);

            // إنشاء جدول جديد
            DoctorSchedule schedule = new DoctorSchedule();
            schedule.setDoctor(doctor);
            schedule.setDayOfWeek(workingDay);
            schedule.setStartTime(request.getStartTime());
            schedule.setEndTime(request.getEndTime());
            schedule.setBreakStartTime(request.getBreakStartTime());
            schedule.setBreakEndTime(request.getBreakEndTime());
            schedule.setEffectiveDate(request.getEffectiveDate());
            schedule.setEndDate(request.getEndDate());
            schedule.setScheduleType(request.getScheduleType());
            schedule.setNotes(request.getNotes());
            schedule.setIsActive(true);

            // **NEW: Set duration configuration**
            schedule.setDurationConfigType(request.getDurationConfigType());
            schedule.setDurationMinutes(request.getDurationMinutes());
            schedule.setTargetTokensPerDay(request.getTargetTokensPerDay());

            // **NEW: Validate and apply duration configuration**
            durationCalculationService.applyDurationConfiguration(schedule);

            validateScheduleTimes(schedule.getStartTime(), schedule.getEndTime(),
                    schedule.getBreakStartTime(), schedule.getBreakEndTime());


            DoctorSchedule saved = scheduleRepository.save(schedule);
            createdSchedules.add(saved);
        }

        return createdSchedules;
    }

    /**
     * Get the configured duration for a doctor on a specific date
     */
    @Transactional(readOnly = true)
    public Integer getDurationForDoctor(User doctor, LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        Optional<DoctorSchedule> scheduleOpt = scheduleRepository.findDoctorScheduleForDay(
                doctor, dayOfWeek, date
        );

        if (scheduleOpt.isEmpty()) {
            throw new ResourceNotFoundException("لا يوجد جدول للطبيب في هذا اليوم");
        }

        DoctorSchedule schedule = scheduleOpt.get();
        Integer effectiveDuration = schedule.getEffectiveDuration();

        if (effectiveDuration == null || effectiveDuration <= 0) {
            throw new BadRequestException("لم يتم تكوين مدة المواعيد لهذا الجدول");
        }

        return effectiveDuration;
    }

    /**
     * إضافة وقت عدم توفر للطبيب
     */
    public DoctorUnavailability addUnavailability(Long clinicId, CreateUnavailabilityRequest request) {
        // التحقق من وجود الطبيب
        User doctor = userRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("الطبيب غير موجود"));

        if (!doctor.getClinic().getId().equals(clinicId)) {
            throw new BadRequestException("الطبيب لا ينتمي لهذه العيادة");
        }

        if (doctor.getRole() != UserRole.DOCTOR) {
            throw new BadRequestException("المستخدم المحدد ليس طبيباً");
        }

        // التحقق من صحة الأوقات
        if (request.getStartTime() != null && request.getEndTime() != null) {
            if (request.getStartTime().isAfter(request.getEndTime())) {
                throw new BadRequestException("وقت البداية يجب أن يكون قبل وقت النهاية");
            }
        }

        DoctorUnavailability unavailability = new DoctorUnavailability();
        unavailability.setDoctor(doctor);
        unavailability.setUnavailableDate(request.getUnavailableDate());
        unavailability.setStartTime(request.getStartTime());
        unavailability.setEndTime(request.getEndTime());
        unavailability.setUnavailabilityType(request.getUnavailabilityType());
        unavailability.setReason(request.getReason());
        unavailability.setIsRecurring(request.getIsRecurring());
        unavailability.setRecurrenceEndDate(request.getRecurrenceEndDate());

        return unavailabilityRepository.save(unavailability);
    }

    /**
     * التحقق من توفر الطبيب في تاريخ ووقت معين
     */
    public boolean isDoctorAvailable(User doctor, LocalDate date, LocalTime time) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        // البحث عن جدول الطبيب لهذا اليوم
        Optional<DoctorSchedule> scheduleOpt = scheduleRepository.findDoctorScheduleForDay(
                doctor, dayOfWeek, date);

        if (scheduleOpt.isEmpty()) {
            return false; // الطبيب لا يعمل في هذا اليوم
        }

        DoctorSchedule schedule = scheduleOpt.get();

        // التحقق من أن الوقت ضمن ساعات العمل
        if (!schedule.isAvailableAt(time)) {
            return false;
        }

        // التحقق من عدم وجود أوقات عدم توفر
        List<DoctorUnavailability> unavailabilities = unavailabilityRepository
                .findByDoctorAndUnavailableDate(doctor, date);

        for (DoctorUnavailability unavailability : unavailabilities) {
            if (unavailability.isUnavailableAt(time)) {
                return false;
            }
        }

        return true;
    }

    /**
     * يسترجع جدول الطبيب بناءً على معرف العيادة ومعرف الجدول.
     */
    @Transactional(readOnly = true)
    public DoctorSchedule getScheduleById(Long clinicId, Long scheduleId) {
        DoctorSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("لم يتم العثور على جدول الطبيب المطلوب."));

        if (!schedule.getDoctor().getClinic().getId().equals(clinicId)) {
            throw new BadRequestException("هذا الجدول لا يتبع العيادة المحددة، يرجى التحقق من البيانات.");
        }

        return schedule;
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

    /*
    الحصول على جداول عمل جميع أطباء العيادة
     */
    public List<DoctorSchedule> getAllDoctorsSchedules(Long clinicId) {
        return scheduleRepository.findByClinicId(clinicId);
    }

    /**
     * الحصول على أوقات عدم التوفر للطبيب
     */
    @Transactional(readOnly = true)
    public List<DoctorUnavailability> getDoctorUnavailability(Long clinicId, Long doctorId,
                                                              LocalDate startDate, LocalDate endDate) {
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("الطبيب غير موجود"));

        if (!doctor.getClinic().getId().equals(clinicId)) {
            throw new BadRequestException("الطبيب لا ينتمي لهذه العيادة");
        }

        return unavailabilityRepository.findByDoctorAndDateRange(doctor, startDate, endDate);
    }

    /**
     * الحصول على الأطباء المتاحين في وقت معين
     */
    @Transactional(readOnly = true)
    public List<User> getAvailableDoctors(Long clinicId, LocalDate date, LocalTime time) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        // الحصول على الأطباء الذين يعملون في هذا الوقت
        List<User> workingDoctors = scheduleRepository.findAvailableDoctors(
                clinicId, dayOfWeek, time, date);

        // تصفية الأطباء غير المتاحين
        return workingDoctors.stream()
                .filter(doctor -> isDoctorAvailable(doctor, date, time))
                .collect(Collectors.toList());
    }

    /**
     * الحصول على الأوقات المتاحة للطبيب في تاريخ معين
     */
    @Transactional(readOnly = true)
    public List<LocalTime> getAvailableTimeSlots(Long clinicId, Long doctorId, LocalDate date, int durationMinutes) {
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("الطبيب غير موجود"));

        if (!doctor.getClinic().getId().equals(clinicId)) {
            throw new BadRequestException("الطبيب لا ينتمي لهذه العيادة");
        }

        DayOfWeek dayOfWeek = date.getDayOfWeek();
        Optional<DoctorSchedule> scheduleOpt = scheduleRepository.findDoctorScheduleForDay(
                doctor, dayOfWeek, date);

        if (scheduleOpt.isEmpty()) {
            return new ArrayList<>(); // لا يعمل في هذا اليوم
        }

        DoctorSchedule schedule = scheduleOpt.get();
        List<LocalTime> availableSlots = new ArrayList<>();

        // إنشاء قائمة الأوقات المتاحة (كل 30 دقيقة مثلاً)
        LocalTime currentTime = schedule.getStartTime();
        while (currentTime.plusMinutes(durationMinutes).isBefore(schedule.getEndTime()) ||
                currentTime.plusMinutes(durationMinutes).equals(schedule.getEndTime())) {

            if (isDoctorAvailable(doctor, date, currentTime)) {
                availableSlots.add(currentTime);
            }

            currentTime = currentTime.plusMinutes(30); // فترات 30 دقيقة
        }

        return availableSlots;
    }

    /**
     * حذف جدولة
     */
    public void deleteSchedule(Long clinicId, Long scheduleId) {
        DoctorSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("الجدول غير موجود"));

        if (!schedule.getDoctor().getClinic().getId().equals(clinicId)) {
            throw new BadRequestException("الجدول لا ينتمي لهذه العيادة");
        }

        scheduleRepository.delete(schedule);
    }

    /**
     * حذف وقت عدم توفر
     */
    public void deleteUnavailability(Long clinicId, Long unavailabilityId) {
        DoctorUnavailability unavailability = unavailabilityRepository.findById(unavailabilityId)
                .orElseThrow(() -> new ResourceNotFoundException("وقت عدم التوفر غير موجود"));

        if (!unavailability.getDoctor().getClinic().getId().equals(clinicId)) {
            throw new BadRequestException("وقت عدم التوفر لا ينتمي لهذه العيادة");
        }

        unavailabilityRepository.delete(unavailability);
    }

    // **COMPLETE UPDATE METHOD**
    @Transactional
    public DoctorSchedule updateDoctorSchedule(Long clinicId, Long scheduleId,
                                               UpdateDoctorScheduleRequest request) {
        // 1. Find and validate schedule
        DoctorSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("الجدول غير موجود"));

        if (!schedule.getDoctor().getClinic().getId().equals(clinicId)) {
            throw new BadRequestException("الجدول لا ينتمي لهذه العيادة");
        }

        // 2. Store original values for comparison
        LocalTime originalStartTime = schedule.getStartTime();
        LocalTime originalEndTime = schedule.getEndTime();
        Integer originalDuration = schedule.getEffectiveDuration();
        DayOfWeek originalDayOfWeek = schedule.getDayOfWeek();

        // 3. Check if there are existing appointments that would be affected
        boolean hasExistingAppointments = checkForExistingAppointments(schedule);
        boolean criticalChanges = false;

        // 4. Update basic time fields if provided
        if (request.getStartTime() != null) {
            if (!request.getStartTime().equals(originalStartTime)) {
                criticalChanges = true;
            }
            schedule.setStartTime(request.getStartTime());
        }

        if (request.getEndTime() != null) {
            if (!request.getEndTime().equals(originalEndTime)) {
                criticalChanges = true;
            }
            schedule.setEndTime(request.getEndTime());
        }

        if (request.getBreakStartTime() != null) {
            schedule.setBreakStartTime(request.getBreakStartTime());
        }

        if (request.getBreakEndTime() != null) {
            schedule.setBreakEndTime(request.getBreakEndTime());
        }

        // 5. Update date range if provided
        if (request.getEffectiveDate() != null) {
            schedule.setEffectiveDate(request.getEffectiveDate());
        }

        if (request.getEndDate() != null) {
            schedule.setEndDate(request.getEndDate());
        }

        // 6. Update other fields
        if (request.getScheduleType() != null) {
            schedule.setScheduleType(request.getScheduleType());
        }

        if (request.getNotes() != null) {
            schedule.setNotes(request.getNotes());
        }

        if (request.getIsActive() != null) {
            schedule.setIsActive(request.getIsActive());
        }

        // 7. Handle duration configuration updates
        boolean durationConfigChanged = false;

        if (request.getDurationConfigType() != null) {
            if (!request.getDurationConfigType().equals(schedule.getDurationConfigType())) {
                durationConfigChanged = true;
                criticalChanges = true;
            }
            schedule.setDurationConfigType(request.getDurationConfigType());
        }

        if (request.getDurationMinutes() != null) {
            if (!request.getDurationMinutes().equals(schedule.getDurationMinutes())) {
                durationConfigChanged = true;
                criticalChanges = true;
            }
            schedule.setDurationMinutes(request.getDurationMinutes());
        }

        if (request.getTargetTokensPerDay() != null) {
            if (!request.getTargetTokensPerDay().equals(schedule.getTargetTokensPerDay())) {
                durationConfigChanged = true;
                criticalChanges = true;
            }
            schedule.setTargetTokensPerDay(request.getTargetTokensPerDay());
        }

        // 8. Validate time constraints
        validateScheduleTimes(schedule.getStartTime(), schedule.getEndTime(),
                schedule.getBreakStartTime(), schedule.getBreakEndTime());

        // 9. Check for overlapping schedules (if dates changed)
        if (request.getEffectiveDate() != null || request.getEndDate() != null) {
            LocalDate checkStartDate = schedule.getEffectiveDate() != null ?
                    schedule.getEffectiveDate() : LocalDate.now();
            LocalDate checkEndDate = schedule.getEndDate() != null ?
                    schedule.getEndDate() : LocalDate.now().plusYears(10);

            boolean hasOverlap = scheduleRepository.hasOverlappingSchedule(
                    schedule.getDoctor(),
                    schedule.getDayOfWeek(),
                    checkStartDate,
                    checkEndDate,
                    scheduleId
            );

            if (hasOverlap) {
                throw new BadRequestException(
                        "يوجد جدول آخر متداخل لنفس الطبيب في نفس اليوم والفترة الزمنية"
                );
            }
        }

        // 10. Reapply duration configuration if changed
        if (durationConfigChanged) {
            durationCalculationService.applyDurationConfiguration(schedule);
        }

        // 11. Check impact on existing appointments
        if (criticalChanges && hasExistingAppointments) {
            Integer newDuration = schedule.getEffectiveDuration();

            // Log warning about existing appointments
            logger.warn(
                    "Schedule {} updated with critical changes. " +
                            "Original duration: {}, New duration: {}. " +
                            "Existing appointments may be affected.",
                    scheduleId, originalDuration, newDuration
            );

            // Get count of affected appointments
            long affectedCount = countAffectedAppointments(schedule);
            if (affectedCount > 0) {
                logger.info(
                        "Schedule update affects {} existing appointments. " +
                                "Appointments will keep their original durations unless manually updated.",
                        affectedCount
                );
            }
        }

        // 12. Save and return
        DoctorSchedule updatedSchedule = scheduleRepository.save(schedule);

        logger.info(
                "Schedule {} updated successfully. Doctor: {}, Day: {}, Duration: {} minutes",
                scheduleId, schedule.getDoctor().getId(),
                schedule.getDayOfWeek(), schedule.getEffectiveDuration()
        );

        return updatedSchedule;
    }

    // **HELPER METHOD: Check for existing appointments**
    private boolean checkForExistingAppointments(DoctorSchedule schedule) {
        // Check if there are any appointments using this schedule
        // For a specific day of week, check upcoming occurrences
        LocalDate today = LocalDate.now();
        LocalDate checkUntil = today.plusMonths(3); // Check next 3 months

        LocalDate checkDate = today;
        while (checkDate.isBefore(checkUntil)) {
            if (checkDate.getDayOfWeek().equals(schedule.getDayOfWeek())) {
                long count = scheduleRepository.countActiveAppointmentsOnDate(
                        schedule.getDoctor(), checkDate
                );
                if (count > 0) {
                    return true;
                }
            }
            checkDate = checkDate.plusDays(1);
        }

        return false;
    }

    // **HELPER METHOD: Count affected appointments**
    private long countAffectedAppointments(DoctorSchedule schedule) {
        LocalDate today = LocalDate.now();
        LocalDate checkUntil = today.plusMonths(3);

        long totalCount = 0;
        LocalDate checkDate = today;
        while (checkDate.isBefore(checkUntil)) {
            if (checkDate.getDayOfWeek().equals(schedule.getDayOfWeek())) {
                long count = scheduleRepository.countActiveAppointmentsOnDate(
                        schedule.getDoctor(), checkDate
                );
                totalCount += count;
            }
            checkDate = checkDate.plusDays(1);
        }

        return totalCount;
    }

    // **NEW METHOD: Batch update schedules for multiple days**
    @Transactional
    public List<DoctorSchedule> batchUpdateSchedules(Long clinicId, Long doctorId,
                                                     UpdateDoctorScheduleRequest request,
                                                     List<DayOfWeek> daysOfWeek) {
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("الطبيب غير موجود"));

        if (!doctor.getClinic().getId().equals(clinicId)) {
            throw new BadRequestException("الطبيب لا ينتمي لهذه العيادة");
        }

        List<DoctorSchedule> updatedSchedules = new ArrayList<>();

        for (DayOfWeek dayOfWeek : daysOfWeek) {
            // Find active schedule for this day
            Optional<DoctorSchedule> scheduleOpt = scheduleRepository
                    .findDoctorScheduleForDay(doctor, dayOfWeek, LocalDate.now());

            if (scheduleOpt.isPresent()) {
                DoctorSchedule schedule = scheduleOpt.get();
                DoctorSchedule updated = updateDoctorSchedule(clinicId, schedule.getId(), request);
                updatedSchedules.add(updated);
            } else {
                logger.warn("No active schedule found for doctor {} on {}", doctorId, dayOfWeek);
            }
        }

        return updatedSchedules;
    }

    // **NEW METHOD: Deactivate schedule**
    @Transactional
    public void deactivateSchedule(Long clinicId, Long scheduleId) {
        DoctorSchedule schedule = getScheduleById(clinicId, scheduleId);

        // Check for future appointments
        boolean hasExistingAppointments = checkForExistingAppointments(schedule);
        if (hasExistingAppointments) {
            throw new BadRequestException(
                    "لا يمكن تعطيل الجدول لأنه يحتوي على مواعيد مستقبلية. " +
                            "يرجى إلغاء أو نقل المواعيد أولاً."
            );
        }

        schedule.setIsActive(false);
        scheduleRepository.save(schedule);

        logger.info("Schedule {} deactivated for doctor {}",
                scheduleId, schedule.getDoctor().getId());
    }

    // **NEW METHOD: Clone schedule to new days**
    @Transactional
    public List<DoctorSchedule> cloneScheduleToNewDays(Long clinicId, Long sourceScheduleId,
                                                       List<DayOfWeek> targetDays) {
        DoctorSchedule sourceSchedule = getScheduleById(clinicId, sourceScheduleId);

        List<DoctorSchedule> newSchedules = new ArrayList<>();

        for (DayOfWeek targetDay : targetDays) {
            // Check if schedule already exists for this day
            Optional<DoctorSchedule> existingOpt = scheduleRepository
                    .findDoctorScheduleForDay(sourceSchedule.getDoctor(), targetDay, LocalDate.now());

            if (existingOpt.isPresent()) {
                logger.warn("Schedule already exists for doctor {} on {}",
                        sourceSchedule.getDoctor().getId(), targetDay);
                continue;
            }

            // Clone the schedule
            DoctorSchedule newSchedule = new DoctorSchedule();
            newSchedule.setDoctor(sourceSchedule.getDoctor());
            newSchedule.setDayOfWeek(targetDay);
            newSchedule.setStartTime(sourceSchedule.getStartTime());
            newSchedule.setEndTime(sourceSchedule.getEndTime());
            newSchedule.setBreakStartTime(sourceSchedule.getBreakStartTime());
            newSchedule.setBreakEndTime(sourceSchedule.getBreakEndTime());
            newSchedule.setEffectiveDate(sourceSchedule.getEffectiveDate());
            newSchedule.setEndDate(sourceSchedule.getEndDate());
            newSchedule.setScheduleType(sourceSchedule.getScheduleType());
            newSchedule.setNotes(sourceSchedule.getNotes() + " (نسخة من جدول " +
                    sourceSchedule.getDayOfWeek() + ")");
            newSchedule.setIsActive(true);

            // Copy duration configuration
            newSchedule.setDurationConfigType(sourceSchedule.getDurationConfigType());
            newSchedule.setDurationMinutes(sourceSchedule.getDurationMinutes());
            newSchedule.setTargetTokensPerDay(sourceSchedule.getTargetTokensPerDay());
            newSchedule.setCalculatedDurationMinutes(sourceSchedule.getCalculatedDurationMinutes());

            newSchedules.add(newSchedule);
        }

        if (!newSchedules.isEmpty()) {
            newSchedules = scheduleRepository.saveAll(newSchedules);
            logger.info("Cloned schedule {} to {} new days",
                    sourceScheduleId, newSchedules.size());
        }

        return newSchedules;
    }

    // =============================================================================
    // Helper Methods
    // =============================================================================

    /**
     * التحقق من صحة أوقات الجدولة
     */
    private void validateScheduleTimes(LocalTime startTime, LocalTime endTime,
                                       LocalTime breakStartTime, LocalTime breakEndTime) {
        if (startTime.isAfter(endTime)) {
            throw new BadRequestException("وقت البداية يجب أن يكون قبل وقت النهاية");
        }

        if (breakStartTime != null && breakEndTime != null) {
            if (breakStartTime.isAfter(breakEndTime)) {
                throw new BadRequestException("وقت بداية الاستراحة يجب أن يكون قبل وقت النهاية");
            }

            if (breakStartTime.isBefore(startTime) || breakEndTime.isAfter(endTime)) {
                throw new BadRequestException("أوقات الاستراحة يجب أن تكون ضمن أوقات العمل");
            }
        }
    }
}