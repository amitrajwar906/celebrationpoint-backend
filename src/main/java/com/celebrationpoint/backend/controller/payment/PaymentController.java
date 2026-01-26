package com.celebrationpoint.backend.controller.payment;

import com.celebrationpoint.backend.dto.PaymentResponse;
import com.celebrationpoint.backend.entity.Payment;
import com.celebrationpoint.backend.service.payment.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin
@PreAuthorize("isAuthenticated()")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    // =================================================
    // 💳 INITIATE ONLINE PAYMENT
    // =================================================
    @PostMapping("/initiate")
    public ResponseEntity<?> initiatePayment(
            @RequestBody Map<String, String> request,
            Authentication authentication) {

        Long orderId = Long.valueOf(request.get("orderId"));
        String provider = request.get("provider");

        String userEmail = authentication.getName();

        Payment payment = paymentService.initiatePayment(orderId, provider, userEmail);

        return ResponseEntity.ok(
                Map.of(
                        "paymentId", payment.getId(),
                        "status", payment.getStatus(),
                        "amount", payment.getAmount(),
                        "provider", provider
                )
        );
    }

    // =================================================
    // ✅ ONLINE PAYMENT SUCCESS (CALLBACK / WEBHOOK)
    // =================================================
    @PostMapping("/success")
    public ResponseEntity<?> paymentSuccess(
            @RequestBody Map<String, String> request,
            Authentication authentication) {

        Long paymentId = Long.valueOf(request.get("paymentId"));
        String providerPaymentId = request.get("providerPaymentId");

        String performedBy = authentication.getName();
        String role = authentication.getAuthorities()
                .iterator()
                .next()
                .getAuthority();

        paymentService.markPaymentSuccess(
                paymentId,
                providerPaymentId,
                performedBy,
                role
        );

        return ResponseEntity.ok(
                Map.of(
                        "message", "Payment successful",
                        "paymentId", paymentId
                )
        );
    }

    // =================================================
    // ❌ PAYMENT FAILED (ONLINE ONLY)
    // =================================================
    @PostMapping("/failed")
    public ResponseEntity<?> paymentFailed(
            @RequestBody Map<String, String> request,
            Authentication authentication) {

        Long paymentId = Long.valueOf(request.get("paymentId"));
        String userEmail = authentication.getName();

        paymentService.markPaymentFailed(paymentId, userEmail);

        return ResponseEntity.ok(
                Map.of(
                        "message", "Payment failed",
                        "paymentId", paymentId
                )
        );
    }

    // =================================================
    // 🔍 GET PAYMENT BY ORDER ID
    // =================================================
    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getPaymentByOrder(@PathVariable Long orderId) {

        Payment payment = paymentService.getPaymentByOrder(orderId);

        return ResponseEntity.ok(
                new PaymentResponse(
                        payment.getId(),
                        payment.getOrder().getId(),
                        payment.getAmount(),
                        payment.getMethod(),
                        payment.getStatus(),
                        payment.getProvider(),
                        payment.getCreatedAt()
                )
        );
    }

    // =================================================
    // 🛠️ ADMIN: MARK COD PAYMENT AS PAID
    // =================================================
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/cod/{paymentId}/paid")
    public ResponseEntity<?> markCodPaid(
            @PathVariable Long paymentId,
            Authentication authentication) {

        String adminEmail = authentication.getName();
        String role = "ROLE_ADMIN";

        paymentService.markPaymentSuccess(
                paymentId,
                "COD-COLLECTED",
                adminEmail,
                role
        );

        return ResponseEntity.ok(
                Map.of(
                        "message", "COD payment marked as PAID",
                        "paymentId", paymentId
                )
        );
    }
}
