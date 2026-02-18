package com.wgu.capstone.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Entity
@Table(name = "part_suppliers", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"part_id", "supplier_id"})
})

public class PartSupplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long partSupplierId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "part_id")
    private Part part;

    @Column(name = "part_cost", nullable = false)
    @NotNull
    private BigDecimal partCost;

    @Column(name = "num_in_stock", nullable = false)
    @NotNull
    private Integer numInStock;


    protected PartSupplier() {}

    public PartSupplier(Supplier supplier, Part part, BigDecimal partCost, Integer numInStock) {
        this.supplier = supplier;
        this.part = part;
        this.partCost = partCost;
        this.numInStock = numInStock;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public Long getSupplierId() {
        return supplier != null ? supplier.getSupplierId() : null;
    }

    public Part getPart() {
        return part;
    }

    public String getPartId() {
        return part != null ? part.getPartId() : null;
    }


    public void setPartCost(BigDecimal partCost) {
        this.partCost = partCost;
    }

    public BigDecimal getPartCost() {
        return partCost;
    }

    public void setNumInStock(Integer numInStock) {
        this.numInStock = numInStock;
    }

    public Integer getNumInStock() {
        return numInStock;
    }

    public Long getPartSupplierId() {
        return partSupplierId;
    }

    public void calculateStock(int orderedQuantity) {
        // 1) Validate input
        if (orderedQuantity <= 0) {
            throw new IllegalArgumentException("orderedQuantity must be greater than 0");
        }

        // 2) Defensive: treat null stock as 0
        if (this.numInStock == null) {
            this.numInStock = 0;
        }

        // 3) Prevent overselling
        if (this.numInStock < orderedQuantity) {
            throw new IllegalStateException(
                    "Not enough stock. Available: " + this.numInStock + ", requested: " + orderedQuantity
            );
        }

        // 4) Apply the math
        this.numInStock = this.numInStock - orderedQuantity;
    }

    public void restock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be greater than 0");
        }
        if (this.numInStock == null) {
            this.numInStock = 0;
        }
        this.numInStock += quantity;
    }




}
