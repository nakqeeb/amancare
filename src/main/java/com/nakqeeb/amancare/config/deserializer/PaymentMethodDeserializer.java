package com.nakqeeb.amancare.config.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.nakqeeb.amancare.entity.PaymentMethod;
import com.nakqeeb.amancare.exception.BadRequestException;

import java.io.IOException;

public class PaymentMethodDeserializer extends JsonDeserializer<PaymentMethod> {

    @Override
    public PaymentMethod deserialize(JsonParser parser, DeserializationContext context)
            throws IOException {
        String value = parser.getText();

        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String normalizedValue = value.trim().toUpperCase().replace(" ", "_").replace("-", "_");

        try {
            return PaymentMethod.valueOf(normalizedValue);
        } catch (IllegalArgumentException e) {
            String validValues = String.join(", ",
                    java.util.Arrays.stream(PaymentMethod.values())
                            .map(Enum::name)
                            .toArray(String[]::new));

            throw new BadRequestException(
                    String.format("طريقة الدفع غير صحيحة: '%s'. القيم المسموحة هي: %s",
                            value, validValues)
            );
        }
    }
}
