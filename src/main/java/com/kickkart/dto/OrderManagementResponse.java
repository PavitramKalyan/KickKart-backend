package com.kickkart.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderManagementResponse {

    private boolean success = true;
    private String message = "Orders retrieved successfully";
    private String role;
    private String username;
    private Map<String, List<OrderProductResponse>> orders;
    private Object data;

    public OrderManagementResponse() {
        this.orders = new HashMap<>();
    }

    public OrderManagementResponse(String role, String username, List<OrderProductResponse> products) {
        this.success = true;
        this.message = "Orders retrieved successfully";
        this.role = role;
        this.username = username;
        this.orders = new HashMap<>();
        this.orders.put("products", products);

        Map<String, Object> dataPayload = new HashMap<>();
        dataPayload.put("role", role);
        dataPayload.put("username", username);
        dataPayload.put("orders", this.orders);
        this.data = dataPayload;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Map<String, List<OrderProductResponse>> getOrders() {
        return orders;
    }

    public void setOrders(Map<String, List<OrderProductResponse>> orders) {
        this.orders = orders;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
