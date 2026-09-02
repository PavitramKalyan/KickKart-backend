package com.kickkart.dto;

import java.math.BigDecimal;

public class RazorpayOrderResponse {

    private String razorpayKeyId;
    private String razorpayOrderId;
    private long amountInPaise;
    private String currency = "INR";
    private BigDecimal subtotal;
    private BigDecimal gst;
    private BigDecimal grandTotal;

    public RazorpayOrderResponse() {}

    public RazorpayOrderResponse(String razorpayKeyId, String razorpayOrderId, long amountInPaise, BigDecimal subtotal, BigDecimal gst, BigDecimal grandTotal) {
        this.razorpayKeyId = razorpayKeyId;
        this.razorpayOrderId = razorpayOrderId;
        this.amountInPaise = amountInPaise;
        this.subtotal = subtotal;
        this.gst = gst;
        this.grandTotal = grandTotal;
        this.currency = "INR";
    }

    public String getRazorpayKeyId() {
        return razorpayKeyId;
    }

    public void setRazorpayKeyId(String razorpayKeyId) {
        this.razorpayKeyId = razorpayKeyId;
    }

    public String getRazorpayOrderId() {
        return razorpayOrderId;
    }

    public void setRazorpayOrderId(String razorpayOrderId) {
        this.razorpayOrderId = razorpayOrderId;
    }

    public long getAmountInPaise() {
        return amountInPaise;
    }

    public void setAmountInPaise(long amountInPaise) {
        this.amountInPaise = amountInPaise;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
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
}
