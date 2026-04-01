package com.ecommerce.controller;

import com.ecommerce.dto.PaymentRequest;
import com.ecommerce.dto.PaymentResponse;
import com.ecommerce.service.PaymentService;
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
@RequestMapping("/payments")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Payments", description = "Payment processing endpoints")
public class PaymentController {

    private final PaymentService paymentService;
    private final SecurityUtil securityUtil;

    @PostMapping("/process")
    @Operation(summary = "Process payment", description = "Process a payment for an order using Stripe")
    @ApiResponse(responseCode = "200", description = "Payment processed successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request - order already paid or payment failed")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<PaymentResponse> processPayment(HttpServletRequest request,
                                                         @Valid @RequestBody PaymentRequest paymentRequest) {
        Long userId = securityUtil.extractUserIdFromRequest(request);
        PaymentResponse response = paymentService.processPayment(paymentRequest.getOrderId(), userId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/confirm/{paymentIntentId}")
    @Operation(summary = "Confirm payment", description = "Confirm a Stripe payment intent")
    @Parameter(name = "paymentIntentId", description = "Stripe Payment Intent ID", required = true)
    @ApiResponse(responseCode = "200", description = "Payment confirmed successfully")
    @ApiResponse(responseCode = "404", description = "Payment intent not found")
    public ResponseEntity<PaymentResponse> confirmPayment(@PathVariable String paymentIntentId) {
        PaymentResponse response = paymentService.confirmPayment(paymentIntentId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/{paymentId}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Refund payment", description = "Refund a processed payment (Admin only)")
    @Parameter(name = "paymentId", description = "Payment ID", required = true)
    @ApiResponse(responseCode = "204", description = "Payment refunded successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "404", description = "Payment not found")
    public ResponseEntity<Void> refundPayment(@PathVariable Long paymentId) {
        paymentService.refundPayment(paymentId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


}
