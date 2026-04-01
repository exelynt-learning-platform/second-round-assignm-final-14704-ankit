package com.ecommerce.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "cart_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = "cart")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "cart_item_product",
            joinColumns = @JoinColumn(name = "cart_item_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    @Builder.Default
    private Set<Product> products = new HashSet<>();

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Long createdAt = System.currentTimeMillis();

    private Long updatedAt;

    public BigDecimal getTotalPrice() {
        return products.stream()
                .map(p -> p.getPrice().multiply(new BigDecimal(quantity)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
