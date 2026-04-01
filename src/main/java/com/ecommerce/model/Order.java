package com.ecommerce.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"user", "orderItems"})
@EqualsAndHashCode(exclude = {"user", "orderItems"})
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<OrderItem> orderItems = new HashSet<>();

    @Column(nullable = false)
    private BigDecimal totalPrice;

    @Column(nullable = false, columnDefinition = "VARCHAR(50) DEFAULT 'PENDING'")
    @Builder.Default
    private String orderStatus = "PENDING";

    @Column(nullable = false, columnDefinition = "VARCHAR(50) DEFAULT 'UNPAID'")
    @Builder.Default
    private String paymentStatus = "UNPAID";

    @Column(nullable = false)
    private String shippingAddress;

    private String shippingCity;

    private String shippingState;

    private String shippingZipCode;

    private String shippingCountry;

    private String paymentMethod;

    private String stripePaymentIntentId;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Long createdAt = System.currentTimeMillis();

    private Long updatedAt;
}
