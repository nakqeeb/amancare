// =============================================================================
// Clinic Service - خدمة إدارة العيادات
// src/main/java/com/nakqeeb/amancare/service/ClinicService.java
// =============================================================================

package com.nakqeeb.amancare.service;

import com.nakqeeb.amancare.dto.request.CreateClinicRequest;
import com.nakqeeb.amancare.dto.request.UpdateClinicRequest;
import com.nakqeeb.amancare.dto.request.UpdateSubscriptionRequest;
import com.nakqeeb.amancare.dto.response.*;
import com.nakqeeb.amancare.entity.Clinic;
import com.nakqeeb.amancare.entity.Patient;
import com.nakqeeb.amancare.entity.SubscriptionPlan;
import com.nakqeeb.amancare.entity.User;
import com.nakqeeb.amancare.entity.UserRole;
import com.nakqeeb.amancare.exception.DuplicateResourceException;
import com.nakqeeb.amancare.exception.ForbiddenOperationException;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * خدمة إدارة العيادات
 * Service for managing clinic operations
 */
@Service
@Transactional
public class ClinicService {
    private static final Logger logger = LoggerFactory.getLogger(ClinicService.class);

    @Autowired
    private ClinicRepository clinicRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private ClinicContextService clinicContextService;

    // ===================================================================
    // CREATE OPERATIONS
    // ===================================================================

    /**
     * Create a new clinic (SYSTEM_ADMIN only)
     */
    public ClinicResponse createClinic(CreateClinicRequest request, UserPrincipal currentUser) {
        logger.info("Creating new clinic: {} by user: {}", request.getName(), currentUser.getId());

        // Only SYSTEM_ADMIN can create clinics
        if (!UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
            throw new ForbiddenOperationException("Only SYSTEM_ADMIN can create clinics");
        }

        // Check for duplicate clinic name
        if (clinicRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Clinic with name '" + request.getName() + "' already exists");
        }

        // Create new clinic
        Clinic clinic = new Clinic();
        clinic.setName(request.getName());
        clinic.setDescription(request.getDescription());
        clinic.setAddress(request.getAddress());
        clinic.setPhone(request.getPhone());
        clinic.setEmail(request.getEmail());

        // Set working hours
        if (request.getWorkingHoursStart() != null) {
            clinic.setWorkingHoursStart(LocalTime.parse(request.getWorkingHoursStart()));
        }
        if (request.getWorkingHoursEnd() != null) {
            clinic.setWorkingHoursEnd(LocalTime.parse(request.getWorkingHoursEnd()));
        }
        if (request.getWorkingDays() != null) {
            clinic.setWorkingDays(request.getWorkingDays());
        }

        // Set subscription
        SubscriptionPlan plan = request.getSubscriptionPlan() != null ?
                request.getSubscriptionPlan() : SubscriptionPlan.BASIC;
        clinic.setSubscriptionPlan(plan);
        clinic.setSubscriptionStartDate(LocalDate.now());
        clinic.setSubscriptionEndDate(calculateSubscriptionEndDate(plan));
        clinic.setIsActive(true);

        Clinic savedClinic = clinicRepository.save(clinic);

        // Log the action
        auditLogService.logAction(
                currentUser.getId(),
                AuditLogService.ACTION_CREATE,
                savedClinic.getId(),
                AuditLogService.RESOURCE_CLINIC,
                savedClinic.getId(),
                "Created new clinic: " + savedClinic.getName()
        );

        logger.info("Successfully created clinic with ID: {}", savedClinic.getId());
        return ClinicResponse.fromEntity(savedClinic);
    }

    // ===================================================================
    // READ OPERATIONS
    // ===================================================================

    /**
     * Get all clinics with pagination (SYSTEM_ADMIN only)
     */
    public Page<ClinicResponse> getAllClinics(Pageable pageable, String searchTerm,
                                              Boolean isActive, UserPrincipal currentUser) {
        logger.info("Fetching all clinics - User: {}, Role: {}", currentUser.getId(), currentUser.getRole());

        // Only SYSTEM_ADMIN can view all clinics
        if (!UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
            throw new ForbiddenOperationException("Only SYSTEM_ADMIN can view all clinics");
        }

        Page<Clinic> clinics;

        if (searchTerm != null && !searchTerm.isEmpty()) {
            clinics = clinicRepository.findByNameContainingIgnoreCase(searchTerm, pageable);
        } else if (isActive != null && isActive) {
            clinics = clinicRepository.findByIsActiveTrue(pageable);
        } else {
            clinics = clinicRepository.findAll(pageable);
        }

        return clinics.map(ClinicResponse::fromEntity);
    }

