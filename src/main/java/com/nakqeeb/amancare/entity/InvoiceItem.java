// =============================================================================
// Invoice Item Entity - كيان عنصر الفاتورة
// =============================================================================

package com.nakqeeb.amancare.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * كيان عنصر الفاتورة
 */
@Entity
@Table(name = "invoice_items")
public class InvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @NotBlank(message = "اسم الخدمة مطلوب")
    @Column(name = "service_name", nullable = false)
    private String serviceName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "الكمية مطلوبة")
    @Min(value = 1, message = "الكمية يجب أن تكون أكبر من صفر")
    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;

    @NotNull(message = "سعر الوحدة مطلوب")
    @DecimalMin(value = "0.00", message = "سعر الوحدة يجب أن يكون أكبر من أو يساوي صفر")
    @Column(name = "unit_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @NotNull(message = "السعر الإجمالي مطلوب")
    @DecimalMin(value = "0.00", message = "السعر الإجمالي يجب أن يكون أكبر من أو يساوي صفر")
    @Column(name = "total_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalPrice;

    // Constructors
    public InvoiceItem() {}

    public InvoiceItem(Invoice invoice, String serviceName, Integer quantity, BigDecimal unitPrice) {
        this.invoice = invoice;
        this.serviceName = serviceName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    // Methods
    public void calculateTotal() {
        this.totalPrice = this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Invoice getInvoice() { return invoice; }
    public void setInvoice(Invoice invoice) { this.invoice = invoice; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        calculateTotal();
    }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        calculateTotal();
    }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
}