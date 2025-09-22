package com.nakqeeb.amancare.config.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.nakqeeb.amancare.exception.BadRequestException;

import java.io.IOException;

/**
 * Generic enum deserializer that can be reused for any enum type
 */
public class GenericEnumDeserializer<T extends Enum<T>> extends JsonDeserializer<T> {

    private final Class<T> enumClass;
    private final String fieldName;

    public GenericEnumDeserializer(Class<T> enumClass, String fieldName) {
        this.enumClass = enumClass;
        this.fieldName = fieldName;
    }

    @Override
    public T deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String value = parser.getText();

        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        // Normalize the value
        String normalizedValue = value.trim().toUpperCase()
                .replace(" ", "_")
                .replace("-", "_");

        try {
            return Enum.valueOf(enumClass, normalizedValue);
        } catch (IllegalArgumentException e) {
            // Get all valid values
            String validValues = String.join(", ",
                    java.util.Arrays.stream(enumClass.getEnumConstants())
                            .map(Enum::name)
                            .toArray(String[]::new));

            throw new BadRequestException(
                    String.format("%s غير صحيح: '%s'. القيم المسموحة هي: %s",
                            fieldName, value, validValues)
            );
        }
    }
}
