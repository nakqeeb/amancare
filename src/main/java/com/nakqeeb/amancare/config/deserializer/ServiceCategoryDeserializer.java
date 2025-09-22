package com.nakqeeb.amancare.config.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.nakqeeb.amancare.entity.ServiceCategory;
import com.nakqeeb.amancare.exception.BadRequestException;

import java.io.IOException;

public class ServiceCategoryDeserializer extends JsonDeserializer<ServiceCategory> {

    @Override
    public ServiceCategory deserialize(JsonParser parser, DeserializationContext context)
            throws IOException {
        String value = parser.getText();

        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String normalizedValue = value.trim().toUpperCase().replace(" ", "_");

        // Handle common variations
        if (normalizedValue.equals("LAB") || normalizedValue.equals("LABORATORY")) {
            normalizedValue = "LAB_TEST";
        }

        try {
            return ServiceCategory.valueOf(normalizedValue);
        } catch (IllegalArgumentException e) {
            String validValues = String.join(", ",
                    java.util.Arrays.stream(ServiceCategory.values())
                            .map(Enum::name)
                            .toArray(String[]::new));

            throw new BadRequestException(
                    String.format("فئة الخدمة غير صحيحة: '%s'. القيم المسموحة هي: %s",
                            value, validValues)
            );
        }
    }
}
