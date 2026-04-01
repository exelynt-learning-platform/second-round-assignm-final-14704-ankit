package com.ecommerce.service;

import com.ecommerce.dto.AddToCartRequest;
import com.ecommerce.dto.CartDTO;
import com.ecommerce.dto.CartItemDTO;
import com.ecommerce.dto.ProductDTO;
import com.ecommerce.exception.InvalidRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Cart;
import com.ecommerce.model.CartItem;
import com.ecommerce.model.Product;
import com.ecommerce.model.User;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public CartDTO addToCart(Long userId, AddToCartRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (product.getStockQuantity() < request.getQuantity()) {
            throw new InvalidRequestException("Insufficient stock available");
        }

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> Cart.builder()
                        .user(user)
                        .cartItems(new HashSet<>())
                        .build());

        Optional<CartItem> existingItem = cart.getCartItems().stream()
                .filter(item -> item.getProducts().contains(product))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            item.setUpdatedAt(System.currentTimeMillis());
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .products(new HashSet<>(Collections.singletonList(product)))
                    .quantity(request.getQuantity())
                    .build();
            cart.getCartItems().add(newItem);
        }

        cart.setUpdatedAt(System.currentTimeMillis());
        cart = cartRepository.save(cart);

        return convertToDTO(cart);
    }

    public CartDTO getCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user"));

        return convertToDTO(cart);
    }

    public CartDTO updateCartItem(Long userId, Long cartItemId, Integer newQuantity) {
        if (newQuantity <= 0) {
            throw new InvalidRequestException("Quantity must be greater than 0");
        }

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        if (!cart.getCartItems().contains(cartItem)) {
            throw new InvalidRequestException("Cart item does not belong to this cart");
        }

        Product product = cartItem.getProducts().iterator().next();
        if (product.getStockQuantity() < newQuantity) {
            throw new InvalidRequestException("Insufficient stock available");
        }

        cartItem.setQuantity(newQuantity);
        cartItem.setUpdatedAt(System.currentTimeMillis());
        cartItemRepository.save(cartItem);

        cart.setUpdatedAt(System.currentTimeMillis());
        cart = cartRepository.save(cart);

        return convertToDTO(cart);
    }

    public CartDTO removeFromCart(Long userId, Long cartItemId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        if (!cart.getCartItems().contains(cartItem)) {
            throw new InvalidRequestException("Cart item does not belong to this cart");
        }

        cart.getCartItems().remove(cartItem);
        cartItemRepository.delete(cartItem);

        cart.setUpdatedAt(System.currentTimeMillis());
        cart = cartRepository.save(cart);

        return convertToDTO(cart);
    }

    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        cart.getCartItems().clear();
        cart.setUpdatedAt(System.currentTimeMillis());
        cartRepository.save(cart);
    }

    private CartDTO convertToDTO(Cart cart) {
        List<CartItemDTO> cartItemDTOs = cart.getCartItems().stream()
                .map(this::convertCartItemToDTO)
                .toList();

        return CartDTO.builder()
                .id(cart.getId())
                .cartItems(cartItemDTOs)
                .totalPrice(cart.getTotalPrice())
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }

    private CartItemDTO convertCartItemToDTO(CartItem cartItem) {
        List<ProductDTO> productDTOs = cartItem.getProducts().stream()
                .map(this::convertProductToDTO)
                .toList();

        return CartItemDTO.builder()
                .id(cartItem.getId())
                .products(productDTOs)
                .quantity(cartItem.getQuantity())
                .totalPrice(cartItem.getTotalPrice())
                .createdAt(cartItem.getCreatedAt())
                .updatedAt(cartItem.getUpdatedAt())
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
