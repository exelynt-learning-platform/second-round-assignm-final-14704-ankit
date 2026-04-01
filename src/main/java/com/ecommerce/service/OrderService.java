package com.ecommerce.service;

import com.ecommerce.dto.CreateOrderRequest;
import com.ecommerce.dto.OrderDTO;
import com.ecommerce.dto.OrderItemDTO;
import com.ecommerce.dto.ProductDTO;
import com.ecommerce.exception.InvalidRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.*;
import com.ecommerce.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;


@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public OrderDTO createOrderFromCart(Long userId, CreateOrderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        if (cart.getCartItems().isEmpty()) {
            throw new InvalidRequestException("Cannot create order from an empty cart");
        }

        BigDecimal totalPrice = cart.getTotalPrice();

        Order order = Order.builder()
                .user(user)
                .totalPrice(totalPrice)
                .orderStatus("PENDING")
                .paymentStatus("UNPAID")
                .shippingAddress(request.getShippingAddress())
                .shippingCity(request.getShippingCity())
                .shippingState(request.getShippingState())
                .shippingZipCode(request.getShippingZipCode())
                .shippingCountry(request.getShippingCountry())
                .paymentMethod(request.getPaymentMethod())
                .build();

        Set<OrderItem> orderItems = new HashSet<>();
        for (CartItem cartItem : cart.getCartItems()) {
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .products(cartItem.getProducts())
                    .quantity(cartItem.getQuantity())
                    .pricePerUnit(cartItem.getProducts().iterator().next().getPrice())
                    .build();
            orderItems.add(orderItem);
        }

        order.setOrderItems(orderItems);

        // Reduce product stock
        for (CartItem cartItem : cart.getCartItems()) {
            for (Product product : cartItem.getProducts()) {
                product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
                productRepository.save(product);
            }
        }

        order = orderRepository.save(order);

        // Clear cart after order creation
        cart.getCartItems().clear();
        cartRepository.save(cart);

        return convertToDTO(order);
    }

    public OrderDTO getOrderById(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getUser().getId().equals(userId)) {
            throw new InvalidRequestException("Unauthorized access to this order");
        }

        return convertToDTO(order);
    }

    public List<OrderDTO> getUserOrders(Long userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .toList();
    }

    public OrderDTO updateOrderStatus(Long orderId, String orderStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!isValidOrderStatus(orderStatus)) {
            throw new InvalidRequestException("Invalid order status");
        }

        order.setOrderStatus(orderStatus);
        order.setUpdatedAt(System.currentTimeMillis());
        order = orderRepository.save(order);

        return convertToDTO(order);
    }

    public OrderDTO updatePaymentStatus(Long orderId, String paymentStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!isValidPaymentStatus(paymentStatus)) {
            throw new InvalidRequestException("Invalid payment status");
        }

        order.setPaymentStatus(paymentStatus);
        order.setUpdatedAt(System.currentTimeMillis());
        order = orderRepository.save(order);

        return convertToDTO(order);
    }

    private boolean isValidOrderStatus(String status) {
        return List.of("PENDING", "CONFIRMED", "SHIPPED", "DELIVERED", "CANCELLED").contains(status);
    }

    private boolean isValidPaymentStatus(String status) {
        return List.of("UNPAID", "PAID", "FAILED", "REFUNDED").contains(status);
    }

    private OrderDTO convertToDTO(Order order) {
        List<OrderItemDTO> orderItemDTOs = order.getOrderItems().stream()
                .map(this::convertOrderItemToDTO)
                .toList();

        return OrderDTO.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .orderItems(orderItemDTOs)
                .totalPrice(order.getTotalPrice())
                .orderStatus(order.getOrderStatus())
                .paymentStatus(order.getPaymentStatus())
                .shippingAddress(order.getShippingAddress())
                .shippingCity(order.getShippingCity())
                .shippingState(order.getShippingState())
                .shippingZipCode(order.getShippingZipCode())
                .shippingCountry(order.getShippingCountry())
                .paymentMethod(order.getPaymentMethod())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private OrderItemDTO convertOrderItemToDTO(OrderItem orderItem) {
        List<ProductDTO> productDTOs = orderItem.getProducts().stream()
                .map(this::convertProductToDTO)
                .toList();

        return OrderItemDTO.builder()
                .id(orderItem.getId())
                .products(productDTOs)
                .quantity(orderItem.getQuantity())
                .pricePerUnit(orderItem.getPricePerUnit())
                .totalPrice(orderItem.getTotalPrice())
                .createdAt(orderItem.getCreatedAt())
                .updatedAt(orderItem.getUpdatedAt())
                .build();
    }

    private ProductDTO convertProductToDTO(Product product) {
        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .imageUrl(product.getImageUrl())
                .status(product.getStatus())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
