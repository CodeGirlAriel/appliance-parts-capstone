package com.wgu.capstone.service.dto;

import java.math.BigDecimal;

public class SupplierOptionDto {
    private final Long partSupplierId;
    private final Long supplierId;
    private final String supplierName;
    private final BigDecimal partCost;
    private final Integer numInStock;
    private final Integer shippingTime;

    public SupplierOptionDto(
            Long partSupplierId,
            Long supplierId,
            String supplierName,
            BigDecimal partCost,
            Integer numInStock,
            Integer shippingTime
    ) {
        this.partSupplierId = partSupplierId;
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.partCost = partCost;
        this.numInStock = numInStock;
        this.shippingTime = shippingTime;
    }

    public Long getPartSupplierId() {return partSupplierId;}
    public Long getSupplierId() {return supplierId;}
    public String getSupplierName() {return supplierName;}
    public BigDecimal getPartCost() {return partCost;}
    public Integer getNumInStock() {return numInStock;}
    public Integer getShippingTime() {return shippingTime;}
}
