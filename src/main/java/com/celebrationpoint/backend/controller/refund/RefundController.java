package com.celebrationpoint.backend.controller.refund;

import com.celebrationpoint.backend.service.refund.RefundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/refunds")
@CrossOrigin
public class RefundController {

    @Autowired
    private RefundService refundService;

    // ===============================
    // 💸 INITIATE REFUND (ADMIN ONLY)
    // ===============================
    @PostMapping("/initiate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> initiateRefund(@RequestBody Map<String, String> request) {

        Long orderId = Long.parseLong(request.get("orderId"));

        var refund = refundService.initiateRefund(orderId);

        return ResponseEntity.ok(
                Map.of(
                        "message", "Refund initiated",
                        "refundId", refund.getId(),
                        "amount", refund.getAmount(),
                        "status", refund.getStatus().name()
                )
        );
    }

    // ===============================
    // ✅ REFUND SUCCESS CALLBACK
    // ===============================
    @PostMapping("/{refundId}/success")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> refundSuccess(
            @PathVariable Long refundId,
            @RequestBody Map<String, String> request
    ) {

        String providerRefundId = request.get("providerRefundId");

        refundService.markRefundSuccess(refundId, providerRefundId);

        return ResponseEntity.ok(
                Map.of("message", "Refund successful")
        );
    }

    // ===============================
    // ❌ REFUND FAILED CALLBACK
    // ===============================
    @PostMapping("/{refundId}/failed")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> refundFailed(@PathVariable Long refundId) {

        refundService.markRefundFailed(refundId);

        return ResponseEntity.ok(
                Map.of("message", "Refund failed")
        );
    }
}
