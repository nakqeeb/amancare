// =============================================================================
// Fixed UserService - Solves Hibernate Lazy Loading Issue
// =============================================================================

package com.nakqeeb.amancare.service;

import com.nakqeeb.amancare.entity.Clinic;
import com.nakqeeb.amancare.entity.User;
import com.nakqeeb.amancare.entity.UserRole;
import com.nakqeeb.amancare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * خدمة المستخدمين - حل مشكلة Hibernate Lazy Loading
 * UserService - Fixed Hibernate lazy loading issue
 */
@Service
@Transactional(readOnly = true) // This ensures the Hibernate session stays open
public class UserService {

    @Autowired
    private UserRepository userRepository;

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
}