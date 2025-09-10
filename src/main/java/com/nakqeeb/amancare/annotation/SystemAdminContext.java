package com.nakqeeb.amancare.annotation;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation to add SYSTEM_ADMIN context headers to Swagger
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Parameters({
        @Parameter(
                in = ParameterIn.HEADER,
                name = "X-Acting-Clinic-Id",
                description = "معرف العيادة للعمل نيابة عنها (SYSTEM_ADMIN فقط)",
                required = false,
                schema = @Schema(type = "integer", format = "int64", example = "1")
        ),
        @Parameter(
                in = ParameterIn.HEADER,
                name = "X-Acting-Reason",
                description = "سبب تبديل السياق (SYSTEM_ADMIN فقط)",
                required = false,
                schema = @Schema(type = "string", example = "دعم فني - تذكرة #1234")
        )
})
public @interface SystemAdminContext {
}