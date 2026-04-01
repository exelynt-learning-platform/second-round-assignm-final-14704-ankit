package com.ecommerce.controller;

import com.ecommerce.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String authToken;
    private Long userId;

    private String uniqueEmail;

    @BeforeEach
    public void setUp() throws Exception {
        uniqueEmail = "testuser_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email(uniqueEmail)
                .firstName("Test")
                .lastName("User")
                .password("testPassword123")
                .build();

        MvcResult registerResult = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        AuthResponse authResponse = objectMapper.readValue(registerResult.getResponse().getContentAsString(), AuthResponse.class);
        authToken = authResponse.getToken();
        userId = authResponse.getUserId();
    }

    // ==================== AUTHENTICATION API TESTS ====================

    @Test
    public void testRegisterNewUser() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("newuser_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com")
                .firstName("New")
                .lastName("User")
                .password("password123")
                .build();

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.firstName").value("New"));
    }

    @Test
    public void testLoginUser() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email(uniqueEmail)
                .password("testPassword123")
                .build();

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value(uniqueEmail));
    }

    @Test
    public void testLoginWithInvalidCredentials() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email(uniqueEmail)
                .password("wrongPassword")
                .build();

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ==================== PRODUCT API TESTS ====================

    @Test
    public void testGetAllProducts() throws Exception {
        mockMvc.perform(get("/products")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    public void testCreateProductAsNonAdmin() throws Exception {
        ProductDTO productRequest = ProductDTO.builder()
                .name("Test Product")
                .description("Test Description")
                .price(java.math.BigDecimal.valueOf(99.99))
                .stockQuantity(10)
                .imageUrl("http://example.com/product.jpg")
                .build();

        mockMvc.perform(post("/products")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().is(403));
    }

    @Test
    public void testGetProductById() throws Exception {
        // First, get all products
        MvcResult allProductsResult = mockMvc.perform(get("/products")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String response = allProductsResult.getResponse().getContentAsString();
        ProductDTO[] products = objectMapper.readValue(response, ProductDTO[].class);

        if (products.length > 0) {
            Long productId = products[0].getId();
            mockMvc.perform(get("/products/{productId}", productId)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(productId));
        }
    }

    // ==================== CART API TESTS ====================

    @Test
    public void testGetCart() throws Exception {
        mockMvc.perform(get("/cart")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId));
    }

    @Test
    public void testAddToCart() throws Exception {
        // Get a product first
        MvcResult allProductsResult = mockMvc.perform(get("/products")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String response = allProductsResult.getResponse().getContentAsString();
        ProductDTO[] products = objectMapper.readValue(response, ProductDTO[].class);

        if (products.length > 0) {
            Long productId = products[0].getId();

            AddToCartRequest addToCartRequest = AddToCartRequest.builder()
                    .productId(productId)
                    .quantity(2)
                    .build();

            mockMvc.perform(post("/cart/items")
                    .header("Authorization", "Bearer " + authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(addToCartRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cartItems").isArray());
        }
    }

    @Test
    public void testAddToCartWithoutAuth() throws Exception {
        AddToCartRequest request = AddToCartRequest.builder()
                .productId(1L)
                .quantity(2)
                .build();

        mockMvc.perform(post("/cart/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ==================== ORDER API TESTS ====================

    @Test
    public void testGetUserOrders() throws Exception {
        mockMvc.perform(get("/orders")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    public void testCreateOrder() throws Exception {
        // First add item to cart
        MvcResult allProductsResult = mockMvc.perform(get("/products")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String response = allProductsResult.getResponse().getContentAsString();
        ProductDTO[] products = objectMapper.readValue(response, ProductDTO[].class);

        if (products.length > 0) {
            Long productId = products[0].getId();

            AddToCartRequest addToCartRequest = AddToCartRequest.builder()
                    .productId(productId)
                    .quantity(1)
                    .build();

            mockMvc.perform(post("/cart/items")
                    .header("Authorization", "Bearer " + authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(addToCartRequest)))
                    .andExpect(status().isOk());

            // Now create order
            CreateOrderRequest createOrderRequest = CreateOrderRequest.builder()
                    .shippingAddress("123 Main St")
                    .shippingCity("New York")
                    .shippingState("NY")
                    .shippingZipCode("10001")
                    .shippingCountry("USA")
                    .paymentMethod("CARD")
                    .build();

            mockMvc.perform(post("/orders")
                    .header("Authorization", "Bearer " + authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createOrderRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.orderStatus").value("PENDING"))
                    .andExpect(jsonPath("$.paymentStatus").value("UNPAID"));
        }
    }

    @Test
    public void testCreateOrderWithoutAuth() throws Exception {
        CreateOrderRequest request = CreateOrderRequest.builder()
                .shippingAddress("123 Main St")
                .shippingCity("New York")
                .shippingState("NY")
                .shippingZipCode("10001")
                .shippingCountry("USA")
                .paymentMethod("CARD")
                .build();

        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetOrderById() throws Exception {
        // Get user orders
        MvcResult ordersResult = mockMvc.perform(get("/orders")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String response = ordersResult.getResponse().getContentAsString();
        OrderDTO[] orders = objectMapper.readValue(response, OrderDTO[].class);

        if (orders.length > 0) {
            Long orderId = orders[0].getId();
            mockMvc.perform(get("/orders/{orderId}", orderId)
                    .header("Authorization", "Bearer " + authToken)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(orderId));
        }
    }

    // ==================== PAYMENT API TESTS ====================

    @Test
    public void testProcessPayment() throws Exception {
        PaymentRequest paymentRequest = PaymentRequest.builder()
                .orderId(999L) // Non-existent order for testing
                .build();

        mockMvc.perform(post("/payments/process")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testProcessPaymentWithoutAuth() throws Exception {
        PaymentRequest request = PaymentRequest.builder()
                .orderId(1L)
                .build();

        mockMvc.perform(post("/payments/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ==================== VALIDATION TESTS ====================

    @Test
    public void testRegisterWithInvalidEmail() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("") // Empty email
                .firstName("Test")
                .lastName("User")
                .password("password123")
                .build();

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testAddToCartWithInvalidProductId() throws Exception {
        AddToCartRequest request = AddToCartRequest.builder()
                .productId(-1L) // Invalid
                .quantity(1)
                .build();

        mockMvc.perform(post("/cart/items")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUnauthorizedAccess() throws Exception {
        mockMvc.perform(get("/cart")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
