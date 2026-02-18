package com.wgu.capstone.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "parts")
public class Part {

    @Id
    @Column(nullable = false, updatable = false)
    private String partId;

    @NotBlank
    @Column(name = "part_name", nullable = false)
    private String partName;

    @OneToMany(mappedBy = "part")
    @JsonIgnore
    private List<OrderItem> orderItems = new ArrayList<>();

    // Supplier relationships
    @OneToMany(mappedBy = "part")
    @JsonIgnore
    private List<PartSupplier> suppliers = new ArrayList<>();

    protected Part() {}

    public Part(String partId, String partName) {
        this.partId = partId;
        this.partName = partName;
    }

    public String getPartId() {
        return partId;
    }


    public String getPartName() {
        return partName;
    }

}
