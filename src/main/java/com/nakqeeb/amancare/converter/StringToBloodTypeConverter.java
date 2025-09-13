// =============================================================================
// StringToBloodTypeConverter.java
// Location: /src/main/java/com/nakqeeb/amancare/converter/
// =============================================================================

package com.nakqeeb.amancare.converter;

import com.nakqeeb.amancare.entity.BloodType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Converter to handle BloodType conversion from String
 * Supports both enum names (O_POSITIVE) and symbols (O+)
 *
 * This converter allows the API to accept blood type values in multiple formats:
 * - Symbol format: O+, O-, A+, A-, B+, B-, AB+, AB-
 * - Enum format: O_POSITIVE, O_NEGATIVE, A_POSITIVE, etc.
 * - Mixed format: O POSITIVE, A NEGATIVE (with spaces)
 * - Case insensitive: o+, O+, o positive
 */
@Component
public class StringToBloodTypeConverter implements Converter<String, BloodType> {

    @Override
    public BloodType convert(String source) {
        // Handle null or empty values
        if (source == null || source.trim().isEmpty()) {
            return null;
        }

        // Normalize the input: trim and convert to uppercase
        String normalizedSource = source.trim().toUpperCase();

        // First, try to match by symbol (O+, A-, etc.)
        // This is the most common format from frontend
        for (BloodType bloodType : BloodType.values()) {
            if (bloodType.getSymbol().equalsIgnoreCase(normalizedSource)) {
                return bloodType;
            }
        }

        // Then try to match by enum name (O_POSITIVE, A_NEGATIVE, etc.)
        // This handles the standard enum format
        try {
            return BloodType.valueOf(normalizedSource);
        } catch (IllegalArgumentException e) {
            // If direct enum conversion fails, try more transformations
        }

        // Try replacing common variations to match enum names
        // Handle formats like "O+", "A-", "AB+" etc.
        String enumStyleSource = normalizedSource
                .replace("+", "_POSITIVE")
                .replace("-", "_NEGATIVE")
                .replace(" ", "_");  // Handle "O POSITIVE" format

        try {
            return BloodType.valueOf(enumStyleSource);
        } catch (IllegalArgumentException ex) {
            // If still no match, try one more variation
        }

        // Handle special cases where user might type differently
        // For example: "O POS", "A NEG", etc.
        enumStyleSource = normalizedSource
                .replace("POS", "_POSITIVE")
                .replace("NEG", "_NEGATIVE")
                .replace("POSITIVE", "_POSITIVE")
                .replace("NEGATIVE", "_NEGATIVE")
                .replace(" ", "_")
                .replace("__", "_");  // Clean up double underscores

        try {
            return BloodType.valueOf(enumStyleSource);
        } catch (IllegalArgumentException finalEx) {
            // Provide a helpful error message with all valid formats
            throw new IllegalArgumentException(
                    "Invalid blood type: '" + source + "'. " +
                            "Valid formats are: " +
                            "O+, O-, A+, A-, B+, B-, AB+, AB- or " +
                            "O_POSITIVE, O_NEGATIVE, A_POSITIVE, A_NEGATIVE, B_POSITIVE, B_NEGATIVE, AB_POSITIVE, AB_NEGATIVE"
            );
        }
    }
}