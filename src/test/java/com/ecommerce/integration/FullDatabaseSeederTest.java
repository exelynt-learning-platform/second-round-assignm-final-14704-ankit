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
public class FullDatabaseSeederTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void seedAllTablesViaAPI() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        
        System.out.println("\n========== SEEDING ALL TABLES VIA API ==========\n");

        // Step 1: Register Admin User
        System.out.println("1. Creating admin user...");
        String adminEmail = "admin_" + suffix + "@example.com";
        String adminToken = registerUser(adminEmail, "Admin", "User", "Admin@123");
        System.out.println("✓ Admin created: " + adminEmail + "\n");

        // Step 2: Create Products
        System.out.println("2. Creating products...");
        Long product1 = createProduct(adminToken, "Laptop", "High-performance laptop", "1299.99", 50);
        Long product2 = createProduct(adminToken, "Smartphone", "Latest smartphone", "899.99", 100);
        Long product3 = createProduct(adminToken, "Headphones", "Wireless headphones", "199.99", 200);
        System.out.println("✓ 3 products created\n");

        // Step 3: Register Customer 1 and create order
        System.out.println("3. Creating customer 1 and order...");
        String customer1Email = "customer1_" + suffix + "@example.com";
        String customer1Token = registerUser(customer1Email, "John", "Doe", "Customer@123");
        
        // Add items to cart
        addItemToCart(customer1Token, product1, 1);
        Thread.sleep(100); // Small delay to avoid Hibernate issues
        addItemToCart(customer1Token, product2, 2);
        
        // Create order
        Long order1 = createOrder(customer1Token, "123 Main St", "New York", "NY", "10001", "USA");
        System.out.println("✓ Customer 1 and Order 1 created\n");

        // Step 4: Register Customer 2 and create order
        System.out.println("4. Creating customer 2 and order...");
        String customer2Email = "customer2_" + suffix + "@example.com";
        String customer2Token = registerUser(customer2Email, "Jane", "Smith", "Customer@123");
        
        // Add items to cart
        addItemToCart(customer2Token, product3, 3);
        
        // Create order
        Long order2 = createOrder(customer2Token, "456 Oak Ave", "Los Angeles", "CA", "90001", "USA");
        System.out.println("✓ Customer 2 and Order 2 created\n");

        // Step 5: Register Customer 3 with active cart (no order)
        System.out.println("5. Creating customer 3 with active cart...");
        String customer3Email = "customer3_" + suffix + "@example.com";
        String customer3Token = registerUser(customer3Email, "Bob", "Johnson", "Customer@123");
        
        // Add items to cart but don't create order
        addItemToCart(customer3Token, product1, 1);
        Thread.sleep(100);
        addItemToCart(customer3Token, product2, 1);
        System.out.println("✓ Customer 3 with active cart created\n");

        System.out.println("========== DATA SEEDING COMPLETE ==========\n");
        System.out.println("Database: ecommerce_db");
        System.out.println("\nRecords created via API:");
        System.out.println("- Users: 4 (1 admin, 3 customers)");
        System.out.println("- Products: 3");
        System.out.println("- Orders: 2");
        System.out.println("- Order Items: 4 (3 in order 1, 1 in order 2)");
        System.out.println("- Active Carts: 1 (customer 3)");
        System.out.println("- Active Cart Items: 2");
        System.out.println("\nVerify with SQL:");
        System.out.println("  SELECT * FROM users WHERE email LIKE '%" + suffix + "%';");
        System.out.println("  SELECT * FROM products;");
        System.out.println("  SELECT * FROM orders;");
        System.out.println("  SELECT * FROM order_items;");
        System.out.println("  SELECT * FROM carts;");
        System.out.println("  SELECT * FROM cart_items;");
    }

    private String registerUser(String email, String firstName, String lastName, String password) throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .password(password)
                .build();

        MvcResult result = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        AuthResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), AuthResponse.class);
        return response.getToken();
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

        String responseBody = result.getResponse().getContentAsString();
        // Parse the product ID from response
        return objectMapper.readTree(responseBody).get("id").asLong();
    }

    private void addItemToCart(String token, Long productId, int quantity) throws Exception {
        AddToCartRequest request = AddToCartRequest.builder()
                .productId(productId)
                .quantity(quantity)
                .build();

        mockMvc.perform(post("/cart/items")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
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
