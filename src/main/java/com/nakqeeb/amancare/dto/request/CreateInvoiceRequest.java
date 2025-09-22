package com.nakqeeb.amancare.dto.request;

import com.nakqeeb.amancare.entity.ServiceCategory;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class CreateInvoiceRequest {

    @NotNull(message = "معرف المريض مطلوب")
    private Long patientId;

    private Long appointmentId;

    @NotNull(message = "تاريخ الاستحقاق مطلوب")
    @FutureOrPresent(message = "تاريخ الاستحقاق يجب أن يكون في المستقبل أو اليوم")
    private LocalDate dueDate;

    @NotEmpty(message = "يجب إضافة عنصر واحد على الأقل للفاتورة")
    private List<InvoiceItemRequest> items;

    @DecimalMin(value = "0.00", message = "نسبة الضريبة يجب أن تكون موجبة")
    @DecimalMax(value = "100.00", message = "نسبة الضريبة لا يمكن أن تتجاوز 100%")
    private BigDecimal taxPercentage = BigDecimal.ZERO;

    @DecimalMin(value = "0.00", message = "مبلغ الخصم يجب أن يكون موجب")
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @DecimalMin(value = "0.00", message = "نسبة الخصم يجب أن تكون موجبة")
    @DecimalMax(value = "100.00", message = "نسبة الخصم لا يمكن أن تتجاوز 100%")
    private BigDecimal discountPercentage = BigDecimal.ZERO;

    @Size(max = 1000, message = "الملاحظات يجب أن تكون أقل من 1000 حرف")
    private String notes;

    @Size(max = 1000, message = "الشروط يجب أن تكون أقل من 1000 حرف")
    private String terms;

    @Data
    public static class InvoiceItemRequest {
        @NotBlank(message = "اسم الخدمة مطلوب")
        private String serviceName;

        private String serviceCode;
        private String description;

        @NotNull(message = "فئة الخدمة مطلوبة")
        private ServiceCategory category;

        @NotNull(message = "الكمية مطلوبة")
        @Min(value = 1, message = "الكمية يجب أن تكون 1 على الأقل")
        private Integer quantity;

        @NotNull(message = "سعر الوحدة مطلوب")
        @DecimalMin(value = "0.01", message = "سعر الوحدة يجب أن يكون أكبر من صفر")
        private BigDecimal unitPrice;

        @DecimalMin(value = "0.00", message = "مبلغ الخصم يجب أن يكون موجب")
        private BigDecimal discountAmount = BigDecimal.ZERO;

        @DecimalMin(value = "0.00", message = "نسبة الخصم يجب أن تكون موجبة")
        @DecimalMax(value = "100.00", message = "نسبة الخصم لا يمكن أن تتجاوز 100%")
        private BigDecimal discountPercentage = BigDecimal.ZERO;

        private boolean taxable = false;
        private String notes;
    }
}
