// =============================================================================
// Custom Enum Deserializers and Configuration
// Location: src/main/java/com/nakqeeb/amancare/config/
// =============================================================================

// ===================================================================
// File: src/main/java/com/nakqeeb/amancare/config/JacksonConfig.java
// ===================================================================
package com.nakqeeb.amancare.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.nakqeeb.amancare.entity.*;
import com.nakqeeb.amancare.config.deserializer.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Jackson configuration for custom JSON serialization/deserialization
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.build();

        // Configure to be more lenient with enums
        objectMapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Register custom deserializers
        SimpleModule module = new SimpleModule();
        module.addDeserializer(SubscriptionPlan.class, new SubscriptionPlanDeserializer());
        module.addDeserializer(InvoiceStatus.class, new InvoiceStatusDeserializer());
        module.addDeserializer(PaymentMethod.class, new PaymentMethodDeserializer());
        module.addDeserializer(PaymentStatus.class, new PaymentStatusDeserializer());
        module.addDeserializer(ServiceCategory.class, new ServiceCategoryDeserializer());
        module.addDeserializer(Gender.class, new GenderDeserializer());
        module.addDeserializer(BloodType.class, new BloodTypeDeserializer());
        module.addDeserializer(UserRole.class, new UserRoleDeserializer());
        module.addDeserializer(AppointmentStatus.class, new AppointmentStatusDeserializer());

        objectMapper.registerModule(module);

        return objectMapper;
    }
}
