package com.nakqeeb.amancare.config.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.nakqeeb.amancare.entity.Gender;
import com.nakqeeb.amancare.exception.BadRequestException;

import java.io.IOException;

public class GenderDeserializer extends JsonDeserializer<Gender> {

    @Override
    public Gender deserialize(JsonParser parser, DeserializationContext context)
            throws IOException {
        String value = parser.getText();

        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String normalizedValue = value.trim().toUpperCase();

        // Handle common variations
        if (normalizedValue.equals("M") || normalizedValue.equals("ذكر")) {
            normalizedValue = "MALE";
        } else if (normalizedValue.equals("F") || normalizedValue.equals("أنثى")) {
            normalizedValue = "FEMALE";
        }

        try {
            return Gender.valueOf(normalizedValue);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(
                    String.format("الجنس غير صحيح: '%s'. القيم المسموحة هي: MALE, FEMALE", value)
            );
        }
    }
}
