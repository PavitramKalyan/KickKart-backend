package com.kickkart.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class AnalyticsDto {

    private String period; // "DAILY", "MONTHLY", "YEARLY", "OVERALL"
    private String dateLabel;
    private BigDecimal totalRevenue;
    private Long totalOrders;
    private Long totalItemsSold;
    private Long totalCustomers;
    private List<Map<String, Object>> monthlyBreakdown;

    public AnalyticsDto() {}

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getDateLabel() {
        return dateLabel;
    }

    public void setDateLabel(String dateLabel) {
        this.dateLabel = dateLabel;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public Long getTotalItemsSold() {
        return totalItemsSold;
    }

    public void setTotalItemsSold(Long totalItemsSold) {
        this.totalItemsSold = totalItemsSold;
    }

    public Long getTotalCustomers() {
        return totalCustomers;
    }

    public void setTotalCustomers(Long totalCustomers) {
        this.totalCustomers = totalCustomers;
    }

    public List<Map<String, Object>> getMonthlyBreakdown() {
        return monthlyBreakdown;
    }

    public void setMonthlyBreakdown(List<Map<String, Object>> monthlyBreakdown) {
        this.monthlyBreakdown = monthlyBreakdown;
    }
}
