package com.ecommerce.service;

import com.ecommerce.dto.PaymentResponse;
import com.ecommerce.exception.InvalidRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Order;
import com.ecommerce.model.Payment;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.PaymentRepository;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Value("${stripe.api-key}")
    private String stripeApiKey;

    @jakarta.annotation.PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    public PaymentResponse processPayment(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getUser().getId().equals(userId)) {
            throw new InvalidRequestException("Unauthorized access to this order");
        }

        if ("PAID".equalsIgnoreCase(order.getPaymentStatus())) {
            throw new InvalidRequestException("Order already paid");
        }

        try {
            PaymentIntent intent = createPaymentIntent(order);

            Payment payment = Payment.builder()
                    .order(order)
                    .amount(order.getTotalPrice())
                    .paymentMethod(order.getPaymentMethod())
                    .status("PENDING")
                    .stripePaymentIntentId(intent.getId())
                    .build();

            payment = paymentRepository.save(payment);

            return PaymentResponse.builder()
                    .paymentId(payment.getId())
                    .orderId(order.getId())
                    .status(payment.getStatus())
                    .stripePaymentIntentId(intent.getId())
                    .build();
        } catch (Exception ex) {
            Payment payment = Payment.builder()
                    .order(order)
                    .amount(order.getTotalPrice())
                    .paymentMethod(order.getPaymentMethod())
                    .status("FAILED")
                    .failureReason(ex.getMessage())
                    .build();
            paymentRepository.save(payment);

            throw new InvalidRequestException("Payment processing failed: " + ex.getMessage());
        }
    }

    public PaymentResponse confirmPayment(String paymentIntentId) {
        Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        try {
            PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);

            if ("succeeded".equals(intent.getStatus())) {
                payment.setStatus("PAID");
                payment.setTransactionId(intent.getId());
                payment.setUpdatedAt(System.currentTimeMillis());
                payment = paymentRepository.save(payment);

                Order order = payment.getOrder();
                order.setPaymentStatus("PAID");
                order.setOrderStatus("CONFIRMED");
                order.setUpdatedAt(System.currentTimeMillis());
                orderRepository.save(order);

                return PaymentResponse.builder()
                        .paymentId(payment.getId())
                        .orderId(order.getId())
                        .status(payment.getStatus())
                        .transactionId(payment.getTransactionId())
                        .stripePaymentIntentId(intent.getId())
                        .build();
            } else if ("processing".equals(intent.getStatus())) {
                payment.setStatus("PENDING");
                payment.setUpdatedAt(System.currentTimeMillis());
                paymentRepository.save(payment);

                return PaymentResponse.builder()
                        .paymentId(payment.getId())
                        .orderId(payment.getOrder().getId())
                        .status(payment.getStatus())
                        .stripePaymentIntentId(intent.getId())
                        .build();
            } else {
                payment.setStatus("FAILED");
                payment.setFailureReason("Payment intent status: " + intent.getStatus());
                payment.setUpdatedAt(System.currentTimeMillis());
                paymentRepository.save(payment);

                throw new InvalidRequestException("Payment failed with status: " + intent.getStatus());
            }
        } catch (Exception ex) {
            throw new InvalidRequestException("Failed to confirm payment: " + ex.getMessage());
        }
    }

    public void refundPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        if (!"PAID".equals(payment.getStatus())) {
            throw new InvalidRequestException("Can only refund paid payments");
        }

        try {
            PaymentIntent intent = PaymentIntent.retrieve(payment.getStripePaymentIntentId());
            intent.cancel();

            payment.setStatus("REFUNDED");
            payment.setUpdatedAt(System.currentTimeMillis());
            paymentRepository.save(payment);

            Order order = payment.getOrder();
            order.setPaymentStatus("REFUNDED");
            order.setUpdatedAt(System.currentTimeMillis());
            orderRepository.save(order);
        } catch (Exception ex) {
            throw new InvalidRequestException("Refund failed: " + ex.getMessage());
        }
    }

    private PaymentIntent createPaymentIntent(Order order) throws Exception {
        long amountInCents = order.getTotalPrice().multiply(new BigDecimal(100)).longValue();

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency("usd")
                .setDescription("Order #" + order.getId())
                .putMetadata("orderId", order.getId().toString())
                .build();

        return PaymentIntent.create(params);
    }
}