    /**
     * Get clinic by ID
     */
    public ClinicResponse getClinicById(Long id, UserPrincipal currentUser) {
        logger.info("Fetching clinic with ID: {} by user: {}", id, currentUser.getId());

        Clinic clinic = clinicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Clinic not found with id: " + id));

        // Check permissions
        if (!UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole()) &&
                !clinic.getId().equals(currentUser.getClinicId())) {
            throw new ForbiddenOperationException("You don't have permission to view this clinic");
        }

        return ClinicResponse.fromEntity(clinic);
    }

    /**
     * Get clinic statistics
     */
    public ClinicStatisticsResponse getClinicStatistics(Long clinicId, UserPrincipal currentUser) {
        logger.info("Fetching statistics for clinic: {} by user: {}", clinicId, currentUser.getId());

        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new ResourceNotFoundException("Clinic not found with id: " + clinicId));

        // Check permissions
        if (!UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole()) &&
                !clinic.getId().equals(currentUser.getClinicId())) {
            throw new ForbiddenOperationException("You don't have permission to view this clinic's statistics");
        }

        ClinicStatisticsResponse stats = new ClinicStatisticsResponse();
        stats.setClinicId(clinicId);
        stats.setClinicName(clinic.getName());

        // Get counts
        stats.setTotalUsers(userRepository.countByClinic(clinic));
        stats.setTotalPatients(patientRepository.countActivePatientsByClinic(clinic));
        stats.setActivePatients(patientRepository.countActivePatientsByClinic(clinic));
        stats.setTotalAppointments(appointmentRepository.countByClinic(clinic));
        stats.setTodayAppointments(appointmentRepository.countTodayAppointmentsByClinic(
                clinic, LocalDate.now()));

        // Get financial data (handle nulls from repository)
        BigDecimal totalRevenue = invoiceRepository.getTotalRevenueByClinic(clinic);
        stats.setTotalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO);

        BigDecimal monthlyRevenue = invoiceRepository.getMonthlyRevenue(
                clinic, LocalDate.now().getYear(), LocalDate.now().getMonthValue());
        stats.setMonthlyRevenue(monthlyRevenue != null ? monthlyRevenue : BigDecimal.ZERO);

        BigDecimal outstandingBalance = invoiceRepository.getTotalOutstandingAmount(clinic);
        stats.setOutstandingBalance(outstandingBalance != null ? outstandingBalance : BigDecimal.ZERO);

        // Subscription info
        stats.setSubscriptionPlan(clinic.getSubscriptionPlan().name());
        stats.setSubscriptionEndDate(clinic.getSubscriptionEndDate());
        stats.setIsActive(clinic.getIsActive());

        return stats;
    }

    // ===================================================================
    // UPDATE OPERATIONS
    // ===================================================================

    /**
     * Update clinic information
     */
    @Transactional
    public ClinicResponse updateClinic(UpdateClinicRequest request, UserPrincipal currentUser) {
        // Get effective clinic ID - this will throw exception if SYSTEM_ADMIN has no context
        Long effectiveClinicId = clinicContextService.getEffectiveClinicId(currentUser);
        logger.info("Updating clinic with ID: {} by user: {}", effectiveClinicId, currentUser.getId());

        // التحقق من وجود العيادة
        Clinic clinic = clinicRepository.findById(effectiveClinicId)
                .orElseThrow(() -> new ResourceNotFoundException("Clinic not found with id: " + effectiveClinicId));

        // Check permissions - SYSTEM_ADMIN or ADMIN of the same clinic
        boolean isSystemAdmin = UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole());
        boolean isClinicAdmin = UserRole.ADMIN.name().equals(currentUser.getRole()) &&
                clinic.getId().equals(currentUser.getClinicId());

        if (!isSystemAdmin && !isClinicAdmin) {
            throw new ForbiddenOperationException("You don't have permission to update this clinic");
        }

        // Check for duplicate name if changing
        if (request.getName() != null && !request.getName().equals(clinic.getName())) {
            if (clinicRepository.existsByNameIgnoreCase(request.getName())) {
                throw new DuplicateResourceException("Clinic with name '" + request.getName() + "' already exists");
            }
            clinic.setName(request.getName());
        }

        // Update fields
        if (request.getDescription() != null) {
            clinic.setDescription(request.getDescription());
        }
        if (request.getAddress() != null) {
            clinic.setAddress(request.getAddress());
        }
        if (request.getPhone() != null) {
            clinic.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            clinic.setEmail(request.getEmail());
        }
        if (request.getWorkingHoursStart() != null) {
            clinic.setWorkingHoursStart(LocalTime.parse(request.getWorkingHoursStart()));
        }
        if (request.getWorkingHoursEnd() != null) {
            clinic.setWorkingHoursEnd(LocalTime.parse(request.getWorkingHoursEnd()));
        }
        if (request.getWorkingDays() != null) {
            clinic.setWorkingDays(request.getWorkingDays());
        }

        Clinic updatedClinic = clinicRepository.save(clinic);

        // Log the action
        auditLogService.logAction(
                currentUser.getId(),
                AuditLogService.ACTION_UPDATE,
                updatedClinic.getId(),
                AuditLogService.RESOURCE_CLINIC,
                updatedClinic.getId(),
                "Updated clinic: " + updatedClinic.getName()
        );

        logger.info("Successfully updated clinic with ID: {}", updatedClinic.getId());
        return ClinicResponse.fromEntity(updatedClinic);
    }

    /**
     * Update clinic subscription (SYSTEM_ADMIN only)
     */
    @Transactional
    public ClinicResponse updateSubscription(Long id, UpdateSubscriptionRequest request,
                                             UserPrincipal currentUser) {
        logger.info("Updating subscription for clinic: {} by user: {}", id, currentUser.getId());

        // Only SYSTEM_ADMIN can update subscriptions
        if (!UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
            throw new ForbiddenOperationException("Only SYSTEM_ADMIN can update subscriptions");
        }

        Clinic clinic = clinicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Clinic not found with id: " + id));

        SubscriptionPlan oldPlan = clinic.getSubscriptionPlan();
        clinic.setSubscriptionPlan(request.getSubscriptionPlan());

        if (request.getSubscriptionStartDate() != null) {
            clinic.setSubscriptionStartDate(LocalDate.parse(request.getSubscriptionStartDate()));
        }

        if (request.getSubscriptionEndDate() != null) {
            clinic.setSubscriptionEndDate(LocalDate.parse(request.getSubscriptionEndDate()));
        } else {
            clinic.setSubscriptionEndDate(calculateSubscriptionEndDate(request.getSubscriptionPlan()));
        }

        Clinic updatedClinic = clinicRepository.save(clinic);

        // Log the action
        auditLogService.logAction(
                currentUser.getId(),
                AuditLogService.ACTION_UPDATE,
                updatedClinic.getId(),
                AuditLogService.RESOURCE_CLINIC,
                updatedClinic.getId(),
                String.format("Updated subscription from %s to %s", oldPlan, request.getSubscriptionPlan())
        );

        logger.info("Successfully updated subscription for clinic: {}", id);
        return ClinicResponse.fromEntity(updatedClinic);
    }

    /**
     * Activate clinic (SYSTEM_ADMIN only)
     */
    @Transactional
    public ClinicResponse activateClinic(Long id, UserPrincipal currentUser) {
        logger.info("Activating clinic: {} by user: {}", id, currentUser.getId());

        // Only SYSTEM_ADMIN can activate clinics
        if (!UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
            throw new ForbiddenOperationException("Only SYSTEM_ADMIN can activate clinics");
        }

        Clinic clinic = clinicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Clinic not found with id: " + id));

        if (clinic.getIsActive()) {
            logger.info("Clinic {} is already active", id);
            return ClinicResponse.fromEntity(clinic);
        }

        clinic.setIsActive(true);
        Clinic updatedClinic = clinicRepository.save(clinic);

        // Log the action
        auditLogService.logAction(
                currentUser.getId(),
                AuditLogService.ACTION_ACTIVATE,
                updatedClinic.getId(),
                AuditLogService.RESOURCE_CLINIC,
                updatedClinic.getId(),
                "Activated clinic: " + updatedClinic.getName()
        );

        logger.info("Successfully activated clinic: {}", id);
        return ClinicResponse.fromEntity(updatedClinic);
    }

    /**
     * Deactivate clinic (SYSTEM_ADMIN only)
     */
    @Transactional
    public ClinicResponse deactivateClinic(Long id, String reason, UserPrincipal currentUser) {
        logger.info("Deactivating clinic: {} by user: {} - Reason: {}", id, currentUser.getId(), reason);

        // Only SYSTEM_ADMIN can deactivate clinics
        if (!UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
            throw new ForbiddenOperationException("Only SYSTEM_ADMIN can deactivate clinics");
        }

        Clinic clinic = clinicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Clinic not found with id: " + id));

        if (!clinic.getIsActive()) {
            logger.info("Clinic {} is already inactive", id);
            return ClinicResponse.fromEntity(clinic);
        }

        clinic.setIsActive(false);
        Clinic updatedClinic = clinicRepository.save(clinic);

        // Log the action with reason
        auditLogService.logAction(
                currentUser.getId(),
                AuditLogService.ACTION_DEACTIVATE,
                updatedClinic.getId(),
                AuditLogService.RESOURCE_CLINIC,
                updatedClinic.getId(),
                "Deactivated clinic: " + updatedClinic.getName() + " - Reason: " + reason
        );

        logger.info("Successfully deactivated clinic: {}", id);
        return ClinicResponse.fromEntity(updatedClinic);
    }

    // ===================================================================
    // DELETE OPERATIONS
    // ===================================================================

    /**
     * Delete clinic (SYSTEM_ADMIN only)
     * This is a soft delete - clinic is deactivated, not removed from database
     */
    @Transactional
    public void deleteClinic(Long id, String reason, UserPrincipal currentUser) {
        logger.info("Deleting clinic: {} by user: {} - Reason: {}", id, currentUser.getId(), reason);

        // Only SYSTEM_ADMIN can delete clinics
        if (!UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
            throw new ForbiddenOperationException("Only SYSTEM_ADMIN can delete clinics");
        }

        Clinic clinic = clinicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Clinic not found with id: " + id));

        // Check if clinic has active data
        long activeUsers = userRepository.countByClinic(clinic);
        long activePatients = patientRepository.countActivePatientsByClinic(clinic);

        if (activeUsers > 0 || activePatients > 0) {
            throw new ForbiddenOperationException(
                    String.format("Cannot delete clinic with active data. Users: %d, Patients: %d",
                            activeUsers, activePatients)
            );
        }

        // Soft delete - just deactivate
        clinic.setIsActive(false);
        clinicRepository.save(clinic);

        // Log the action
        auditLogService.logAction(
                currentUser.getId(),
                AuditLogService.ACTION_DELETE,
                clinic.getId(),
                AuditLogService.RESOURCE_CLINIC,
                clinic.getId(),
                "Deleted clinic: " + clinic.getName() + " - Reason: " + reason
        );

        logger.info("Successfully deleted (deactivated) clinic: {}", id);
    }

    // ===================================================================
    // HELPER METHODS
    // ===================================================================

    /**
     * Calculate subscription end date based on plan
     */
    private LocalDate calculateSubscriptionEndDate(SubscriptionPlan plan) {
        LocalDate startDate = LocalDate.now();
        return switch (plan) {
            case BASIC -> startDate.plusMonths(1);
            case PREMIUM -> startDate.plusMonths(6);
            case ENTERPRISE -> startDate.plusYears(1);
        };
    }

    /**
     * Get clinics with expiring subscriptions
     */
    public List<ClinicResponse> getClinicsWithExpiringSoonSubscriptions(int daysAhead) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(daysAhead);

        List<Clinic> clinics = clinicRepository.findClinicsWithExpiringSoonSubscription(startDate, endDate);
        return clinics.stream()
                .map(ClinicResponse::fromEntity)
                .collect(Collectors.toList());
    }
}