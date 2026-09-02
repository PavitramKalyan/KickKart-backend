package com.kickkart.service;

import com.kickkart.dto.OrderDto;
import com.kickkart.dto.OrderItemDto;
import com.kickkart.dto.OrderManagementResponse;
import com.kickkart.dto.OrderProductResponse;
import com.kickkart.dto.OrderRequest;
import com.kickkart.dto.ProductDto;
import com.kickkart.dto.RazorpayVerificationRequest;
import com.kickkart.entity.CartItem;
import com.kickkart.entity.Category;
import com.kickkart.entity.Order;
import com.kickkart.entity.OrderItem;
import com.kickkart.entity.Product;
import com.kickkart.entity.ProductImage;
import com.kickkart.entity.User;
import com.kickkart.exception.BadRequestException;
import com.kickkart.exception.ResourceNotFoundException;
import com.kickkart.repository.CartRepository;
import com.kickkart.repository.CategoryRepository;
import com.kickkart.repository.OrderItemRepository;
import com.kickkart.repository.OrderRepository;
import com.kickkart.repository.ProductImageRepository;
import com.kickkart.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private ProductService productService;

    @Transactional
    public OrderDto createOrder(Long userId, OrderRequest request) {
        List<CartItem> cartItems = cartRepository.findByUserId(userId);
        if (cartItems.isEmpty()) {
            throw new BadRequestException("Your cart is empty. Cannot checkout.");
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        List<CartItemValidated> validatedList = new ArrayList<>();

        for (CartItem item : cartItems) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + item.getProductId()));

            if (product.getStock() < item.getQuantity()) {
                throw new BadRequestException("Insufficient stock for product '" + product.getName() + "'. Available: " + product.getStock());
            }

            BigDecimal itemSubtotal = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            subtotal = subtotal.add(itemSubtotal);
            validatedList.add(new CartItemValidated(product, item.getQuantity(), itemSubtotal));
        }

        BigDecimal gst = subtotal.multiply(new BigDecimal("0.18")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal grandTotal = subtotal.add(gst).setScale(2, RoundingMode.HALF_UP);

        String orderId = generateOrderId();

        Order order = new Order();
        order.setOrderId(orderId);
        order.setUserId(userId);
        order.setFullName(request.getFullName());
        order.setPhone(request.getPhone());
        order.setAddress(request.getAddress());
        order.setCity(request.getCity());
        order.setState(request.getState());
        order.setPincode(request.getPincode());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setTotalAmount(grandTotal);
        order.setGst(gst);
        order.setStatus("SUCCESS");
        order.setPaymentStatus("COMPLETED");

        orderRepository.save(order);

        List<OrderItemDto> itemDtos = new ArrayList<>();

        for (CartItemValidated itemVal : validatedList) {
            Product product = itemVal.product;

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(orderId);
            orderItem.setProductId(product.getProductId());
            orderItem.setQuantity(itemVal.quantity);
            orderItem.setPricePerUnit(product.getPrice());
            orderItem.setTotalPrice(itemVal.itemSubtotal);
            orderItemRepository.save(orderItem);

            // Reduce stock
            product.setStock(product.getStock() - itemVal.quantity);
            productRepository.save(product);

            ProductDto productDto = productService.convertToDto(product);
            OrderItemDto itemDto = new OrderItemDto();
            itemDto.setId(orderItem.getId());
            itemDto.setProductId(product.getProductId());
            itemDto.setProductCode(productDto.getProductId());
            itemDto.setProductName(product.getName());
            itemDto.setImageUrl(productDto.getImageUrl());
            itemDto.setQuantity(itemVal.quantity);
            itemDto.setPricePerUnit(product.getPrice());
            itemDto.setTotalPrice(itemVal.itemSubtotal);
            itemDtos.add(itemDto);
        }

        // Clear user cart
        cartRepository.deleteByUserId(userId);

        OrderDto orderDto = convertToDto(order);
        orderDto.setItems(itemDtos);
        return orderDto;
    }

    @Transactional
    public OrderDto createOrderWithRazorpayPayment(Long userId, RazorpayVerificationRequest request) {
        // Idempotency check: Return existing order if already completed with this payment ID
        if (request.getRazorpayPaymentId() != null) {
            Order existingPayment = orderRepository.findByRazorpayPaymentId(request.getRazorpayPaymentId()).orElse(null);
            if (existingPayment != null) {
                return getOrderById(existingPayment.getOrderId(), userId);
            }
        }

        List<CartItem> cartItems = cartRepository.findByUserId(userId);
        if (cartItems.isEmpty()) {
            // Check if order was already processed via order ID
            if (request.getRazorpayOrderId() != null) {
                Order existingOrder = orderRepository.findByRazorpayOrderId(request.getRazorpayOrderId()).orElse(null);
                if (existingOrder != null) {
                    return getOrderById(existingOrder.getOrderId(), userId);
                }
            }
            throw new BadRequestException("Your cart is empty. Cannot checkout.");
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        List<CartItemValidated> validatedList = new ArrayList<>();

        for (CartItem item : cartItems) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + item.getProductId()));

            if (product.getStock() < item.getQuantity()) {
                throw new BadRequestException("Insufficient stock for product '" + product.getName() + "'. Available: " + product.getStock());
            }

            BigDecimal itemSubtotal = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            subtotal = subtotal.add(itemSubtotal);
            validatedList.add(new CartItemValidated(product, item.getQuantity(), itemSubtotal));
        }

        BigDecimal gst = subtotal.multiply(new BigDecimal("0.18")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal grandTotal = subtotal.add(gst).setScale(2, RoundingMode.HALF_UP);

        String orderId = generateOrderId();

        Order order = new Order();
        order.setOrderId(orderId);
        order.setUserId(userId);
        order.setFullName(request.getFullName());
        order.setPhone(request.getPhone());
        order.setAddress(request.getAddress());
        order.setCity(request.getCity());
        order.setState(request.getState());
        order.setPincode(request.getPincode());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setTransactionId(request.getRazorpayPaymentId());
        order.setRazorpayOrderId(request.getRazorpayOrderId());
        order.setRazorpayPaymentId(request.getRazorpayPaymentId());
        order.setRazorpaySignature(request.getRazorpaySignature());
        order.setTotalAmount(grandTotal);
        order.setGst(gst);
        order.setStatus("SUCCESS");
        order.setPaymentStatus("PAID");

        orderRepository.save(order);

        List<OrderItemDto> itemDtos = new ArrayList<>();

        for (CartItemValidated itemVal : validatedList) {
            Product product = itemVal.product;

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(orderId);
            orderItem.setProductId(product.getProductId());
            orderItem.setQuantity(itemVal.quantity);
            orderItem.setPricePerUnit(product.getPrice());
            orderItem.setTotalPrice(itemVal.itemSubtotal);
            orderItemRepository.save(orderItem);

            // Reduce stock
            product.setStock(product.getStock() - itemVal.quantity);
            productRepository.save(product);

            ProductDto productDto = productService.convertToDto(product);
            OrderItemDto itemDto = new OrderItemDto();
            itemDto.setId(orderItem.getId());
            itemDto.setProductId(product.getProductId());
            itemDto.setProductCode(productDto.getProductId());
            itemDto.setProductName(product.getName());
            itemDto.setImageUrl(productDto.getImageUrl());
            itemDto.setQuantity(itemVal.quantity);
            itemDto.setPricePerUnit(product.getPrice());
            itemDto.setTotalPrice(itemVal.itemSubtotal);
            itemDtos.add(itemDto);
        }

        // Clear user cart
        cartRepository.deleteByUserId(userId);

        OrderDto orderDto = convertToDto(order);
        orderDto.setItems(itemDtos);
        return orderDto;
    }

    public OrderManagementResponse getOrderManagementResponse(User user) {
        String role = (user.getRole() != null && !user.getRole().isEmpty()) ? user.getRole() : "CUSTOMER";
        String username = (user.getUsername() != null && !user.getUsername().isEmpty()) ? user.getUsername() : user.getEmail();

        List<Order> userOrders = orderRepository.findByUserIdOrderByCreatedAtDesc(user.getUserId());
        List<OrderProductResponse> productResponses = new ArrayList<>();

        for (Order order : userOrders) {
            // Filter for successful orders only
            if ("SUCCESS".equalsIgnoreCase(order.getStatus()) || "PAID".equalsIgnoreCase(order.getPaymentStatus()) || "COMPLETED".equalsIgnoreCase(order.getPaymentStatus())) {
                List<OrderItem> items = orderItemRepository.findByOrderId(order.getOrderId());
                for (OrderItem item : items) {
                    OrderProductResponse prodResp = new OrderProductResponse();
                    prodResp.setOrderId(order.getOrderId());
                    prodResp.setProductId(item.getProductId());
                    prodResp.setQuantity(item.getQuantity());
                    prodResp.setPricePerUnit(item.getPricePerUnit());
                    prodResp.setTotalPrice(item.getTotalPrice());
                    prodResp.setOrderStatus(order.getStatus() != null ? order.getStatus() : "SUCCESS");
                    prodResp.setOrderDate(order.getCreatedAt());

                    Product product = productRepository.findById(item.getProductId()).orElse(null);
                    if (product != null) {
                        prodResp.setName(product.getName());
                        prodResp.setDescription(product.getDescription());

                        if (product.getCategoryId() != null) {
                            Category cat = categoryRepository.findById(product.getCategoryId()).orElse(null);
                            prodResp.setCategory(cat != null ? cat.getCategoryName() : "Sneakers");
                        } else {
                            prodResp.setCategory("Sneakers");
                        }

                        ProductImage img = productImageRepository.findFirstByProductId(product.getProductId()).orElse(null);
                        if (img != null && img.getImageUrl() != null && !img.getImageUrl().trim().isEmpty()) {
                            prodResp.setImageUrl(img.getImageUrl());
                        } else {
                            prodResp.setImageUrl("https://ik.imagekit.io/contentcoder/shoes1.jpg");
                        }
                    } else {
                        prodResp.setName("Product #" + item.getProductId());
                        prodResp.setDescription("KickKart Shoe");
                        prodResp.setCategory("Sneakers");
                        prodResp.setImageUrl("https://ik.imagekit.io/contentcoder/shoes1.jpg");
                    }

                    productResponses.add(prodResp);
                }
            }
        }

        return new OrderManagementResponse(role, username, productResponses);
    }

    public List<OrderDto> getUserOrders(Long userId) {
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return orders.stream().map(order -> {
            OrderDto dto = convertToDto(order);
            List<OrderItem> items = orderItemRepository.findByOrderId(order.getOrderId());
            List<OrderItemDto> itemDtos = items.stream().map(item -> {
                OrderItemDto itemDto = new OrderItemDto();
                itemDto.setId(item.getId());
                itemDto.setProductId(item.getProductId());

                Product product = productRepository.findById(item.getProductId()).orElse(null);
                if (product != null) {
                    ProductDto pDto = productService.convertToDto(product);
                    itemDto.setProductCode(pDto.getProductId());
                    itemDto.setProductName(product.getName());
                    itemDto.setImageUrl(pDto.getImageUrl());
                } else {
                    itemDto.setProductName("Product #" + item.getProductId());
                    itemDto.setImageUrl("https://ik.imagekit.io/contentcoder/shoes1.jpg");
                }

                itemDto.setQuantity(item.getQuantity());
                itemDto.setPricePerUnit(item.getPricePerUnit());
                itemDto.setTotalPrice(item.getTotalPrice());
                return itemDto;
            }).collect(Collectors.toList());
            dto.setItems(itemDtos);
            return dto;
        }).collect(Collectors.toList());
    }

    public OrderDto getOrderById(String orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        if (!order.getUserId().equals(userId)) {
            throw new BadRequestException("Unauthorized to view this order");
        }

        OrderDto dto = convertToDto(order);
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        List<OrderItemDto> itemDtos = items.stream().map(item -> {
            OrderItemDto itemDto = new OrderItemDto();
            itemDto.setId(item.getId());
            itemDto.setProductId(item.getProductId());

            Product product = productRepository.findById(item.getProductId()).orElse(null);
            if (product != null) {
                ProductDto pDto = productService.convertToDto(product);
                itemDto.setProductCode(pDto.getProductId());
                itemDto.setProductName(product.getName());
                itemDto.setImageUrl(pDto.getImageUrl());
            }

            itemDto.setQuantity(item.getQuantity());
            itemDto.setPricePerUnit(item.getPricePerUnit());
            itemDto.setTotalPrice(item.getTotalPrice());
            return itemDto;
        }).collect(Collectors.toList());
        dto.setItems(itemDtos);
        return dto;
    }

    public OrderDto convertToDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setOrderId(order.getOrderId());
        dto.setUserId(order.getUserId());
        dto.setFullName(order.getFullName());
        dto.setPhone(order.getPhone());
        dto.setAddress(order.getAddress());
        dto.setCity(order.getCity());
        dto.setState(order.getState());
        dto.setPincode(order.getPincode());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setTransactionId(order.getTransactionId());
        dto.setRazorpayOrderId(order.getRazorpayOrderId());
        dto.setRazorpayPaymentId(order.getRazorpayPaymentId());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setGst(order.getGst());
        dto.setStatus(order.getStatus());
        dto.setCreatedAt(order.getCreatedAt());
        return dto;
    }

    private String generateOrderId() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder("ORD");
        sb.append(System.currentTimeMillis());
        Random random = new Random();
        for (int i = 0; i < 5; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private static class CartItemValidated {
        Product product;
        int quantity;
        BigDecimal itemSubtotal;

        CartItemValidated(Product product, int quantity, BigDecimal itemSubtotal) {
            this.product = product;
            this.quantity = quantity;
            this.itemSubtotal = itemSubtotal;
        }
    }
}
