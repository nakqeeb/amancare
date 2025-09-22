package com.nakqeeb.amancare.dto.request;

import com.nakqeeb.amancare.entity.InvoiceStatus;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class UpdateInvoiceRequest {

    @FutureOrPresent(message = "تاريخ الاستحقاق يجب أن يكون في المستقبل أو اليوم")
    private LocalDate dueDate;

    private List<CreateInvoiceRequest.InvoiceItemRequest> items;

    @DecimalMin(value = "0.00", message = "نسبة الضريبة يجب أن تكون موجبة")
    @DecimalMax(value = "100.00", message = "نسبة الضريبة لا يمكن أن تتجاوز 100%")
    private BigDecimal taxPercentage;

    @DecimalMin(value = "0.00", message = "مبلغ الخصم يجب أن يكون موجب")
    private BigDecimal discountAmount;

    @DecimalMin(value = "0.00", message = "نسبة الخصم يجب أن تكون موجبة")
    @DecimalMax(value = "100.00", message = "نسبة الخصم لا يمكن أن تتجاوز 100%")
    private BigDecimal discountPercentage;

    @Size(max = 1000, message = "الملاحظات يجب أن تكون أقل من 1000 حرف")
    private String notes;

    @Size(max = 1000, message = "الشروط يجب أن تكون أقل من 1000 حرف")
    private String terms;

    private InvoiceStatus status;
}
