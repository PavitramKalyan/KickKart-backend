package com.kickkart.service;

import com.kickkart.dto.AnalyticsDto;
import com.kickkart.entity.Order;
import com.kickkart.entity.OrderItem;
import com.kickkart.repository.OrderItemRepository;
import com.kickkart.repository.OrderRepository;
import com.kickkart.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;

@Service
public class AdminAnalyticsService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private UserRepository userRepository;

    public AnalyticsDto getDailyAnalytics(LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }

        List<Order> allOrders = orderRepository.findAll();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        long totalOrders = 0;
        long totalItemsSold = 0;

        for (Order order : allOrders) {
            if (isSuccessfulOrder(order) && order.getCreatedAt() != null) {
                if (order.getCreatedAt().toLocalDate().equals(date)) {
                    totalRevenue = totalRevenue.add(order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO);
                    totalOrders++;
                    List<OrderItem> items = orderItemRepository.findByOrderId(order.getOrderId());
                    for (OrderItem item : items) {
                        totalItemsSold += (item.getQuantity() != null ? item.getQuantity() : 0);
                    }
                }
            }
        }

        AnalyticsDto dto = new AnalyticsDto();
        dto.setPeriod("DAILY");
        dto.setDateLabel(date.toString());
        dto.setTotalRevenue(totalRevenue);
        dto.setTotalOrders(totalOrders);
        dto.setTotalItemsSold(totalItemsSold);
        dto.setTotalCustomers(userRepository.count());
        return dto;
    }

    public AnalyticsDto getMonthlyAnalytics(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        List<Order> allOrders = orderRepository.findAll();

        BigDecimal totalRevenue = BigDecimal.ZERO;
        long totalOrders = 0;
        long totalItemsSold = 0;

        for (Order order : allOrders) {
            if (isSuccessfulOrder(order) && order.getCreatedAt() != null) {
                YearMonth orderYm = YearMonth.from(order.getCreatedAt());
                if (orderYm.equals(ym)) {
                    totalRevenue = totalRevenue.add(order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO);
                    totalOrders++;
                    List<OrderItem> items = orderItemRepository.findByOrderId(order.getOrderId());
                    for (OrderItem item : items) {
                        totalItemsSold += (item.getQuantity() != null ? item.getQuantity() : 0);
                    }
                }
            }
        }

        AnalyticsDto dto = new AnalyticsDto();
        dto.setPeriod("MONTHLY");
        dto.setDateLabel(ym.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + year);
        dto.setTotalRevenue(totalRevenue);
        dto.setTotalOrders(totalOrders);
        dto.setTotalItemsSold(totalItemsSold);
        dto.setTotalCustomers(userRepository.count());
        return dto;
    }

    public AnalyticsDto getYearlyAnalytics(int year) {
        List<Order> allOrders = orderRepository.findAll();

        BigDecimal totalRevenue = BigDecimal.ZERO;
        long totalOrders = 0;
        long totalItemsSold = 0;

        Map<Integer, BigDecimal> monthRevenueMap = new HashMap<>();
        Map<Integer, Long> monthOrdersMap = new HashMap<>();

        for (int m = 1; m <= 12; m++) {
            monthRevenueMap.put(m, BigDecimal.ZERO);
            monthOrdersMap.put(m, 0L);
        }

        for (Order order : allOrders) {
            if (isSuccessfulOrder(order) && order.getCreatedAt() != null) {
                if (order.getCreatedAt().getYear() == year) {
                    BigDecimal amt = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;
                    totalRevenue = totalRevenue.add(amt);
                    totalOrders++;

                    int m = order.getCreatedAt().getMonthValue();
                    monthRevenueMap.put(m, monthRevenueMap.get(m).add(amt));
                    monthOrdersMap.put(m, monthOrdersMap.get(m) + 1);

                    List<OrderItem> items = orderItemRepository.findByOrderId(order.getOrderId());
                    for (OrderItem item : items) {
                        totalItemsSold += (item.getQuantity() != null ? item.getQuantity() : 0);
                    }
                }
            }
        }

        List<Map<String, Object>> breakdown = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            Map<String, Object> mData = new HashMap<>();
            String mName = YearMonth.of(year, m).getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            mData.put("monthNumber", m);
            mData.put("monthName", mName);
            mData.put("revenue", monthRevenueMap.get(m));
            mData.put("ordersCount", monthOrdersMap.get(m));
            breakdown.add(mData);
        }

        AnalyticsDto dto = new AnalyticsDto();
        dto.setPeriod("YEARLY");
        dto.setDateLabel(String.valueOf(year));
        dto.setTotalRevenue(totalRevenue);
        dto.setTotalOrders(totalOrders);
        dto.setTotalItemsSold(totalItemsSold);
        dto.setTotalCustomers(userRepository.count());
        dto.setMonthlyBreakdown(breakdown);
        return dto;
    }

    public AnalyticsDto getOverallAnalytics() {
        List<Order> allOrders = orderRepository.findAll();

        BigDecimal totalRevenue = BigDecimal.ZERO;
        long totalOrders = 0;
        long totalItemsSold = 0;

        for (Order order : allOrders) {
            if (isSuccessfulOrder(order)) {
                totalRevenue = totalRevenue.add(order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO);
                totalOrders++;
                List<OrderItem> items = orderItemRepository.findByOrderId(order.getOrderId());
                for (OrderItem item : items) {
                    totalItemsSold += (item.getQuantity() != null ? item.getQuantity() : 0);
                }
            }
        }

        AnalyticsDto dto = new AnalyticsDto();
        dto.setPeriod("OVERALL");
        dto.setDateLabel("Lifetime");
        dto.setTotalRevenue(totalRevenue);
        dto.setTotalOrders(totalOrders);
        dto.setTotalItemsSold(totalItemsSold);
        dto.setTotalCustomers(userRepository.count());
        return dto;
    }

    private boolean isSuccessfulOrder(Order order) {
        if (order == null) return false;
        String status = order.getStatus() != null ? order.getStatus() : "";
        String pStatus = order.getPaymentStatus() != null ? order.getPaymentStatus() : "";
        return "SUCCESS".equalsIgnoreCase(status) || "PAID".equalsIgnoreCase(pStatus) || "COMPLETED".equalsIgnoreCase(pStatus);
    }
}
