package com.ecommerce.controller;

import com.ecommerce.dto.AddToCartRequest;
import com.ecommerce.dto.CartDTO;
import com.ecommerce.service.CartService;
import com.ecommerce.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Shopping Cart", description = "Shopping cart management endpoints")
public class CartController {

    private final CartService cartService;
    private final SecurityUtil securityUtil;

    @PostMapping("/items")
    @Operation(summary = "Add item to cart", description = "Add a product to the user's shopping cart")
    @ApiResponse(responseCode = "200", description = "Item added to cart successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<CartDTO> addToCart(HttpServletRequest request,
                                             @Valid @RequestBody AddToCartRequest addToCartRequest) {
        Long userId = securityUtil.extractUserIdFromRequest(request);
        CartDTO cartDTO = cartService.addToCart(userId, addToCartRequest);
        return new ResponseEntity<>(cartDTO, HttpStatus.OK);
    }

    @GetMapping
    @Operation(summary = "Get cart", description = "Retrieve the current user's shopping cart")
    @ApiResponse(responseCode = "200", description = "Cart retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<CartDTO> getCart(HttpServletRequest request) {
        Long userId = securityUtil.extractUserIdFromRequest(request);
        CartDTO cartDTO = cartService.getCart(userId);
        return new ResponseEntity<>(cartDTO, HttpStatus.OK);
    }

    @PutMapping("/items/{cartItemId}")
    @Operation(summary = "Update cart item", description = "Update the quantity of a specific cart item")
    @Parameter(name = "cartItemId", description = "Cart item ID", required = true)
    @Parameter(name = "quantity", description = "New quantity", required = true)
    @ApiResponse(responseCode = "200", description = "Cart item updated successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<CartDTO> updateCartItem(HttpServletRequest request,
                                                  @PathVariable Long cartItemId,
                                                  @RequestParam Integer quantity) {
        Long userId = securityUtil.extractUserIdFromRequest(request);
        CartDTO cartDTO = cartService.updateCartItem(userId, cartItemId, quantity);
        return new ResponseEntity<>(cartDTO, HttpStatus.OK);
    }

    @DeleteMapping("/items/{cartItemId}")
    @Operation(summary = "Remove item from cart", description = "Delete a specific item from the user's shopping cart")
    @Parameter(name = "cartItemId", description = "Cart item ID", required = true)
    @ApiResponse(responseCode = "200", description = "Item removed from cart successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<CartDTO> removeFromCart(HttpServletRequest request,
                                                  @PathVariable Long cartItemId) {
        Long userId = securityUtil.extractUserIdFromRequest(request);
        CartDTO cartDTO = cartService.removeFromCart(userId, cartItemId);
        return new ResponseEntity<>(cartDTO, HttpStatus.OK);
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(HttpServletRequest request) {
        Long userId = securityUtil.extractUserIdFromRequest(request);
        cartService.clearCart(userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


}
