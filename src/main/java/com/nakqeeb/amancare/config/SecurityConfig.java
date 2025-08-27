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

                        // مسارات إدارة المستخدمين (مدير العيادة أو مدير النظام)
                        .requestMatchers(HttpMethod.POST, "/users").hasAnyRole("ADMIN", "SYSTEM_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/users/**").hasAnyRole("ADMIN", "SYSTEM_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/users/**").hasAnyRole("ADMIN", "SYSTEM_ADMIN")

                        // مسارات المرضى (جميع المستخدمين المصرح لهم)
                        .requestMatchers("/patients/**").hasAnyRole("ADMIN", "DOCTOR", "NURSE", "RECEPTIONIST")

                        // مسارات المواعيد
                        .requestMatchers(HttpMethod.GET, "/appointments/**").hasAnyRole("ADMIN", "DOCTOR", "NURSE", "RECEPTIONIST")
                        .requestMatchers(HttpMethod.POST, "/appointments/**").hasAnyRole("ADMIN", "DOCTOR", "RECEPTIONIST")
                        .requestMatchers(HttpMethod.PUT, "/appointments/**").hasAnyRole("ADMIN", "DOCTOR", "RECEPTIONIST")
                        .requestMatchers(HttpMethod.DELETE, "/appointments/**").hasAnyRole("ADMIN", "DOCTOR")

                        // مسارات السجلات الطبية (الأطباء فقط)
                        .requestMatchers("/medical-records/**").hasAnyRole("ADMIN", "DOCTOR")

                        // مسارات الفواتير
                        .requestMatchers(HttpMethod.GET, "/invoices/**").hasAnyRole("ADMIN", "DOCTOR", "RECEPTIONIST")
                        .requestMatchers(HttpMethod.POST, "/invoices/**").hasAnyRole("ADMIN", "RECEPTIONIST")
                        .requestMatchers(HttpMethod.PUT, "/invoices/**").hasAnyRole("ADMIN", "RECEPTIONIST")
                        .requestMatchers(HttpMethod.DELETE, "/invoices/**").hasRole("ADMIN")

                        // مسارات المدفوعات
                        .requestMatchers("/payments/**").hasAnyRole("ADMIN", "RECEPTIONIST")

                        // التقارير (مدير العيادة والأطباء)
                        .requestMatchers("/reports/**").hasAnyRole("ADMIN", "DOCTOR")

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