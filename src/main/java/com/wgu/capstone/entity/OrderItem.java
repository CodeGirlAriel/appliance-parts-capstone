package com.wgu.capstone.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")

public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "part_supplier_id")
    @JsonIgnore
    private PartSupplier selectedSupplier;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    @JsonIgnore
    private Order order;

    @ManyToOne(optional = false, fetch  = FetchType.LAZY)
    @JoinColumn(name = "part_id")
    private Part part;

    @NotNull
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @NotNull
    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;

    protected OrderItem() {}

    public OrderItem(Order order, Part part, int quantity, BigDecimal unitPrice) {
        this.order = order;
        this.part = part;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }
    
    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Integer getQuantity() {
        return quantity;
    }


    public void setOrder(Order order) {
        this.order = order;
    }

    public Part getPart() {
        return part;
    }

    public Long getOrderItemId() {
        return orderItemId;
    }
    
    public PartSupplier getSelectedSupplier() {
        return selectedSupplier;
    }
    
    public void setSelectedSupplier(PartSupplier selectedSupplier) {
        this.selectedSupplier = selectedSupplier;
    }
    
    // Helper method to get supplier name for JSON serialization
    public String getSupplierName() {
        return selectedSupplier != null && selectedSupplier.getSupplier() != null 
            ? selectedSupplier.getSupplier().getSupplierName() 
            : null;
    }
    
    // Helper method to get part supplier ID for JSON serialization
    public Long getPartSupplierId() {
        return selectedSupplier != null ? selectedSupplier.getPartSupplierId() : null;
    }
}
