// =============================================================================
// Doctor Schedule Service - خدمة جدولة الأطباء
// =============================================================================

package com.nakqeeb.amancare.service;

import com.nakqeeb.amancare.dto.request.CreateDoctorScheduleRequest;
import com.nakqeeb.amancare.dto.request.CreateUnavailabilityRequest;
import com.nakqeeb.amancare.entity.*;
import com.nakqeeb.amancare.exception.BadRequestException;
import com.nakqeeb.amancare.exception.ResourceNotFoundException;
import com.nakqeeb.amancare.repository.*;
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

    @Autowired
    private DoctorScheduleRepository scheduleRepository;

    @Autowired
    private DoctorUnavailabilityRepository unavailabilityRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClinicRepository clinicRepository;

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

            DoctorSchedule saved = scheduleRepository.save(schedule);
            createdSchedules.add(saved);
        }

        return createdSchedules;
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