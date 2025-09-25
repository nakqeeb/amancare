// =============================================================================
// Fixed UserService - Solves Hibernate Lazy Loading Issue
// =============================================================================

package com.nakqeeb.amancare.service;

import com.nakqeeb.amancare.dto.request.UpdateUserRequest;
import com.nakqeeb.amancare.dto.response.ClinicUserResponse;
import com.nakqeeb.amancare.dto.response.ClinicUserStats;
import com.nakqeeb.amancare.dto.response.UserResponse;
import com.nakqeeb.amancare.entity.Clinic;
import com.nakqeeb.amancare.entity.Patient;
import com.nakqeeb.amancare.entity.User;
import com.nakqeeb.amancare.entity.UserRole;
import com.nakqeeb.amancare.exception.BadRequestException;
import com.nakqeeb.amancare.exception.ForbiddenOperationException;
import com.nakqeeb.amancare.exception.ResourceNotFoundException;
import com.nakqeeb.amancare.exception.UnauthorizedException;
import com.nakqeeb.amancare.repository.ClinicRepository;
import com.nakqeeb.amancare.repository.UserRepository;
import com.nakqeeb.amancare.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * خدمة المستخدمين - حل مشكلة Hibernate Lazy Loading
 * UserService - Fixed Hibernate lazy loading issue
 */
