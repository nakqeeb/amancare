package com.nakqeeb.amancare.config.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.nakqeeb.amancare.entity.InvoiceStatus;
import com.nakqeeb.amancare.exception.BadRequestException;

import java.io.IOException;

public class InvoiceStatusDeserializer extends JsonDeserializer<InvoiceStatus> {

    @Override
    public InvoiceStatus deserialize(JsonParser parser, DeserializationContext context)
            throws IOException {
        String value = parser.getText();

        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String normalizedValue = value.trim().toUpperCase();

        try {
            return InvoiceStatus.valueOf(normalizedValue);
        } catch (IllegalArgumentException e) {
            String validValues = String.join(", ",
                    java.util.Arrays.stream(InvoiceStatus.values())
                            .map(Enum::name)
                            .toArray(String[]::new));

            throw new BadRequestException(
                    String.format("حالة الفاتورة غير صحيحة: '%s'. القيم المسموحة هي: %s",
                            value, validValues)
            );
        }
    }
}
