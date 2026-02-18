package com.wgu.capstone.controller.dto;

public class SaveQuoteRequest {

    private Long partSupplierId;
    private Integer quantity;
    private Boolean isCartItem = false; // true for cart, false for saved quote

    public SaveQuoteRequest() {
    }

    public Long getPartSupplierId() {
        return partSupplierId;
    }

    public void setPartSupplierId(Long partSupplierId) {
        this.partSupplierId = partSupplierId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Boolean getIsCartItem() {
        return isCartItem;
    }

    public void setIsCartItem(Boolean isCartItem) {
        this.isCartItem = isCartItem;
    }
}