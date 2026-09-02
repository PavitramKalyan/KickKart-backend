package com.kickkart.service;

import com.kickkart.dto.OrderDto;
import com.kickkart.dto.RazorpayOrderRequest;
import com.kickkart.dto.RazorpayOrderResponse;
import com.kickkart.dto.RazorpayVerificationRequest;
import com.kickkart.entity.CartItem;
import com.kickkart.entity.Product;
import com.kickkart.exception.BadRequestException;
import com.kickkart.exception.ResourceNotFoundException;
import com.kickkart.repository.CartRepository;
import com.kickkart.repository.ProductRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class PaymentService {

    @Value("${razorpay.key.id:rzp_test_TOorQ6SD8YpZfV}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret:R7QhONfJggKs2rBNgiadXKfm}")
    private String razorpayKeySecret;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderService orderService;

    public RazorpayOrderResponse createRazorpayOrder(Long userId, RazorpayOrderRequest request) {
        List<CartItem> cartItems = cartRepository.findByUserId(userId);
        if (cartItems.isEmpty()) {
            throw new BadRequestException("Your cart is empty. Cannot initiate payment.");
        }

        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItem item : cartItems) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + item.getProductId()));

            if (product.getStock() < item.getQuantity()) {
                throw new BadRequestException("Insufficient stock for product '" + product.getName() + "'. Available: " + product.getStock());
            }

            BigDecimal itemSubtotal = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            subtotal = subtotal.add(itemSubtotal);
        }

        BigDecimal gst = subtotal.multiply(new BigDecimal("0.18")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal grandTotal = subtotal.add(gst).setScale(2, RoundingMode.HALF_UP);

        long amountInPaise = grandTotal.multiply(new BigDecimal("100")).longValue();

        try {
            RazorpayClient razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "kk_ord_" + System.currentTimeMillis());

            Order razorpayOrder = razorpayClient.orders.create(orderRequest);
            String razorpayOrderId = razorpayOrder.get("id");

            return new RazorpayOrderResponse(
                    razorpayKeyId,
                    razorpayOrderId,
                    amountInPaise,
                    subtotal,
                    gst,
                    grandTotal
            );
        } catch (Exception e) {
            throw new BadRequestException("Failed to create Razorpay Order: " + e.getMessage());
        }
    }

    public OrderDto verifyAndCompletePayment(Long userId, RazorpayVerificationRequest request) {
        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_payment_id", request.getRazorpayPaymentId());
            options.put("razorpay_order_id", request.getRazorpayOrderId());
            options.put("razorpay_signature", request.getRazorpaySignature());

            boolean isValidSignature = Utils.verifyPaymentSignature(options, razorpayKeySecret);

            if (!isValidSignature) {
                throw new BadRequestException("Invalid Razorpay payment signature. Payment verification failed.");
            }
        } catch (BadRequestException bre) {
            throw bre;
        } catch (Exception e) {
            throw new BadRequestException("Signature verification error: " + e.getMessage());
        }

        // Complete Order & Stock deduction in transactional OrderService
        return orderService.createOrderWithRazorpayPayment(userId, request);
    }
}
