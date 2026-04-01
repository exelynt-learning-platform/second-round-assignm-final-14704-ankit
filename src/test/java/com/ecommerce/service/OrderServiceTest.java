package com.ecommerce.service;

import com.ecommerce.dto.CreateOrderRequest;
import com.ecommerce.dto.OrderDTO;
import com.ecommerce.exception.InvalidRequestException;
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
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private OrderService orderService;

    private User testUser;
    private Product testProduct;
    private Cart testCart;
    private CreateOrderRequest createOrderRequest;

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

        CartItem cartItem = CartItem.builder()
                .id(1L)
                .quantity(2)
                .products(new HashSet<>(Collections.singletonList(testProduct)))
                .build();

        testCart = Cart.builder()
                .id(1L)
                .user(testUser)
                .cartItems(new HashSet<>(Collections.singletonList(cartItem)))
                .build();

        createOrderRequest = CreateOrderRequest.builder()
                .shippingAddress("123 Main St")
                .shippingCity("New York")
                .shippingState("NY")
                .shippingZipCode("10001")
                .shippingCountry("USA")
                .paymentMethod("STRIPE")
                .build();
    }

    @Test
    void testCreateOrderFromCartSuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        OrderDTO orderDTO = orderService.createOrderFromCart(1L, createOrderRequest);

        assertNotNull(orderDTO);
        assertEquals("PENDING", orderDTO.getOrderStatus());
        assertEquals("UNPAID", orderDTO.getPaymentStatus());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void testCreateOrderFromEmptyCartFailure() {
        testCart.getCartItems().clear();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));

        assertThrows(InvalidRequestException.class, 
                () -> orderService.createOrderFromCart(1L, createOrderRequest));
    }

    @Test
    void testGetOrderByIdSuccess() {
        Order order = Order.builder()
                .id(1L)
                .user(testUser)
                .totalPrice(new BigDecimal("199.98"))
                .orderStatus("PENDING")
                .paymentStatus("UNPAID")
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderDTO orderDTO = orderService.getOrderById(1L, 1L);

        assertNotNull(orderDTO);
        assertEquals(1L, orderDTO.getId());
    }

    @Test
    void testGetOrderByIdUnauthorized() {
        Order order = Order.builder()
                .id(1L)
                .user(testUser)
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(InvalidRequestException.class, 
                () -> orderService.getOrderById(1L, 999L));
    }

    @Test
    void testGetUserOrdersSuccess() {
        Order order = Order.builder()
                .id(1L)
                .user(testUser)
                .totalPrice(new BigDecimal("199.98"))
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(orderRepository.findByUserId(1L)).thenReturn(List.of(order));

        List<OrderDTO> orders = orderService.getUserOrders(1L);

        assertNotNull(orders);
        assertEquals(1, orders.size());
    }

    @Test
    void testUpdateOrderStatusSuccess() {
        Order order = Order.builder()
                .id(1L)
                .user(testUser)
                .orderStatus("PENDING")
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderDTO orderDTO = orderService.updateOrderStatus(1L, "CONFIRMED");

        assertNotNull(orderDTO);
        assertEquals("CONFIRMED", orderDTO.getOrderStatus());
    }

    @Test
    void testUpdateOrderStatusInvalid() {
        Order order = Order.builder()
                .id(1L)
                .user(testUser)
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(InvalidRequestException.class, 
                () -> orderService.updateOrderStatus(1L, "INVALID_STATUS"));
    }
}
