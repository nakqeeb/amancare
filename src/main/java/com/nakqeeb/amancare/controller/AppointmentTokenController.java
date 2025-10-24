package com.nakqeeb.amancare.controller;

import com.nakqeeb.amancare.dto.response.ApiResponse;
import com.nakqeeb.amancare.entity.User;
import com.nakqeeb.amancare.exception.ResourceNotFoundException;
import com.nakqeeb.amancare.repository.UserRepository;
import com.nakqeeb.amancare.security.UserPrincipal;
import com.nakqeeb.amancare.service.AppointmentTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Token Management Controller
 * Endpoints for managing appointment tokens and time slots
 */
@RestController
@RequestMapping("/appointments/tokens")
@Tag(name = "Appointment Tokens", description = "إدارة رموز المواعيد والأوقات المتاحة")
public class AppointmentTokenController {

    @Autowired
    private AppointmentTokenService tokenService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Get all time slots with their token numbers for a doctor on a specific date
     */
    @GetMapping("/doctor/{doctorId}/slots")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST')")
    @Operation(summary = "الحصول على جميع الفترات الزمنية مع أرقام الرموز",
            description = "عرض جميع الفترات الزمنية المتاحة وأرقام الرموز المقابلة لها")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> getAllTimeSlotsWithTokens(
            @Parameter(description = "معرف الطبيب", required = true)
            @PathVariable Long doctorId,
            @Parameter(description = "التاريخ", required = true, example = "2025-01-15")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("الطبيب غير موجود"));

        Map<LocalTime, Integer> slotsWithTokens = tokenService.generateTimeSlotsWithTokens(
                doctor, date
        );

        // Convert LocalTime keys to String for JSON serialization
        Map<String, Integer> result = new LinkedHashMap<>();
        slotsWithTokens.forEach((time, token) -> result.put(time.toString(), token));

        return ResponseEntity.ok(
                new ApiResponse<>(true, "تم الحصول على الفترات الزمنية بنجاح", result)
        );
    }

    /**
     * Get available time slots with their token numbers (excluding booked slots)
     */
    @GetMapping("/doctor/{doctorId}/available-slots")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST')")
    @Operation(summary = "الحصول على الفترات المتاحة مع أرقام الرموز",
            description = "عرض الفترات الزمنية المتاحة فقط (غير المحجوزة) مع أرقام الرموز")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> getAvailableTimeSlotsWithTokens(
            @Parameter(description = "معرف الطبيب", required = true)
            @PathVariable Long doctorId,
            @Parameter(description = "التاريخ", required = true, example = "2025-01-15")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "مدة الموعد بالدقائق", example = "30")
            @AuthenticationPrincipal UserPrincipal currentUser) {

        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("الطبيب غير موجود"));

        Map<LocalTime, Integer> availableSlots = tokenService.getAvailableTimeSlotsWithTokens(
                doctor, date
        );

        // Convert LocalTime keys to String for JSON serialization
        Map<String, Integer> result = new LinkedHashMap<>();
        availableSlots.forEach((time, token) -> result.put(time.toString(), token));

        return ResponseEntity.ok(
                new ApiResponse<>(true, "تم الحصول على الفترات المتاحة بنجاح", result)
        );
    }

    /**
     * Get token number for a specific time slot
     */
    @GetMapping("/doctor/{doctorId}/token")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST')")
    @Operation(summary = "الحصول على رقم الرمز لوقت محدد",
            description = "الحصول على رقم الرمز المقابل لوقت موعد محدد")
    public ResponseEntity<ApiResponse<Integer>> getTokenForTimeSlot(
            @Parameter(description = "معرف الطبيب", required = true)
            @PathVariable Long doctorId,
            @Parameter(description = "التاريخ", required = true, example = "2025-01-15")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "الوقت", required = true, example = "10:30:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("الطبيب غير موجود"));

        Integer tokenNumber = tokenService.getTokenNumberForTimeSlot(
                doctor, date, time
        );

        return ResponseEntity.ok(
                new ApiResponse<>(true, "تم الحصول على رقم الرمز بنجاح", tokenNumber)
        );
    }
}