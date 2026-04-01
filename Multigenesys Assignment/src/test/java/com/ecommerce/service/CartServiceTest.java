package com.ecommerce.service;

import com.ecommerce.dto.AddToCartRequest;
import com.ecommerce.dto.CartDTO;
import com.ecommerce.exception.InvalidRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.*;
import com.ecommerce.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartService cartService;

    private User testUser;
    private Product testProduct;
    private Cart testCart;
    private AddToCartRequest addToCartRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        testProduct = Product.builder()
                .id(1L)
                .name("Test Product")
                .price(new BigDecimal("99.99"))
                .stockQuantity(100)
                .build();

        testCart = Cart.builder()
                .id(1L)
                .user(testUser)
                .cartItems(new HashSet<>())
                .build();

        addToCartRequest = AddToCartRequest.builder()
                .productId(1L)
                .quantity(2)
                .build();
    }

    @Test
    void testAddToCartSuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        CartDTO cartDTO = cartService.addToCart(1L, addToCartRequest);

        assertNotNull(cartDTO);
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void testAddToCartInsufficientStock() {
        testProduct.setStockQuantity(1);
        addToCartRequest.setQuantity(5);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        assertThrows(InvalidRequestException.class, () -> cartService.addToCart(1L, addToCartRequest));
    }

    @Test
    void testGetCartSuccess() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));

        CartDTO cartDTO = cartService.getCart(1L);

        assertNotNull(cartDTO);
        assertEquals(1L, cartDTO.getId());
    }

    @Test
    void testGetCartNotFound() {
        when(cartRepository.findByUserId(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cartService.getCart(999L));
    }

    @Test
    void testRemoveFromCartSuccess() {
        CartItem cartItem = CartItem.builder()
                .id(1L)
                .cart(testCart)
                .products(new HashSet<>(Collections.singletonList(testProduct)))
                .quantity(2)
                .build();

        testCart.getCartItems().add(cartItem);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(cartItem));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        CartDTO cartDTO = cartService.removeFromCart(1L, 1L);

        assertNotNull(cartDTO);
        verify(cartItemRepository, times(1)).delete(cartItem);
    }

    @Test
    void testClearCartSuccess() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        cartService.clearCart(1L);

        assertTrue(testCart.getCartItems().isEmpty());
        verify(cartRepository, times(1)).save(any(Cart.class));
    }
}
