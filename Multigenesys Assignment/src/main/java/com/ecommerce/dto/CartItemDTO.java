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
public class CartItemDTO {

    private Long id;

    private List<ProductDTO> products;

    private Integer quantity;

    private BigDecimal totalPrice;

    private Long createdAt;

    private Long updatedAt;
}
