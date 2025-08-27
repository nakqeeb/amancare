// =============================================================================
// Swagger Configuration - إعدادات Swagger
// =============================================================================

package com.nakqeeb.amancare.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * إعدادات Swagger/OpenAPI للتوثيق التفاعلي
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "نظام إدارة العيادات الطبية",
                description = """
            نظام شامل لإدارة العيادات الطبية يشمل:
            - إدارة المرضى والمواعيد
            - السجلات الطبية والتشخيصات  
            - الفواتير والمدفوعات
            - التقارير والإحصائيات
            
            تم تطويره باستخدام Spring Boot 3 و Angular
            """,
                version = "v1.0.0",
                contact = @Contact(
                        name = "فريق التطوير",
                        email = "developer@clinic-system.com",
                        url = "https://clinic-system.com"
                ),
                license = @License(
                        name = "MIT License",
                        url = "https://opensource.org/licenses/MIT"
                )
        ),
        servers = {
                @Server(
                        url = "http://localhost:8080/api/v1",
                        description = "بيئة التطوير"
                ),
                @Server(
                        url = "https://api.clinic-system.com/v1",
                        description = "بيئة الإنتاج"
                )
        },
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        description = "JWT Authentication",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class SwaggerConfig {
}