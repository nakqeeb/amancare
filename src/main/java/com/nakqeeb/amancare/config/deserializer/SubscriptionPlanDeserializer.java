package com.nakqeeb.amancare.config.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.nakqeeb.amancare.entity.SubscriptionPlan;
import com.nakqeeb.amancare.exception.BadRequestException;

import java.io.IOException;

/**
 * Custom deserializer for SubscriptionPlan enum
 * Handles case-insensitive parsing and provides clear error messages
 */
public class SubscriptionPlanDeserializer extends JsonDeserializer<SubscriptionPlan> {

    @Override
    public SubscriptionPlan deserialize(JsonParser parser, DeserializationContext context)
            throws IOException {
        String value = parser.getText();

        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        // Try to match case-insensitively
        String normalizedValue = value.trim().toUpperCase();

        try {
            return SubscriptionPlan.valueOf(normalizedValue);
        } catch (IllegalArgumentException e) {
            // Provide helpful error message with valid values
            String validValues = String.join(", ",
                    java.util.Arrays.stream(SubscriptionPlan.values())
                            .map(Enum::name)
                            .toArray(String[]::new));

            throw new BadRequestException(
                    String.format("قيمة خطة الاشتراك غير صحيحة: '%s'. القيم المسموحة هي: %s",
                            value, validValues)
            );
        }
    }
}