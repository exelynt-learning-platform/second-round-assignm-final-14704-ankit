package com.ecommerce.integration;

import com.ecommerce.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CompleteDataSeederTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void seedCompleteEcommerceData() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        
        System.out.println("\n========== SEEDING COMPLETE E-COMMERCE DATA ==========\n");

        // Step 1: Register Admin User
        System.out.println("1. Registering admin user...");
        String adminEmail = "admin_" + suffix + "@example.com";
        RegisterRequest adminRequest = RegisterRequest.builder()
                .email(adminEmail)
                .firstName("Admin")
                .lastName("User")
                .password("Admin@123")
                .build();

        MvcResult adminResult = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        AuthResponse adminAuth = objectMapper.readValue(
                adminResult.getResponse().getContentAsString(), AuthResponse.class);
        String adminToken = adminAuth.getToken();
        System.out.println("✓ Admin registered: " + adminEmail + "\n");

        // Step 2: Create Products
        System.out.println("2. Creating products...");
        Long product1Id = createProduct(adminToken, "Laptop", "High-performance laptop", "1299.99", 50);
        Long product2Id = createProduct(adminToken, "Smartphone", "Latest smartphone", "899.99", 100);
        Long product3Id = createProduct(adminToken, "Headphones", "Wireless headphones", "199.99", 200);
        Long product4Id = createProduct(adminToken, "Tablet", "10-inch tablet", "499.99", 75);
        Long product5Id = createProduct(adminToken, "Smartwatch", "Fitness smartwatch", "299.99", 150);
        System.out.println("✓ 5 products created\n");

        // Step 3: Register Customer 1
        System.out.println("3. Registering customer 1...");
        String customer1Email = "customer1_" + suffix + "@example.com";
        RegisterRequest customer1Request = RegisterRequest.builder()
                .email(customer1Email)
                .firstName("John")
                .lastName("Doe")
                .password("Customer@123")
                .build();

        MvcResult customer1Result = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customer1Request)))
                .andExpect(status().isCreated())
                .andReturn();

        AuthResponse customer1Auth = objectMapper.readValue(
                customer1Result.getResponse().getContentAsString(), AuthResponse.class);
        String customer1Token = customer1Auth.getToken();
        System.out.println("✓ Customer 1 registered: " + customer1Email + "\n");

        // Step 4: Customer 1 adds items to cart
        System.out.println("4. Customer 1 adding items to cart...");
        addToCart(customer1Token, product1Id, 1);
        addToCart(customer1Token, product2Id, 2);
        addToCart(customer1Token, product3Id, 1);
        System.out.println("✓ 3 items added to cart\n");

        // Step 5: Customer 1 creates order
        System.out.println("5. Customer 1 creating order...");
        Long order1Id = createOrder(customer1Token, "123 Main St", "New York", "NY", "10001", "USA");
        System.out.println("✓ Order created: Order ID " + order1Id + "\n");

        // Step 6: Register Customer 2
        System.out.println("6. Registering customer 2...");
        String customer2Email = "customer2_" + suffix + "@example.com";
        RegisterRequest customer2Request = RegisterRequest.builder()
                .email(customer2Email)
                .firstName("Jane")
                .lastName("Smith")
                .password("Customer@123")
                .build();

        MvcResult customer2Result = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customer2Request)))
                .andExpect(status().isCreated())
                .andReturn();

        AuthResponse customer2Auth = objectMapper.readValue(
                customer2Result.getResponse().getContentAsString(), AuthResponse.class);
        String customer2Token = customer2Auth.getToken();
        System.out.println("✓ Customer 2 registered: " + customer2Email + "\n");

        // Step 7: Customer 2 adds items to cart
        System.out.println("7. Customer 2 adding items to cart...");
        addToCart(customer2Token, product4Id, 1);
        addToCart(customer2Token, product5Id, 3);
        System.out.println("✓ 2 items added to cart\n");

        // Step 8: Customer 2 creates order
        System.out.println("8. Customer 2 creating order...");
        Long order2Id = createOrder(customer2Token, "456 Oak Ave", "Los Angeles", "CA", "90001", "USA");
        System.out.println("✓ Order created: Order ID " + order2Id + "\n");

        // Step 9: Register Customer 3 with active cart
        System.out.println("9. Registering customer 3...");
        String customer3Email = "customer3_" + suffix + "@example.com";
        RegisterRequest customer3Request = RegisterRequest.builder()
                .email(customer3Email)
                .firstName("Bob")
                .lastName("Johnson")
                .password("Customer@123")
                .build();

        MvcResult customer3Result = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customer3Request)))
                .andExpect(status().isCreated())
                .andReturn();

        AuthResponse customer3Auth = objectMapper.readValue(
                customer3Result.getResponse().getContentAsString(), AuthResponse.class);
        String customer3Token = customer3Auth.getToken();
        System.out.println("✓ Customer 3 registered: " + customer3Email + "\n");

        // Step 10: Customer 3 adds items to cart (but doesn't order yet)
        System.out.println("10. Customer 3 adding items to cart (active cart)...");
        addToCart(customer3Token, product1Id, 1);
        addToCart(customer3Token, product3Id, 2);
        addToCart(customer3Token, product5Id, 1);
        System.out.println("✓ 3 items in active cart\n");

        System.out.println("========== DATA SEEDING COMPLETE ==========\n");
        System.out.println("Database: ecommerce_db");
        System.out.println("\nRecords created:");
        System.out.println("- Users: 3 (1 admin, 3 customers)");
        System.out.println("- Products: 5");
        System.out.println("- Carts: 3 (2 converted to orders, 1 active)");
        System.out.println("- Cart Items: 3 (in active cart)");
        System.out.println("- Orders: 2");
        System.out.println("- Order Items: 5 (3 in order 1, 2 in order 2)");
        System.out.println("\nVerify with SQL:");
        System.out.println("  SELECT * FROM products;");
        System.out.println("  SELECT * FROM carts;");
        System.out.println("  SELECT * FROM cart_items;");
        System.out.println("  SELECT * FROM orders;");
        System.out.println("  SELECT * FROM order_items;");
        System.out.println("  SELECT * FROM users WHERE email LIKE '%" + suffix + "%';");
    }

    private Long createProduct(String adminToken, String name, String description, 
                               String price, int stock) throws Exception {
        ProductDTO productDTO = ProductDTO.builder()
                .name(name)
                .description(description)
                .price(new BigDecimal(price))
                .stockQuantity(stock)
                .imageUrl("http://example.com/" + name.toLowerCase() + ".jpg")
                .build();

        MvcResult result = mockMvc.perform(post("/products")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isCreated())
                .andReturn();

        ProductDTO created = objectMapper.readValue(
                result.getResponse().getContentAsString(), ProductDTO.class);
        System.out.println("  - Created: " + name + " (ID: " + created.getId() + ")");
        return created.getId();
    }

    private void addToCart(String token, Long productId, int quantity) throws Exception {
        AddToCartRequest request = AddToCartRequest.builder()
                .productId(productId)
                .quantity(quantity)
                .build();

        mockMvc.perform(post("/cart/items")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        
        System.out.println("  - Added product " + productId + " (qty: " + quantity + ")");
    }

    private Long createOrder(String token, String address, String city, 
                            String state, String zip, String country) throws Exception {
        CreateOrderRequest request = CreateOrderRequest.builder()
                .shippingAddress(address)
                .shippingCity(city)
                .shippingState(state)
                .shippingZipCode(zip)
                .shippingCountry(country)
                .paymentMethod("CARD")
                .build();

        MvcResult result = mockMvc.perform(post("/orders")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        OrderDTO order = objectMapper.readValue(
                result.getResponse().getContentAsString(), OrderDTO.class);
        return order.getId();
    }
}
