package com.nakqeeb.amancare.service;

import com.nakqeeb.amancare.entity.Appointment;
import com.nakqeeb.amancare.entity.AppointmentStatus;
import com.nakqeeb.amancare.entity.DoctorSchedule;
import com.nakqeeb.amancare.entity.User;
import com.nakqeeb.amancare.exception.BadRequestException;
import com.nakqeeb.amancare.exception.ResourceNotFoundException;
import com.nakqeeb.amancare.repository.AppointmentRepository;
import com.nakqeeb.amancare.repository.DoctorScheduleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * خدمة إدارة رموز المواعيد (Tokens)
 * Manages appointment token numbers and time slot mapping
 */
@Service
public class AppointmentTokenService {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentTokenService.class);

    @Autowired
    private DoctorScheduleService doctorScheduleService;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private DoctorScheduleRepository scheduleRepository;

    /**
     * Generate available time slots with their corresponding token numbers
     *
     * @param doctor The doctor
     * @param date The appointment date
     * @return Map of time slots to token numbers
     */
    @Transactional(readOnly = true)
    public Map<LocalTime, Integer> generateTimeSlotsWithTokens(User doctor, LocalDate date) {
        // **UPDATED: Get duration from doctor's schedule instead of parameter**
        Integer durationMinutes = doctorScheduleService.getDurationForDoctor(doctor, date);

        // 1. Get doctor's schedule for this day
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        Optional<DoctorSchedule> scheduleOpt = scheduleRepository.findDoctorScheduleForDay(
                doctor, dayOfWeek, date
        );

        if (scheduleOpt.isEmpty()) {
            logger.debug("No schedule found for doctor {} on {}", doctor.getId(), date);
            return new LinkedHashMap<>();
        }

        DoctorSchedule schedule = scheduleOpt.get();

        // 2. Generate all possible time slots
        List<LocalTime> allTimeSlots = generateTimeSlots(
                schedule.getStartTime(),
                schedule.getEndTime(),
                schedule.getBreakStartTime(),
                schedule.getBreakEndTime(),
                durationMinutes
        );

        // 3. Create token mapping (sequential starting from 1)
        Map<LocalTime, Integer> slotsWithTokens = new LinkedHashMap<>();
        int tokenNumber = 1;
        for (LocalTime slot : allTimeSlots) {
            slotsWithTokens.put(slot, tokenNumber++);
        }

        logger.debug("Generated {} time slots with tokens for doctor {} on {}",
                slotsWithTokens.size(), doctor.getId(), date);

        return slotsWithTokens;
    }

    /**
     * Get available time slots with tokens (excluding booked ones)
     *
     * @param doctor The doctor
     * @param date The appointment date
     * @return Map of available time slots to token numbers
     */
    @Transactional(readOnly = true)
    public Map<LocalTime, Integer> getAvailableTimeSlotsWithTokens(User doctor, LocalDate date) {
        // 1. Generate all time slots with tokens
        Map<LocalTime, Integer> allSlots = generateTimeSlotsWithTokens(doctor, date);

        // 2. Get all active (non-cancelled) appointments for this doctor on this date
        List<Appointment> bookedAppointments = appointmentRepository
                .findActiveAppointmentsByDoctorAndDateOrderByToken(doctor, date);

        // 3. Remove booked time slots
        Set<LocalTime> bookedTimes = bookedAppointments.stream()
                .map(Appointment::getAppointmentTime)
                .collect(Collectors.toSet());

        Map<LocalTime, Integer> availableSlots = new LinkedHashMap<>();
        for (Map.Entry<LocalTime, Integer> entry : allSlots.entrySet()) {
            if (!bookedTimes.contains(entry.getKey())) {
                availableSlots.put(entry.getKey(), entry.getValue());
            }
        }

        logger.debug("Found {} available slots out of {} total slots for doctor {} on {}",
                availableSlots.size(), allSlots.size(), doctor.getId(), date);

        return availableSlots;
    }

    /**
     * Get token number for a specific time slot
     *
     * @param doctor The doctor
     * @param date The appointment date
     * @param time The appointment time
     * @return Token number for this slot
     * @throws BadRequestException if the time slot is not valid
     */
    @Transactional(readOnly = true)
    public Integer getTokenNumberForTimeSlot(User doctor, LocalDate date, LocalTime time) {
        Map<LocalTime, Integer> allSlots = generateTimeSlotsWithTokens(doctor, date);

        Integer tokenNumber = allSlots.get(time);
        if (tokenNumber == null) {
            throw new BadRequestException("الوقت المحدد غير متاح في جدول الطبيب");
        }

        return tokenNumber;
    }

    /**
     * Check if a time slot is available for booking
     *
     * @param doctor The doctor
     * @param date The appointment date
     * @param time The appointment time
     * @return true if available, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean isTimeSlotAvailable(User doctor, LocalDate date, LocalTime time) {
        long count = appointmentRepository.countAppointmentsByDoctorDateAndTime(doctor, date, time);
        return count == 0;
    }

    /**
     * Get all booked tokens for a doctor on a specific date
     *
     * @param doctor The doctor
     * @param date The appointment date
     * @return Map of token numbers to appointments
     */
    @Transactional(readOnly = true)
    public Map<Integer, Appointment> getBookedTokens(User doctor, LocalDate date) {
        List<Appointment> appointments = appointmentRepository
                .findActiveAppointmentsByDoctorAndDateOrderByToken(doctor, date);

        return appointments.stream()
                .filter(a -> a.getTokenNumber() != null)
                .collect(Collectors.toMap(
                        Appointment::getTokenNumber,
                        a -> a,
                        (a1, a2) -> a1, // In case of duplicates, keep first
                        LinkedHashMap::new
                ));
    }

    /**
     * Assign token number to an appointment
     * This is called when creating a new appointment
     *
     * @param appointment The appointment to assign token to
     * @param durationMinutes Duration of the appointment
     */
    @Transactional
    public void assignTokenToAppointment(Appointment appointment) {
        User doctor = appointment.getDoctor();
        LocalDate date = appointment.getAppointmentDate();
        LocalTime time = appointment.getAppointmentTime();

        // Get the token number for this time slot
        Integer tokenNumber = getTokenNumberForTimeSlot(doctor, date, time);

        // Verify the slot is not already booked
        if (!isTimeSlotAvailable(doctor, date, time)) {
            throw new BadRequestException("هذا الموعد محجوز بالفعل");
        }

        appointment.setTokenNumber(tokenNumber);
        logger.info("Assigned token {} to appointment {} for doctor {} on {} at {}",
                tokenNumber, appointment.getId(), doctor.getId(), date, time);
    }

    /**
     * Free up a token when an appointment is cancelled
     *
     * @param appointment The cancelled appointment
     */
    @Transactional
    public void freeTokenFromAppointment(Appointment appointment) {
        Integer tokenNumber = appointment.getTokenNumber();
        if (tokenNumber != null) {
            logger.info("Freed token {} from cancelled appointment {} for doctor {} on {}",
                    tokenNumber, appointment.getId(),
                    appointment.getDoctor().getId(), appointment.getAppointmentDate());
            // Token is automatically freed because appointment status is CANCELLED
            // The token becomes available for rebooking
        }
    }

    // =============================================================================
    // Helper Methods
    // =============================================================================

    /**
     * Generate time slots between start and end time, excluding break time
     */
    private List<LocalTime> generateTimeSlots(LocalTime startTime, LocalTime endTime,
                                              LocalTime breakStartTime, LocalTime breakEndTime,
                                              int durationMinutes) {
        List<LocalTime> slots = new ArrayList<>();
        LocalTime currentTime = startTime;

        while (currentTime.plusMinutes(durationMinutes).isBefore(endTime) ||
                currentTime.plusMinutes(durationMinutes).equals(endTime)) {

            // Check if current time is NOT in break time
            boolean isInBreakTime = false;
            if (breakStartTime != null && breakEndTime != null) {
                isInBreakTime = !currentTime.isBefore(breakStartTime) &&
                        currentTime.isBefore(breakEndTime);
            }

            if (!isInBreakTime) {
                slots.add(currentTime);
            }

            currentTime = currentTime.plusMinutes(durationMinutes);
        }

        return slots;
    }
}