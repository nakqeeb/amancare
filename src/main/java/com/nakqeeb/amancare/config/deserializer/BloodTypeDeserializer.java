package com.nakqeeb.amancare.config.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.nakqeeb.amancare.entity.BloodType;
import com.nakqeeb.amancare.exception.BadRequestException;

import java.io.IOException;

public class BloodTypeDeserializer extends JsonDeserializer<BloodType> {

    @Override
    public BloodType deserialize(JsonParser parser, DeserializationContext context)
            throws IOException {
        String value = parser.getText();

        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String normalizedValue = value.trim().toUpperCase()
                .replace(" ", "_")
                .replace("+", "_POSITIVE")
                .replace("-", "_NEGATIVE");

        try {
            return BloodType.valueOf(normalizedValue);
        } catch (IllegalArgumentException e) {
            String validValues = String.join(", ",
                    java.util.Arrays.stream(BloodType.values())
                            .map(Enum::name)
                            .toArray(String[]::new));

            throw new BadRequestException(
                    String.format("فصيلة الدم غير صحيحة: '%s'. القيم المسموحة هي: %s",
                            value, validValues)
            );
        }
    }
}
