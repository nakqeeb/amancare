// =============================================================================
// StringToGenderConverter.java
// Location: /src/main/java/com/nakqeeb/amancare/converter/
// =============================================================================

package com.nakqeeb.amancare.converter;

import com.nakqeeb.amancare.entity.Gender;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Converter to handle Gender conversion from String
 * Supports various formats: MALE/FEMALE, male/female, M/F, ذكر/أنثى
 *
 * This converter allows the API to accept gender values in multiple formats:
 * - Full format: MALE, FEMALE
 * - Lowercase: male, female
 * - Short format: M, F
 * - Arabic format: ذكر, أنثى, انثى (with and without hamza)
 * - Case insensitive variations
 */
@Component
public class StringToGenderConverter implements Converter<String, Gender> {

    @Override
    public Gender convert(String source) {
        // Handle null or empty values
        if (source == null || source.trim().isEmpty()) {
            return null;
        }

        // Normalize the input: trim and convert to uppercase
        String normalizedSource = source.trim().toUpperCase();

        // Handle various common formats
        switch (normalizedSource) {
            // English formats
            case "MALE":
            case "M":
                return Gender.MALE;
            case "FEMALE":
            case "F":
                return Gender.FEMALE;
            default:
                // Continue to other checks
                break;
        }

        // Check for Arabic formats (case-sensitive)
        String trimmedSource = source.trim();
        switch (trimmedSource) {
            case "ذكر":
                return Gender.MALE;
            case "أنثى":
            case "انثى":  // Without hamza
            case "انثي":  // Alternative spelling
                return Gender.FEMALE;
            default:
                // Continue to other checks
                break;
        }

        // Try direct enum conversion as last resort
        try {
            return Gender.valueOf(normalizedSource);
        } catch (IllegalArgumentException e) {
            // Check for numeric codes (sometimes used in forms)
            if ("1".equals(normalizedSource)) {
                return Gender.MALE;
            } else if ("2".equals(normalizedSource)) {
                return Gender.FEMALE;
            }

            // If all attempts fail, throw a helpful error
            throw new IllegalArgumentException(
                    "Invalid gender: '" + source + "'. " +
                            "Valid values are: MALE, FEMALE, M, F, ذكر, أنثى, 1, 2"
            );
        }
    }
}