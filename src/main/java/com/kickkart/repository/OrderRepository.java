package com.kickkart.repository;

import com.kickkart.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<Order> findByRazorpayOrderId(String razorpayOrderId);
    Optional<Order> findByRazorpayPaymentId(String razorpayPaymentId);
}
