// =============================================================================
// Invoice Item Entity - كيان عنصر الفاتورة
// src/main/java/com/nakqeeb/amancare/entity/InvoiceItem.java
// =============================================================================

package com.nakqeeb.amancare.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * كيان عنصر الفاتورة
 * Invoice Item Entity - Represents individual line items in an invoice
 */
@Entity
@Table(name = "invoice_items",
        indexes = {
                @Index(name = "idx_invoice_id", columnList = "invoice_id"),
                @Index(name = "idx_service_name", columnList = "service_name"),
                @Index(name = "idx_category", columnList = "category")
        })
public class InvoiceItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ===================================================================
    // RELATIONSHIPS
    // ===================================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    @NotNull(message = "الفاتورة مطلوبة")
    private Invoice invoice;

    // ===================================================================
    // SERVICE DETAILS
    // ===================================================================

    @NotBlank(message = "اسم الخدمة مطلوب")
    @Size(max = 255, message = "اسم الخدمة يجب أن يكون أقل من 255 حرف")
    @Column(name = "service_name", nullable = false)
    private String serviceName;

    @Size(max = 50, message = "كود الخدمة يجب أن يكون أقل من 50 حرف")
    @Column(name = "service_code", length = 50)
    private String serviceCode;

    @Size(max = 500, message = "الوصف يجب أن يكون أقل من 500 حرف")
    @Column(name = "description", length = 500)
    private String description;

    @NotNull(message = "فئة الخدمة مطلوبة")
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private ServiceCategory category;

    // ===================================================================
    // PRICING DETAILS
    // ===================================================================

    @NotNull(message = "الكمية مطلوبة")
    @Min(value = 1, message = "الكمية يجب أن تكون 1 على الأقل")
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @NotNull(message = "سعر الوحدة مطلوب")
    @DecimalMin(value = "0.00", message = "سعر الوحدة يجب أن يكون موجب")
    @Column(name = "unit_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @DecimalMin(value = "0.00", message = "مبلغ الخصم يجب أن يكون موجب")
    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @DecimalMin(value = "0.00", message = "نسبة الخصم يجب أن تكون موجبة")
    @DecimalMax(value = "100.00", message = "نسبة الخصم لا يمكن أن تتجاوز 100%")
    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage = BigDecimal.ZERO;

    @NotNull(message = "السعر الإجمالي مطلوب")
    @DecimalMin(value = "0.00", message = "السعر الإجمالي يجب أن يكون موجب")
    @Column(name = "total_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalPrice;

    // ===================================================================
    // TAX DETAILS
    // ===================================================================

    @Column(name = "is_taxable")
    private boolean taxable = false;

    @DecimalMin(value = "0.00", message = "مبلغ الضريبة يجب أن يكون موجب")
    @Column(name = "tax_amount", precision = 10, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    // ===================================================================
    // ADDITIONAL INFORMATION
    // ===================================================================

    @Column(name = "procedure_date")
    private LocalDate procedureDate;

    @Size(max = 255, message = "اسم المنفذ يجب أن يكون أقل من 255 حرف")
    @Column(name = "performed_by")
    private String performedBy;

    @Size(max = 1000, message = "الملاحظات يجب أن تكون أقل من 1000 حرف")
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // ===================================================================
    // AUDIT FIELDS (inherited from BaseEntity)
    // ===================================================================
    // createdAt, updatedAt are inherited from BaseEntity

    // ===================================================================
    // CONSTRUCTORS
    // ===================================================================

    /**
     * Default constructor
     */
    public InvoiceItem() {
        this.quantity = 1;
        this.unitPrice = BigDecimal.ZERO;
        this.discountAmount = BigDecimal.ZERO;
        this.discountPercentage = BigDecimal.ZERO;
        this.totalPrice = BigDecimal.ZERO;
        this.taxAmount = BigDecimal.ZERO;
        this.taxable = false;
    }

    /**
     * Constructor with basic parameters
     */
    public InvoiceItem(Invoice invoice, String serviceName, ServiceCategory category,
                       Integer quantity, BigDecimal unitPrice) {
        this();
        this.invoice = invoice;
        this.serviceName = serviceName;
        this.category = category;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        calculateTotalPrice();
    }

    // ===================================================================
    // BUSINESS METHODS
    // ===================================================================

    /**
     * Calculate the total price for this item
     * Total = (Unit Price × Quantity) - Discount Amount + Tax Amount
     */
    public void calculateTotalPrice() {
        if (this.unitPrice == null || this.quantity == null) {
            this.totalPrice = BigDecimal.ZERO;
            return;
        }

        // Calculate base amount
        BigDecimal baseAmount = this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));

        // Apply discount
        BigDecimal discountedAmount = baseAmount;
        if (this.discountAmount != null && this.discountAmount.compareTo(BigDecimal.ZERO) > 0) {
            discountedAmount = baseAmount.subtract(this.discountAmount);
        } else if (this.discountPercentage != null && this.discountPercentage.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discount = baseAmount.multiply(this.discountPercentage)
                    .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
            this.discountAmount = discount;
            discountedAmount = baseAmount.subtract(discount);
        }

        // Add tax if applicable
        if (this.taxable && this.taxAmount != null) {
            discountedAmount = discountedAmount.add(this.taxAmount);
        }

        this.totalPrice = discountedAmount;
    }

    /**
     * Calculate tax amount based on a given tax percentage
     */
    public void calculateTaxAmount(BigDecimal taxPercentage) {
        if (!this.taxable || taxPercentage == null || taxPercentage.compareTo(BigDecimal.ZERO) <= 0) {
            this.taxAmount = BigDecimal.ZERO;
            return;
        }

        // Calculate taxable amount (after discount)
        BigDecimal baseAmount = this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));
        BigDecimal taxableAmount = baseAmount.subtract(this.discountAmount);

        // Calculate tax
        this.taxAmount = taxableAmount.multiply(taxPercentage)
                .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Get formatted service details for display
     */
    public String getFormattedServiceDetails() {
        StringBuilder details = new StringBuilder();
        details.append(serviceName);

        if (serviceCode != null && !serviceCode.isEmpty()) {
            details.append(" (").append(serviceCode).append(")");
        }

        if (description != null && !description.isEmpty()) {
            details.append(" - ").append(description);
        }

        return details.toString();
    }

    /**
     * Check if item has discount
     */
    public boolean hasDiscount() {
        return (discountAmount != null && discountAmount.compareTo(BigDecimal.ZERO) > 0) ||
                (discountPercentage != null && discountPercentage.compareTo(BigDecimal.ZERO) > 0);
    }

    // ===================================================================
    // GETTERS AND SETTERS
    // ===================================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ServiceCategory getCategory() {
        return category;
    }

    public void setCategory(ServiceCategory category) {
        this.category = category;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        calculateTotalPrice(); // Recalculate when quantity changes
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        calculateTotalPrice(); // Recalculate when price changes
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount != null ? discountAmount : BigDecimal.ZERO;
        calculateTotalPrice(); // Recalculate when discount changes
    }

    public BigDecimal getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(BigDecimal discountPercentage) {
        this.discountPercentage = discountPercentage != null ? discountPercentage : BigDecimal.ZERO;
        calculateTotalPrice(); // Recalculate when discount percentage changes
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public boolean isTaxable() {
        return taxable;
    }

    public void setTaxable(boolean taxable) {
        this.taxable = taxable;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount != null ? taxAmount : BigDecimal.ZERO;
    }

    public LocalDate getProcedureDate() {
        return procedureDate;
    }

    public void setProcedureDate(LocalDate procedureDate) {
        this.procedureDate = procedureDate;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // ===================================================================
    // EQUALS, HASHCODE, TOSTRING
    // ===================================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InvoiceItem)) return false;
        InvoiceItem that = (InvoiceItem) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "InvoiceItem{" +
                "id=" + id +
                ", serviceName='" + serviceName + '\'' +
                ", category=" + category +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", totalPrice=" + totalPrice +
                ", taxable=" + taxable +
                '}';
    }
}