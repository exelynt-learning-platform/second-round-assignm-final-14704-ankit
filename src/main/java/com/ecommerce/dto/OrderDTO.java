package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDTO {

    private Long id;

    private Long userId;

    private List<OrderItemDTO> orderItems;

    private BigDecimal totalPrice;

    private String orderStatus;

    private String paymentStatus;

    private String shippingAddress;

    private String shippingCity;

    private String shippingState;

    private String shippingZipCode;

    private String shippingCountry;

    private String paymentMethod;

    private Long createdAt;

    private Long updatedAt;
}
