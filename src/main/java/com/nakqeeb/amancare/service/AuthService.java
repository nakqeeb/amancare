// =============================================================================
// Auth Service - خدمة المصادقة
// =============================================================================

package com.nakqeeb.amancare.service;

import com.nakqeeb.amancare.dto.request.ClinicRegistrationRequest;
import com.nakqeeb.amancare.dto.response.JwtAuthenticationResponse;
import com.nakqeeb.amancare.dto.request.UserCreationRequest;
import com.nakqeeb.amancare.entity.Clinic;
import com.nakqeeb.amancare.entity.User;
import com.nakqeeb.amancare.entity.UserRole;
import com.nakqeeb.amancare.exception.ForbiddenOperationException;
import com.nakqeeb.amancare.repository.ClinicRepository;
import com.nakqeeb.amancare.repository.UserRepository;
import com.nakqeeb.amancare.security.JwtTokenProvider;
import com.nakqeeb.amancare.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * خدمة المصادقة وإدارة الحسابات
 */
@Service
@Transactional
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClinicRepository clinicRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    // تعريف الأدوار المسموح إنشاؤها لكل دور
    private static final Set<UserRole> SYSTEM_ADMIN_CAN_CREATE = Set.of(
            UserRole.SYSTEM_ADMIN,  // فقط مدير النظام يمكنه إنشاء مدير نظام آخر
            UserRole.ADMIN,         // ويمكنه إنشاء مدراء عيادات
            UserRole.DOCTOR,
            UserRole.NURSE,
            UserRole.RECEPTIONIST
    );

    private static final Set<UserRole> CLINIC_ADMIN_CAN_CREATE = Set.of(
            UserRole.DOCTOR,        // مدير العيادة يمكنه إنشاء فريق العمل فقط
            UserRole.NURSE,
            UserRole.RECEPTIONIST
            // ملاحظة: لا يمكنه إنشاء ADMIN أو SYSTEM_ADMIN
    );


    /**
     * تسجيل الدخول
     */
    public JwtAuthenticationResponse login(String usernameOrEmail, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(usernameOrEmail, password)
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        // CHECK IF ACCOUNT IS ACTIVE
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("المستخدم غير موجود"));

        if (!user.getIsActive()) {
            throw new RuntimeException("لم يتم تفعيل حسابك بعد. يرجى التحقق من بريدك الإلكتروني");
        }

        // Update last login timestamp
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        String jwt = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(
                ((UserPrincipal) authentication.getPrincipal()).getId()
        );

        return new JwtAuthenticationResponse(jwt, refreshToken, "Bearer");
    }

    /**
     * تسجيل عيادة جديدة (مع مدير العيادة)
     */
    public User registerClinicWithAdmin(ClinicRegistrationRequest request) {
        // التحقق من عدم وجود اسم المستخدم أو البريد الإلكتروني
        if (userRepository.existsByUsername(request.getAdminUsername())) {
            throw new RuntimeException("اسم المستخدم موجود بالفعل!");
        }

        if (userRepository.existsByEmail(request.getAdminEmail())) {
            throw new RuntimeException("البريد الإلكتروني مستخدم بالفعل!");
        }

        if (clinicRepository.existsByNameIgnoreCase(request.getClinicName())) {
            throw new RuntimeException("اسم العيادة موجود بالفعل!");
        }

        // إنشاء العيادة
        Clinic clinic = new Clinic();
        clinic.setName(request.getClinicName());
        clinic.setDescription(request.getClinicDescription());
        clinic.setAddress(request.getClinicAddress());
        clinic.setPhone(request.getClinicPhone());
        clinic.setEmail(request.getClinicEmail());
        clinic.setSubscriptionStartDate(LocalDate.now());
        clinic.setSubscriptionEndDate(LocalDate.now().plusMonths(1)); // شهر تجريبي
        clinic = clinicRepository.save(clinic);

        // إنشاء مدير العيادة
        User admin = new User();
        admin.setClinic(clinic);
        admin.setUsername(request.getAdminUsername());
        admin.setEmail(request.getAdminEmail());
        admin.setPasswordHash(passwordEncoder.encode(request.getAdminPassword()));
        admin.setFirstName(request.getAdminFirstName());
        admin.setLastName(request.getAdminLastName());
        admin.setPhone(request.getAdminPhone());
        admin.setRole(UserRole.ADMIN);

        return userRepository.save(admin);
    }

    /**
     * إنشاء مستخدم جديد في العيادة
     */
    public User createUser(Long clinicId, UserCreationRequest request, UserPrincipal currentUser) {
        // التحقق من صلاحية إنشاء الدور المطلوب
        validateRoleCreationPermission(currentUser, request.getRole());

        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new RuntimeException("العيادة غير موجودة"));

        // التحقق الإضافي: مدير العيادة يمكنه إنشاء مستخدمين في عيادته فقط
        if (currentUser.getRole().equals("ADMIN") && !currentUser.getClinicId().equals(clinicId)) {
            throw new ForbiddenOperationException("لا يمكنك إنشاء مستخدمين في عيادة أخرى");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("اسم المستخدم موجود بالفعل!");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("البريد الإلكتروني مستخدم بالفعل!");
        }

        User user = new User();
        user.setClinic(clinic);
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());
        user.setSpecialization(request.getSpecialization());

        return userRepository.save(user);
    }

    /**
     * التحقق من صلاحية إنشاء الدور
     */
    private void validateRoleCreationPermission(UserPrincipal currentUser, UserRole roleToCreate) {
        UserRole currentUserRole = UserRole.valueOf(currentUser.getRole());

        switch (currentUserRole) {
            case SYSTEM_ADMIN:
                // مدير النظام يمكنه إنشاء أي دور
                if (!SYSTEM_ADMIN_CAN_CREATE.contains(roleToCreate)) {
                    throw new ForbiddenOperationException("مدير النظام لا يمكنه إنشاء هذا الدور");
                }
                break;

            case ADMIN:
                // مدير العيادة يمكنه إنشاء فريق العمل فقط
                if (!CLINIC_ADMIN_CAN_CREATE.contains(roleToCreate)) {
                    throw new ForbiddenOperationException(
                            "مدير العيادة لا يمكنه إنشاء " + roleToCreate.getArabicName() +
                                    ". يمكنك إنشاء: طبيب، ممرض/ممرضة، موظف استقبال فقط"
                    );
                }
                break;

            case DOCTOR:
            case NURSE:
            case RECEPTIONIST:
                // باقي الأدوار لا يمكنها إنشاء أي مستخدمين
                throw new ForbiddenOperationException("ليس لديك صلاحية لإنشاء مستخدمين جدد");

            default:
                throw new ForbiddenOperationException("دور غير معروف");
        }
    }

    /**
     * تحديث كلمة المرور
     */
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("المستخدم غير موجود"));

        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new RuntimeException("كلمة المرور القديمة غير صحيحة");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * تحديث الرمز المميز
     */
    public JwtAuthenticationResponse refreshToken(String refreshToken) {
        if (tokenProvider.validateToken(refreshToken)) {
            Long userId = tokenProvider.getUserIdFromToken(refreshToken);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("المستخدم غير موجود"));

            UserPrincipal userPrincipal = UserPrincipal.create(user);
            String newAccessToken = tokenProvider.generateToken(
                    new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities())
            );
            String newRefreshToken = tokenProvider.generateRefreshToken(userId);

            return new JwtAuthenticationResponse(newAccessToken, newRefreshToken, "Bearer");
        } else {
            throw new RuntimeException("الرمز المميز منتهي الصلاحية");
        }
    }
}


// --------------------------

// =============================================================================
// DTOs للمصادقة
// =============================================================================

