package com.nakqeeb.amancare.config;

import com.nakqeeb.amancare.security.JwtAuthenticationEntryPoint;
import com.nakqeeb.amancare.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * إعدادات الأمان الرئيسية للتطبيق
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
                          JwtAuthenticationFilter jwtAuthenticationFilter,
                          UserDetailsService userDetailsService) {
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
    }

    /**
     * إعداد السلسلة الأمنية للتطبيق
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // تعطيل CSRF لأننا نستخدم JWT
                .csrf(AbstractHttpConfigurer::disable)

                // إعداد CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // إعداد إدارة الجلسات (stateless لأننا نستخدم JWT)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // إعداد نقطة دخول الاستثناءات
                .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))

                // إعداد التفويض
                .authorizeHttpRequests(auth -> auth
                        // المسارات العامة (لا تحتاج تفويض)
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/public/**").permitAll()

                        // وثائق Swagger
                        .requestMatchers("/docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()

                        // Health check
                        .requestMatchers("/health").permitAll()

                        // مسارات إدارة العيادات (مدير النظام فقط)
                        .requestMatchers(HttpMethod.POST, "/clinics").hasRole("SYSTEM_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/clinics/**").hasRole("SYSTEM_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/clinics/**").hasAnyRole("SYSTEM_ADMIN", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/clinics/**").hasRole("SYSTEM_ADMIN")

                        // مسارات إدارة المستخدمين - تضمين SYSTEM_ADMIN
                        .requestMatchers(HttpMethod.POST, "/users").hasAnyRole("SYSTEM_ADMIN", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/users/**").hasAnyRole("SYSTEM_ADMIN", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/users/**").hasAnyRole("SYSTEM_ADMIN", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/users/**").hasAnyRole("SYSTEM_ADMIN", "ADMIN", "DOCTOR", "NURSE", "RECEPTIONIST")

                        // مسارات المرضى - تضمين SYSTEM_ADMIN
                        // IMPORTANT: Specific matchers must come BEFORE general matchers
                        .requestMatchers(HttpMethod.DELETE, "/patients/*/permanent").hasRole("SYSTEM_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/patients/*/reactivate").hasAnyRole("SYSTEM_ADMIN", "ADMIN")
                        .requestMatchers("/patients/**").hasAnyRole("SYSTEM_ADMIN", "ADMIN", "DOCTOR", "NURSE", "RECEPTIONIST")

                        // مسارات المواعيد - تضمين SYSTEM_ADMIN
                        .requestMatchers(HttpMethod.GET, "/appointments/**").hasAnyRole("SYSTEM_ADMIN", "ADMIN", "DOCTOR", "NURSE", "RECEPTIONIST")
                        .requestMatchers(HttpMethod.POST, "/appointments/**").hasAnyRole("SYSTEM_ADMIN", "ADMIN", "DOCTOR", "RECEPTIONIST")
                        .requestMatchers(HttpMethod.PUT, "/appointments/**").hasAnyRole("SYSTEM_ADMIN", "ADMIN", "DOCTOR", "RECEPTIONIST")
                        .requestMatchers(HttpMethod.DELETE, "/appointments/**").hasAnyRole("SYSTEM_ADMIN", "ADMIN", "DOCTOR")

                        // مسارات السجلات الطبية - تضمين SYSTEM_ADMIN
                        .requestMatchers("/medical-records/**").hasAnyRole("SYSTEM_ADMIN", "ADMIN", "DOCTOR")

                        // مسارات الفواتير - تضمين SYSTEM_ADMIN
                        .requestMatchers(HttpMethod.GET, "/invoices/**").hasAnyRole("SYSTEM_ADMIN", "ADMIN", "DOCTOR", "RECEPTIONIST")
                        .requestMatchers(HttpMethod.POST, "/invoices/**").hasAnyRole("SYSTEM_ADMIN", "ADMIN", "RECEPTIONIST")
                        .requestMatchers(HttpMethod.PUT, "/invoices/**").hasAnyRole("SYSTEM_ADMIN", "ADMIN", "RECEPTIONIST")
                        .requestMatchers(HttpMethod.DELETE, "/invoices/**").hasAnyRole("SYSTEM_ADMIN", "ADMIN")

                        // مسارات المدفوعات - تضمين SYSTEM_ADMIN
                        .requestMatchers("/payments/**").hasAnyRole("SYSTEM_ADMIN", "ADMIN", "RECEPTIONIST")

                        // التقارير - تضمين SYSTEM_ADMIN
                        .requestMatchers("/reports/**").hasAnyRole("SYSTEM_ADMIN", "ADMIN", "DOCTOR")

                        // Dashboard and Statistics - تضمين SYSTEM_ADMIN
                        .requestMatchers("/dashboard/**").hasAnyRole("SYSTEM_ADMIN", "ADMIN", "DOCTOR", "NURSE", "RECEPTIONIST")
                        .requestMatchers("/statistics/**").hasAnyRole("SYSTEM_ADMIN", "ADMIN", "DOCTOR")

                        // باقي المسارات تحتاج تصريح
                        .anyRequest().authenticated()
                )

                // إعداد مقدم المصادقة
                .authenticationProvider(daoAuthenticationProvider())

                // إضافة JWT filter قبل UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * مشفر كلمات المرور
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * مدير المصادقة
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * مقدم المصادقة DAO
     */
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * إعدادات CORS
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // المصادر المسموحة
        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:*",
                "https://localhost:*",
                "http://127.0.0.1:*"
        ));

        // الطرق المسموحة
        configuration.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));

        // الهيدرز المسموحة
        configuration.setAllowedHeaders(List.of("*"));

        // السماح بإرسال المعلومات الشخصية (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        // مدة الاحتفاظ بنتيجة preflight request
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

// =============================================================================
// ملاحظات مهمة:
// =============================================================================
//
// 1. الأدوار (Roles):
//    - SYSTEM_ADMIN: مدير النظام العام (يمكنه إنشاء عيادات جديدة)
//    - ADMIN: مدير العيادة (يدير عيادة واحدة)
//    - DOCTOR: طبيب (يمكنه رؤية المرضى وإضافة السجلات الطبية)
//    - NURSE: ممرض/ممرضة (يمكنه رؤية المرضى والمواعيد)
//    - RECEPTIONIST: موظف الاستقبال (يدير المواعيد والفواتير)
//
// 2. JWT Authentication:
//    - يتم إرسال الـ token في الهيدر: Authorization: Bearer <token>
//    - الـ tokens تنتهي صلاحيتها خلال 24 ساعة (قابلة للتعديل)
//
// 3. Multi-tenant Security:
//    - كل مستخدم مرتبط بعيادة واحدة فقط
//    - لا يمكن للمستخدم الوصول لبيانات عيادات أخرى
//    - يتم التحقق من clinic_id في كل request
//
// 4. CORS:
//    - مسموح للـ localhost فقط في بيئة التطوير
//    - يجب تعديل الإعدادات للإنتاج
//
// =============================================================================