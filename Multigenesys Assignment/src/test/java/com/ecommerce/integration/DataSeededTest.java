package com.ecommerce.integration;

import com.ecommerce.dto.LoginRequest;
import com.ecommerce.dto.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class DataSeededTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void seedTestDataIntoDatabase() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String email1 = "seeduser1_" + suffix + "@example.com";
        String email2 = "seeduser2_" + suffix + "@example.com";
        String emailAdmin = "seedadmin_" + suffix + "@example.com";

        System.out.println("\n========== SEEDING TEST DATA INTO MYSQL DATABASE ==========\n");

        System.out.println("1. Registering test user 1...");
        RegisterRequest user1 = RegisterRequest.builder()
                .email(email1).firstName("Seed").lastName("User1").password("SeedPassword@123").build();
        MvcResult result1 = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user1)))
                .andExpect(status().isCreated()).andReturn();
        System.out.println("✓ User 1 registered: " + email1 + "\n");

        System.out.println("2. Registering test user 2...");
        RegisterRequest user2 = RegisterRequest.builder()
                .email(email2).firstName("Seed").lastName("User2").password("SeedPassword@123").build();
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user2)))
                .andExpect(status().isCreated());
        System.out.println("✓ User 2 registered: " + email2 + "\n");

        System.out.println("3. Registering admin user...");
        RegisterRequest admin = RegisterRequest.builder()
                .email(emailAdmin).firstName("Seed").lastName("Admin").password("AdminPassword@123").build();
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(admin)))
                .andExpect(status().isCreated());
        System.out.println("✓ Admin registered: " + emailAdmin + "\n");

        // Test 4: Get all products to verify they can be retrieved
        System.out.println("4. Retrieving all products...");
        mockMvc.perform(get("/products")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        System.out.println("✓ Products retrieved\n");

        System.out.println("5. Testing login with first user...");
        LoginRequest loginRequest = LoginRequest.builder()
                .email(email1).password("SeedPassword@123").build();
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
        System.out.println("✓ Login successful for " + email1 + "\n");

        System.out.println("========== DATA SEEDING COMPLETE ==========");
        System.out.println("\nTest records added to ecommerce_db:");
        System.out.println("- 3 users (" + email1 + ", " + email2 + ", " + emailAdmin + ")");
        System.out.println("- Products available for testing");
        System.out.println("\nYou can now run full integration tests or verify data in MySQL:");
        System.out.println("  SELECT * FROM users WHERE email LIKE 'seed%';");
        System.out.println("  SELECT * FROM products;");
    }
}
