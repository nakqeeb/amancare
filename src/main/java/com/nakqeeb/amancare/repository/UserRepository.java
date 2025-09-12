// =============================================================================
// User Repository - مستودع المستخدمين
// =============================================================================

package com.nakqeeb.amancare.repository;

import com.nakqeeb.amancare.entity.Clinic;
import com.nakqeeb.amancare.entity.User;
import com.nakqeeb.amancare.entity.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * مستودع المستخدمين
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * البحث عن مستخدم حسب اسم المستخدم
     */
    Optional<User> findByUsername(String username);

    /**
     * البحث عن مستخدم حسب البريد الإلكتروني
     */
    Optional<User> findByEmail(String email);

    /**
     * البحث عن مستخدم حسب اسم المستخدم أو البريد الإلكتروني
     */
    @Query("SELECT u FROM User u WHERE u.username = :usernameOrEmail OR u.email = :usernameOrEmail")
    Optional<User> findByUsernameOrEmail(@Param("usernameOrEmail") String usernameOrEmail);

    /**
     * البحث عن المستخدمين حسب العيادة
     */
    List<User> findByClinic(Clinic clinic);

    /**
     * البحث عن المستخدمين النشطين حسب العيادة
     */
    List<User> findByClinicAndIsActiveTrue(Clinic clinic);

    /**
     * البحث عن جميع المستخدمين الذين لديهم دور ADMIN (أصحاب العيادات)
     * Find all users with ADMIN role (clinic owners)
     */
    @Query("SELECT u FROM User u JOIN FETCH u.clinic WHERE u.role = 'ADMIN' AND u.isActive = true")
    List<User> findAllAdminUsersWithClinics();

    /**
     * البحث عن جميع العيادات من خلال المستخدمين الذين لديهم دور ADMIN
     * Find all clinics through users with ADMIN role
     */
    @Query("SELECT DISTINCT u.clinic FROM User u WHERE u.role = 'ADMIN' AND u.isActive = true")
    List<Clinic> findAllClinicsFromAdmins();

    /**
     * البحث عن المستخدمين حسب العيادة والدور
     */
    List<User> findByClinicAndRole(Clinic clinic, UserRole role);

    /**
     * البحث عن المستخدمين النشطين حسب العيادة والدور
     */
    List<User> findByClinicAndRoleAndIsActiveTrue(Clinic clinic, UserRole role);

    // Add only this method to your existing UserRepository interface:
    List<User> findByClinicIdAndRoleAndIsActiveTrue(Long clinicId, UserRole role);

    /**
     * الحصول على الأطباء مع تحميل بيانات العيادة (حل مشكلة Lazy Loading)
     * Get doctors with clinic data eagerly loaded (solves lazy loading issue)
     */
    @Query("SELECT u FROM User u JOIN FETCH u.clinic WHERE u.clinic.id = :clinicId AND u.role = 'DOCTOR' AND u.isActive = true ORDER BY u.firstName, u.lastName")
    List<User> findDoctorsWithClinicByClinicId(@Param("clinicId") Long clinicId);

    /**
     * البحث عن الأطباء في العيادة
     */
    @Query("SELECT u FROM User u WHERE u.clinic = :clinic AND u.role = 'DOCTOR' AND u.isActive = true")
    List<User> findDoctorsByClinic(@Param("clinic") Clinic clinic);

    /**
     * البحث في المستخدمين حسب الاسم (مع دعم البحث الجزئي)
     */
    @Query("SELECT u FROM User u WHERE u.clinic = :clinic AND " +
            "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :name, '%')))")
    Page<User> findByClinicAndNameContaining(@Param("clinic") Clinic clinic,
                                             @Param("name") String name,
                                             Pageable pageable);

    /**
     * التحقق من وجود اسم مستخدم
     */
    boolean existsByUsername(String username);

    /**
     * التحقق من وجود بريد إلكتروني
     */
    boolean existsByEmail(String email);

    /**
     * المستخدمين النشطين في العيادة مع ترقيم الصفحات
     */
    Page<User> findByClinicAndIsActiveTrue(Clinic clinic, Pageable pageable);

    /**
     * إحصائيات المستخدمين
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.clinic = :clinic AND u.isActive = true")
    long countActiveUsersByClinic(@Param("clinic") Clinic clinic);

    @Query("SELECT COUNT(u) FROM User u WHERE u.clinic = :clinic AND u.role = :role AND u.isActive = true")
    long countUsersByClinicAndRole(@Param("clinic") Clinic clinic, @Param("role") UserRole role);
}