package com.nakqeeb.amancare.dto.request;

import com.nakqeeb.amancare.entity.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatePaymentRequest {

    @NotNull(message = "معرف الفاتورة مطلوب")
    private Long invoiceId;

    @NotNull(message = "المبلغ مطلوب")
    @DecimalMin(value = "0.01", message = "المبلغ يجب أن يكون أكبر من صفر")
    private BigDecimal amount;

    @NotNull(message = "طريقة الدفع مطلوبة")
    private PaymentMethod paymentMethod;

    @Size(max = 100, message = "رقم المرجع يجب أن يكون أقل من 100 حرف")
    private String referenceNumber;

    @Size(max = 500, message = "الملاحظات يجب أن تكون أقل من 500 حرف")
    private String notes;

    // Payment method specific fields
    @Pattern(regexp = "^\\d{4}$", message = "آخر 4 أرقام من البطاقة غير صحيحة")
    private String cardLastFourDigits;

    @Size(max = 50, message = "رقم الشيك يجب أن يكون أقل من 50 حرف")
    private String checkNumber;

    @Size(max = 100, message = "اسم البنك يجب أن يكون أقل من 100 حرف")
    private String bankName;

    @Size(max = 100, message = "رقم المعاملة يجب أن يكون أقل من 100 حرف")
    private String transactionId;
}