@Service
@Transactional(readOnly = true) // This ensures the Hibernate session stays open
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClinicRepository clinicRepository;

    @Autowired
    private ClinicContextService clinicContextService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * الحصول على جميع الأطباء النشطين في العيادة مع تحميل بيانات العيادة
     * Get all active doctors in the clinic with clinic data eagerly loaded
     */
    @Transactional(readOnly = true) // Explicit transaction to keep session open
    public List<User> getDoctorsByClinic(Long clinicId) {
        // Use the custom query method that eagerly loads clinic data
        return userRepository.findDoctorsWithClinicByClinicId(clinicId);
    }

    /**
     * الحصول على جميع العيادات من خلال مستخدمي ADMIN
     * Get all clinics through ADMIN users
     */
    @Transactional(readOnly = true)
    public List<Clinic> getAllClinicsFromAdmins() {
        return userRepository.findAllClinicsFromAdmins();
    }

    /**
     * الحصول على جميع العيادات النشطة من خلال مستخدمي ADMIN
     * Get all active clinics through ADMIN users
     */
    @Transactional(readOnly = true)
    public List<Clinic> getAllActiveClinicsFromAdmins() {
        return userRepository.findAllClinicsFromAdmins().stream()
                .filter(Clinic::getIsActive)
                .collect(Collectors.toList());
    }

    /**
     * Get all users in the clinic for ADMIN user
     * @param clinicId The clinicId for the current user
     * @param roleFilter Optional role filter (DOCTOR, NURSE, RECEPTIONIST)
     * @param activeOnly Whether to show only active users
     * @return List of clinic users
     */
    @Transactional(readOnly = true)
    public List<ClinicUserResponse> getClinicUsers(Long clinicId, String roleFilter, boolean activeOnly) {
        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new ResourceNotFoundException("العيادة غير موجودة"));

        List<User> users;

        // Apply role filter if provided
        if (roleFilter != null && !roleFilter.isEmpty()) {
            UserRole userRole = validateAndParseRole(roleFilter);

            if (activeOnly) {
                users = userRepository.findByClinicAndRoleAndIsActiveTrue(clinic, userRole);
            } else {
                users = userRepository.findByClinicAndRole(clinic, userRole);
            }
        } else {
            // Get all users with allowed roles only
            List<UserRole> allowedRoles = Arrays.asList(
                    UserRole.DOCTOR,
                    UserRole.NURSE,
                    UserRole.RECEPTIONIST
            );

            if (activeOnly) {
                users = userRepository.findByClinicAndIsActiveTrue(clinic).stream()
                        .filter(user -> allowedRoles.contains(user.getRole()))
                        .sorted((u1, u2) -> {
                            // Sort by role then by name
                            int roleCompare = u1.getRole().compareTo(u2.getRole());
                            if (roleCompare != 0) return roleCompare;
                            return u1.getFirstName().compareTo(u2.getFirstName());
                        })
                        .collect(Collectors.toList());
            } else {
                users = userRepository.findByClinic(clinic).stream()
                        .filter(user -> allowedRoles.contains(user.getRole()))
                        .sorted((u1, u2) -> {
                            int roleCompare = u1.getRole().compareTo(u2.getRole());
                            if (roleCompare != 0) return roleCompare;
                            return u1.getFirstName().compareTo(u2.getFirstName());
                        })
                        .collect(Collectors.toList());
            }
        }

        // Convert to DTOs
        List<ClinicUserResponse> responses = users.stream()
                .map(ClinicUserResponse::fromEntity)
                .collect(Collectors.toList());

        logger.info("Found {} clinic users for clinic ID: {}", responses.size(), clinic.getId());
        return responses;
    }

    /**
     * Get clinic user statistics for ADMIN user
     * @param clinicId The clinicId for the current user
     * @return Statistics about clinic users
     */
    @Transactional(readOnly = true)
    public ClinicUserStats getClinicUserStats(Long clinicId) {
        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new ResourceNotFoundException("العيادة غير موجودة"));

        ClinicUserStats stats = new ClinicUserStats();

        // Calculate statistics
        stats.setTotalUsers(userRepository.countByClinic(clinic));
        stats.setActiveUsers(userRepository.countActiveUsersByClinic(clinic));
        stats.setDoctorsCount(userRepository.countByClinicAndRole(clinic, UserRole.DOCTOR));
        stats.setNursesCount(userRepository.countByClinicAndRole(clinic, UserRole.NURSE));
        stats.setReceptionistsCount(userRepository.countByClinicAndRole(clinic, UserRole.RECEPTIONIST));

        // Calculate active counts for each role
        stats.setActiveDoctorsCount(userRepository.countUsersByClinicAndRole(clinic, UserRole.DOCTOR));
        stats.setActiveNursesCount(userRepository.countUsersByClinicAndRole(clinic, UserRole.NURSE));
        stats.setActiveReceptionistsCount(userRepository.countUsersByClinicAndRole(clinic, UserRole.RECEPTIONIST));

        logger.info("Clinic stats - Total: {}, Active: {}, Doctors: {}, Nurses: {}, Receptionists: {}",
                stats.getTotalUsers(), stats.getActiveUsers(), stats.getDoctorsCount(),
                stats.getNursesCount(), stats.getReceptionistsCount());

        return stats;
    }

    /**
     * Get specific clinic user by ID
     * @param clinicId The clinicId for the current user
     * @param userId The user ID to fetch
     * @return The user details if found and belongs to the same clinic
     */
    @Transactional(readOnly = true)
    public ClinicUserResponse getClinicUserById(Long clinicId, Long userId) {
        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new ResourceNotFoundException("العيادة غير موجودة"));

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("المستخدم المطلوب غير موجود"));

        // Verify the user belongs to the same clinic
        if (!targetUser.getClinic().getId().equals(clinic.getId())) {
            throw new ForbiddenOperationException("لا يمكن الوصول إلى مستخدم من عيادة أخرى");
        }

        // Don't allow viewing other ADMIN or SYSTEM_ADMIN users
        if (targetUser.getRole() == UserRole.ADMIN || targetUser.getRole() == UserRole.SYSTEM_ADMIN) {
            throw new ForbiddenOperationException("لا يمكن عرض بيانات المدراء");
        }

        return ClinicUserResponse.fromEntity(targetUser);
    }

    /**
     * Toggle user active status
     * @param currentUser The authenticated ADMIN user
     * @param userId The user ID to toggle
     * @param isActive The new active status
     * @return Updated user
     */
    @Transactional
    public ClinicUserResponse toggleClinicUserStatus(UserPrincipal currentUser, Long userId, boolean isActive) {
        // Get effective clinic ID - this will throw exception if SYSTEM_ADMIN has no context
        Long effectiveClinicId = clinicContextService.getEffectiveClinicId(currentUser);

        // First validate access
        ClinicUserResponse userResponse = getClinicUserById(effectiveClinicId, userId);

        // التحقق من وجود العيادة
        Clinic clinic = clinicRepository.findById(effectiveClinicId)
                .orElseThrow(() -> new ResourceNotFoundException("العيادة غير موجودة"));

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("المستخدم المطلوب غير موجود"));

        // التأكد من أن المستخدم ينتمي لهذه العيادة
        if (!targetUser.getClinic().getId().equals(effectiveClinicId)) {
            throw new ResourceNotFoundException("المستخدم غير موجود في هذه العيادة");
        }

        User user = userRepository.findById(userId).orElseThrow();
        user.setIsActive(isActive);
        user = userRepository.save(user);

        logger.info("User {} status changed to {} by admin {}",
                userId, isActive ? "active" : "inactive", currentUser.getUsername());

        return ClinicUserResponse.fromEntity(user);
    }

    /**
     * تحديث بيانات المستخدم مع التحكم في الصلاحيات
     * Update user with role-based access control
     *
     * @param userId المعرف الخاص بالمستخدم
     * @param request بيانات التحديث
     * @param currentUserPrincipal المستخدم الحالي
     * @return UserResponse بيانات المستخدم المحدثة
     */
    @Transactional
    public UserResponse updateUser(Long userId, UpdateUserRequest request, UserPrincipal currentUserPrincipal) {
        logger.info("تحديث بيانات المستخدم - المعرف: {}, المستخدم الحالي: {}", userId, currentUserPrincipal.getUsername());

        // التحقق من وجود المستخدم
        User userToUpdate = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("المستخدم غير موجود بالمعرف: " + userId));

        // التحقق من الصلاحيات
        validateUpdatePermissions(userToUpdate, currentUserPrincipal);

        // التحقق من تفرد البريد الإلكتروني إذا تم تغييره
        if (request.getEmail() != null && !request.getEmail().equals(userToUpdate.getEmail())) {
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new BadRequestException("البريد الإلكتروني مستخدم من قبل مستخدم آخر");
            }
        }

        // تطبيق التحديثات
        applyUserUpdates(userToUpdate, request);

        // حفظ التغييرات
        User updatedUser = userRepository.save(userToUpdate);

        logger.info("تم تحديث المستخدم بنجاح - المعرف: {}", userId);

        return UserResponse.fromUser(updatedUser);
    }

    /**
     * التحقق من صلاحيات التحديث حسب القواعد المحددة
     * Validate update permissions based on role rules
     *
     * SYSTEM_ADMIN → can update any user of any role
     * ADMIN (clinic owner) → can update himself or users of his own clinic only
     */
    private void validateUpdatePermissions(User userToUpdate, UserPrincipal currentUser) {
        UserRole currentUserRole = UserRole.valueOf(currentUser.getRole());

        switch (currentUserRole) {
            case SYSTEM_ADMIN:
                // SYSTEM_ADMIN can update any user - no restrictions
                logger.debug("مدير النظام يقوم بتحديث المستخدم: {}", userToUpdate.getUsername());
                break;

            case ADMIN:
                // ADMIN can update himself or users from his clinic only
                Long currentUserClinicId = currentUser.getClinicId();

                // Check if updating himself
                if (currentUser.getId().equals(userToUpdate.getId())) {
                    logger.debug("مدير العيادة يقوم بتحديث بياناته الشخصية");
                    break;
                }

                // Check if the user belongs to his clinic
                if (!userToUpdate.getClinic().getId().equals(currentUserClinicId)) {
                    throw new UnauthorizedException("ليس لديك صلاحية لتحديث مستخدمين من عيادات أخرى");
                }

                logger.debug("مدير العيادة {} يقوم بتحديث مستخدم من عيادته: {}",
                        currentUser.getUsername(), userToUpdate.getUsername());
                break;

            default:
                // Other roles (DOCTOR, NURSE, RECEPTIONIST) cannot update other users
                if (!currentUser.getId().equals(userToUpdate.getId())) {
                    throw new UnauthorizedException("ليس لديك صلاحية لتحديث بيانات مستخدمين آخرين");
                }
                logger.debug("المستخدم {} يقوم بتحديث بياناته الشخصية", currentUser.getUsername());
                break;
        }
    }

    /**
     * تطبيق التحديثات على كيان المستخدم
     * Apply updates to the user entity
     */
    private void applyUserUpdates(User userToUpdate, UpdateUserRequest request) {
        // تحديث البيانات الأساسية
        if (request.getEmail() != null) {
            userToUpdate.setEmail(request.getEmail());
        }

        if (request.getFirstName() != null) {
            userToUpdate.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null) {
            userToUpdate.setLastName(request.getLastName());
        }

        if (request.getPhone() != null) {
            userToUpdate.setPhone(request.getPhone());
        }

        // تحديث الدور (فقط SYSTEM_ADMIN يمكنه تغيير الأدوار)
        if (request.getRole() != null) {
            // Additional validation could be added here for role changes
            userToUpdate.setRole(request.getRole());
        }

        // تحديث التخصص
        if (request.getSpecialization() != null) {
            userToUpdate.setSpecialization(request.getSpecialization());
        }

//        // تحديث رقم الترخيص
//        if (request.getLicenseNumber() != null) {
//            userToUpdate.setLicenseNumber(request.getLicenseNumber());
//        }

        // تحديث حالة التفعيل
        if (request.getIsActive() != null) {
            userToUpdate.setIsActive(request.getIsActive());
        }

        // تحديث كلمة المرور إذا تم توفيرها
        if (request.hasPasswordChange()) {
            String encodedPassword = passwordEncoder.encode(request.getNewPassword());
            userToUpdate.setPasswordHash(encodedPassword);
            logger.debug("تم تحديث كلمة المرور للمستخدم: {}", userToUpdate.getUsername());
        }

        // تحديث الطابع الزمني
        userToUpdate.setUpdatedAt(LocalDateTime.now());
    }

    // =================== HELPER METHODS ===================

    /**
     * Validate and parse role string
     * @param roleStr The role string to validate
     * @return The validated UserRole
     * @throws IllegalArgumentException if role is invalid or not allowed
     */
    private UserRole validateAndParseRole(String roleStr) {
        try {
            UserRole role = UserRole.valueOf(roleStr.toUpperCase());

            // Only allow specific roles to be queried
            if (role == UserRole.SYSTEM_ADMIN || role == UserRole.ADMIN) {
                throw new IllegalArgumentException("لا يمكن عرض مستخدمي النظام أو المدراء");
            }

            return role;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("الدور المحدد غير صحيح: " + roleStr);
        }
    }
}