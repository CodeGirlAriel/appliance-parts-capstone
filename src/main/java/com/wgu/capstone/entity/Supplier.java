package com.wgu.capstone.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "suppliers")

public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long supplierId;

    @Column(name = "supplier_name", nullable = false)
    @NotBlank
    private String supplierName;

    @Column(name = "shipping_time", nullable = false)
    @NotNull
    private Integer shippingTime;

    @OneToMany(mappedBy = "supplier")
    @JsonIgnore
    private List<PartSupplier> partSuppliers = new ArrayList<>();


    protected Supplier() {}

    public Supplier(String supplierName, int shippingTime) {
        this.supplierName = supplierName;
        this.shippingTime = shippingTime;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public Integer getShippingTime() {
        return shippingTime;
    }

    public void setShippingTime(int shippingTime) {
        this.shippingTime = shippingTime;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

}
