// =============================================================================
// WebConverterConfig.java
// Location: /src/main/java/com/nakqeeb/amancare/config/
// =============================================================================

package com.nakqeeb.amancare.config;

import com.nakqeeb.amancare.converter.StringToBloodTypeConverter;
import com.nakqeeb.amancare.converter.StringToGenderConverter;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC Configuration for custom converters
 * Registers enum converters for request parameter binding
 *
 * This configuration class registers custom converters that allow
 * Spring to automatically convert string values from HTTP requests
 * to enum types (BloodType and Gender).
 */
@Configuration
public class WebConverterConfig implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebConverterConfig.class);

    @Autowired
    private StringToBloodTypeConverter bloodTypeConverter;

    @Autowired
    private StringToGenderConverter genderConverter;

    /**
     * Constructor injection (alternative to @Autowired)
     * You can use this instead of field injection if preferred
     */
    // public WebConverterConfig(StringToBloodTypeConverter bloodTypeConverter,
    //                          StringToGenderConverter genderConverter) {
    //     this.bloodTypeConverter = bloodTypeConverter;
    //     this.genderConverter = genderConverter;
    // }

    /**
     * Register custom converters with Spring MVC
     * This method is called automatically by Spring during startup
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        // Register the BloodType converter
        registry.addConverter(bloodTypeConverter);
        logger.info("Registered converter: StringToBloodTypeConverter");

        // Register the Gender converter
        registry.addConverter(genderConverter);
        logger.info("Registered converter: StringToGenderConverter");
    }

    /**
     * Log confirmation that converters are registered
     * This helps with debugging
     */
    @PostConstruct
    public void init() {
        logger.info("WebConverterConfig initialized - Custom converters will be registered");
        logger.info("Supported BloodType formats: O+, O-, A+, A-, B+, B-, AB+, AB-, O_POSITIVE, etc.");
        logger.info("Supported Gender formats: MALE, FEMALE, M, F, ذكر, أنثى");
    }
}