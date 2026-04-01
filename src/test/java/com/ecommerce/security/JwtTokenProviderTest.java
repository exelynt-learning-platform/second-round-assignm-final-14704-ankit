package com.ecommerce.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() throws Exception {
        jwtTokenProvider = new JwtTokenProvider();
        
        // Use reflection to set private fields since they're injected via @Value in production
        java.lang.reflect.Field secretField = JwtTokenProvider.class.getDeclaredField("jwtSecret");
        secretField.setAccessible(true);
        secretField.set(jwtTokenProvider, "MyVerySecureSecretKeyForJWTTokenSigningThatIsAtLeast256BitsLongForHS256Algorithm");
        
        java.lang.reflect.Field expirationField = JwtTokenProvider.class.getDeclaredField("jwtExpirationMs");
        expirationField.setAccessible(true);
        expirationField.set(jwtTokenProvider, 86400000L);
    }

    @Test
    void testGenerateToken() {
        String token = jwtTokenProvider.generateToken("test@example.com", 1L);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testGetUserEmailFromToken() {
        String token = jwtTokenProvider.generateToken("test@example.com", 1L);
        String email = jwtTokenProvider.getUserEmailFromToken(token);

        assertEquals("test@example.com", email);
    }

    @Test
    void testGetUserIdFromToken() {
        String token = jwtTokenProvider.generateToken("test@example.com", 1L);
        Long userId = jwtTokenProvider.getUserIdFromToken(token);

        assertEquals(1L, userId);
    }

    @Test
    void testValidateTokenSuccess() {
        String token = jwtTokenProvider.generateToken("test@example.com", 1L);
        boolean isValid = jwtTokenProvider.validateToken(token);

        assertTrue(isValid);
    }

    @Test
    void testValidateTokenInvalid() {
        String invalidToken = "invalid.token.here";
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        assertFalse(isValid);
    }
}
