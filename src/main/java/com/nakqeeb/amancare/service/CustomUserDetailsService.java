// =============================================================================
// Custom User Details Service - خدمة تفاصيل المستخدم المخصصة
// =============================================================================

package com.nakqeeb.amancare.service;

import com.nakqeeb.amancare.entity.User;
import com.nakqeeb.amancare.repository.UserRepository;
import com.nakqeeb.amancare.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * خدمة تحميل تفاصيل المستخدم للمصادقة
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    /**
     * تحميل المستخدم حسب اسم المستخدم أو البريد الإلكتروني
     */
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail)
                .orElseThrow(() ->
                        new UsernameNotFoundException("المستخدم غير موجود: " + usernameOrEmail)
                );

        return UserPrincipal.create(user);
    }

    /**
     * تحميل المستخدم حسب المعرف - يستخدم في JWT
     */
    @Transactional
    public UserDetails loadUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UsernameNotFoundException("المستخدم غير موجود بالمعرف: " + id)
                );

        return UserPrincipal.create(user);
    }
}