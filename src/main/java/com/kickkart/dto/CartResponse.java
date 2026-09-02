package com.kickkart.dto;

import java.math.BigDecimal;
import java.util.List;

public class CartResponse {
    private List<CartItemDto> items;
    private BigDecimal subtotal;
    private BigDecimal gst;
    private BigDecimal grandTotal;
    private Integer totalItems;

    public CartResponse() {}

    public CartResponse(List<CartItemDto> items, BigDecimal subtotal, BigDecimal gst, BigDecimal grandTotal, Integer totalItems) {
        this.items = items;
        this.subtotal = subtotal;
        this.gst = gst;
        this.grandTotal = grandTotal;
        this.totalItems = totalItems;
    }

    public List<CartItemDto> getItems() {
        return items;
    }

    public void setItems(List<CartItemDto> items) {
        this.items = items;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getGst() {
        return gst;
    }

    public void setGst(BigDecimal gst) {
        this.gst = gst;
    }

    public BigDecimal getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(BigDecimal grandTotal) {
        this.grandTotal = grandTotal;
    }

    public Integer getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(Integer totalItems) {
        this.totalItems = totalItems;
    }
}
