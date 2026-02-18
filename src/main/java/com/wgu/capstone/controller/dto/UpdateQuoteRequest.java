package com.wgu.capstone.controller.dto;

public class UpdateQuoteRequest {

    private Long newPartSupplierId;

    public UpdateQuoteRequest() {
    }

    public Long getNewPartSupplierId() {
        return newPartSupplierId;
    }

    public void setNewPartSupplierId(Long newPartSupplierId) {
        this.newPartSupplierId = newPartSupplierId;
    }
}

