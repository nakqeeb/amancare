package com.nakqeeb.amancare.dto.response;

import com.nakqeeb.amancare.entity.InvoiceItem;
import com.nakqeeb.amancare.entity.ServiceCategory;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class InvoiceItemResponse {
    private Long id;
    private String serviceName;
    private String serviceCode;
    private String description;
    private ServiceCategory category;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal discountAmount;
    private BigDecimal totalPrice;
    private boolean taxable;
    private String notes;

    public static InvoiceItemResponse fromEntity(InvoiceItem item) {
        InvoiceItemResponse response = new InvoiceItemResponse();
        response.setId(item.getId());
        response.setServiceName(item.getServiceName());
        response.setServiceCode(item.getServiceCode());
        response.setDescription(item.getDescription());
        response.setCategory(item.getCategory());
        response.setQuantity(item.getQuantity());
        response.setUnitPrice(item.getUnitPrice());
        response.setDiscountAmount(item.getDiscountAmount());
        response.setTotalPrice(item.getTotalPrice());
        response.setTaxable(item.isTaxable());
        response.setNotes(item.getNotes());
        return response;
    }
}
