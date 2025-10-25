// =============================================================================
// Updated Security Configuration - تحديث إعدادات الأمان مع نموذج الصلاحيات الهجين
// =============================================================================

package com.nakqeeb.amancare.config;

import com.nakqeeb.amancare.security.JwtAuthenticationEntryPoint;
import com.nakqeeb.amancare.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * إعدادات الأمان مع نموذج الصلاحيات الهجين لـ SYSTEM_ADMIN
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        // مسارات عامة
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/docs/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // ===================================================================
                        // Booking appointments by guest - APIs لحجز المواعيد بدون حساب
                        // ===================================================================
                        .requestMatchers("/guest/**").permitAll()
                        .requestMatchers("/public/**").permitAll()

                        // Announcement Management - إدارة الإعلانات
                        .requestMatchers("/admin/announcements/**").hasRole("SYSTEM_ADMIN")

                        // ===================================================================
                        // CLINIC MANAGEMENT - إدارة العيادات
                        // ===================================================================

                        // SYSTEM_ADMIN فقط يمكنه إنشاء وحذف العيادات
                        .requestMatchers(HttpMethod.POST, "/clinics").hasRole("SYSTEM_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/clinics/**").hasRole("SYSTEM_ADMIN")

                        // SYSTEM_ADMIN يمكنه قراءة جميع العيادات، ADMIN يمكنه قراءة عيادته فقط
                        .requestMatchers(HttpMethod.GET, "/clinics/**").hasAnyRole("SYSTEM_ADMIN", "ADMIN")

                        // SYSTEM_ADMIN يمكنه تحديث أي عيادة (مع سياق)، ADMIN يمكنه تحديث عيادته
                        .requestMatchers(HttpMethod.PUT, "/clinics/**").hasAnyRole("SYSTEM_ADMIN", "ADMIN")

                        // ===================================================================
                        // USER MANAGEMENT - إدارة المستخدمين
                        // ===================================================================

                        // القراءة: SYSTEM_ADMIN يمكنه قراءة جميع المستخدمين
                        .requestMatchers(HttpMethod.GET, "/users/**")
                        .hasAnyRole("SYSTEM_ADMIN", "ADMIN", "DOCTOR", "NURSE", "RECEPTIONIST")

                        // الإنشاء: SYSTEM_ADMIN يحتاج سياق، ADMIN يمكنه الإنشاء في عيادته
                        .requestMatchers(HttpMethod.POST, "/users")
                        .hasAnyRole("SYSTEM_ADMIN", "ADMIN")

                        // التحديث والحذف: نفس القواعد
                        .requestMatchers(HttpMethod.PUT, "/users/**")
                        .hasAnyRole("SYSTEM_ADMIN", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/users/**")
                        .hasAnyRole("SYSTEM_ADMIN", "ADMIN")

                        // ===================================================================
                        // PATIENT MANAGEMENT - إدارة المرضى
                        // ===================================================================

                        // القراءة: SYSTEM_ADMIN يمكنه قراءة جميع المرضى
                        .requestMatchers(HttpMethod.GET, "/patients/**")
                        .hasAnyRole("SYSTEM_ADMIN", "ADMIN", "DOCTOR", "NURSE", "RECEPTIONIST")

                        // الإنشاء: SYSTEM_ADMIN يحتاج سياق العيادة
                        .requestMatchers(HttpMethod.POST, "/patients")
                        .hasAnyRole("SYSTEM_ADMIN", "ADMIN", "DOCTOR", "NURSE", "RECEPTIONIST")

                        // التحديث: SYSTEM_ADMIN مع سياق أو باقي الأدوار في عيادتهم
                        .requestMatchers(HttpMethod.PUT, "/patients/**")
                        .hasAnyRole("SYSTEM_ADMIN", "ADMIN", "DOCTOR", "NURSE")

                        // الحذف المؤقت
                        .requestMatchers(HttpMethod.DELETE, "/patients/*")
                        .hasAnyRole("SYSTEM_ADMIN", "ADMIN", "DOCTOR")

                        // الحذف النهائي: SYSTEM_ADMIN فقط
                        .requestMatchers(HttpMethod.DELETE, "/patients/*/permanent")
                        .hasRole("SYSTEM_ADMIN")

                        // إعادة التفعيل
                        .requestMatchers(HttpMethod.POST, "/patients/*/reactivate")
                        .hasAnyRole("SYSTEM_ADMIN", "ADMIN")

                        // ===================================================================
                        // APPOINTMENT MANAGEMENT - إدارة المواعيد
                        // ===================================================================

                        // القراءة: SYSTEM_ADMIN يمكنه قراءة جميع المواعيد
                        .requestMatchers(HttpMethod.GET, "/appointments/**")
                        .hasAnyRole("SYSTEM_ADMIN", "ADMIN", "DOCTOR", "NURSE", "RECEPTIONIST")

                        // الإنشاء: SYSTEM_ADMIN يحتاج سياق
                        .requestMatchers(HttpMethod.POST, "/appointments")
                        .hasAnyRole("SYSTEM_ADMIN", "ADMIN", "DOCTOR", "RECEPTIONIST")

                        // التحديث والإلغاء
                        .requestMatchers(HttpMethod.PUT, "/appointments/**")
                        .hasAnyRole("SYSTEM_ADMIN", "ADMIN", "DOCTOR", "RECEPTIONIST")
                        .requestMatchers(HttpMethod.DELETE, "/appointments/**")
                        .hasAnyRole("SYSTEM_ADMIN", "ADMIN", "DOCTOR")

                        // ===================================================================
                        // SCHEDULE MANAGEMENT - إدارة الجداول
                        // ===================================================================

                        // القراءة: SYSTEM_ADMIN يمكنه قراءة جميع الجداول
                        .requestMatchers(HttpMethod.GET, "/schedules/**")
                        .hasAnyRole("SYSTEM_ADMIN", "ADMIN", "DOCTOR", "NURSE", "RECEPTIONIST")

                        // الإنشاء والتحديث: SYSTEM_ADMIN يحتاج سياق
                        .requestMatchers(HttpMethod.POST, "/schedules")
                        .hasAnyRole("SYSTEM_ADMIN", "ADMIN", "DOCTOR")
                        .requestMatchers(HttpMethod.PUT, "/schedules/**")
                        .hasAnyRole("SYSTEM_ADMIN", "ADMIN", "DOCTOR")
                        .requestMatchers(HttpMethod.DELETE, "/schedules/**")
                        .hasAnyRole("SYSTEM_ADMIN", "ADMIN")

                        // ===================================================================
                        // MEDICAL RECORDS - السجلات الطبية
                        // ===================================================================

                        // القراءة: SYSTEM_ADMIN للدعم والمراجعة
                        .requestMatchers(HttpMethod.GET, "/medical-records/**")
                        .hasAnyRole("SYSTEM_ADMIN", "ADMIN", "DOCTOR", "NURSE")

                        // الإنشاء والتحديث: SYSTEM_ADMIN مع سياق فقط
                        .requestMatchers(HttpMethod.POST, "/medical-records")
                        .hasAnyRole("SYSTEM_ADMIN", "DOCTOR")
                        .requestMatchers(HttpMethod.PUT, "/medical-records/**")
                        .hasAnyRole("SYSTEM_ADMIN", "DOCTOR")

                        // ===================================================================
                        // SYSTEM ADMIN SPECIFIC ENDPOINTS
                        // ===================================================================

                        // سياق العيادة والإجراءات
                        .requestMatchers("/admin/context/**").hasRole("SYSTEM_ADMIN")
                        .requestMatchers("/admin/actions/**").hasRole("SYSTEM_ADMIN")
                        .requestMatchers("/admin/audit/**").hasRole("SYSTEM_ADMIN")
                        .requestMatchers("/admin/activities/**").hasAnyRole("ADMIN", "SYSTEM_ADMIN")

                        // ===================================================================
                        // REPORTS & ANALYTICS - التقارير
                        // ===================================================================

                        // SYSTEM_ADMIN يمكنه الوصول لجميع التقارير
                        .requestMatchers(HttpMethod.GET, "/reports/**")
                        .hasAnyRole("SYSTEM_ADMIN", "ADMIN", "DOCTOR")

                        // أي طلب آخر يحتاج مصادقة
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200", "http://localhost:3000", "https://amancare-frontend.onrender.com"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList("Authorization", "X-Acting-Clinic-Id"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}