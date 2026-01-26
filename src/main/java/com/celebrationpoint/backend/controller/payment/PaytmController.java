package com.celebrationpoint.backend.controller.payment;

import com.celebrationpoint.backend.config.FrontendConfig;
import com.celebrationpoint.backend.config.PaytmConfig;
import com.celebrationpoint.backend.dto.PaytmInitiateRequest;
import com.celebrationpoint.backend.dto.PaytmInitiateResponse;
import com.celebrationpoint.backend.entity.Order;
import com.celebrationpoint.backend.entity.Payment;
import com.celebrationpoint.backend.entity.User;
import com.celebrationpoint.backend.repository.OrderRepository;
import com.celebrationpoint.backend.repository.PaymentRepository;
import com.celebrationpoint.backend.repository.UserRepository;
import com.celebrationpoint.backend.service.payment.PaytmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Paytm Payment Gateway Integration Controller
 * 
 * SECURITY NOTES:
 * 1. Checksum generation/verification happens ONLY here on backend
 * 2. Merchant key is NEVER sent to frontend
 * 3. Payment status is ONLY updated by verified callback
 * 4. Transaction IDs are logged for audit trail
 */
@RestController
@RequestMapping("/api/paytm")
@CrossOrigin
public class PaytmController {

    @Autowired
    private PaytmService paytmService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaytmConfig paytmConfig;

    @Autowired
    private FrontendConfig frontendConfig;

    // =================================================
    // ðŸ'�ðŸ» INITIATE PAYTM PAYMENT
    // =================================================
    /**
     * POST /api/paytm/initiate
     * 
     * Frontend calls this to start Paytm payment flow.
     * Returns: Paytm parameters + checksum needed to redirect user to Paytm gateway
     * 
     * Flow:
     * 1. Validate order exists
     * 2. Create payment record with INITIATED status
     * 3. Generate Paytm transaction parameters
     * 4. Calculate checksum (using merchant key - BACKEND ONLY)
     * 5. Return parameters to frontend for form redirect
     */
    @PostMapping("/initiate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> initiatePaytmPayment(
            @RequestBody PaytmInitiateRequest request,
            Authentication authentication
    ) {
        try {
            // Verify Paytm is configured
            if (!paytmConfig.isConfigured()) {
                return ResponseEntity
                        .status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(Map.of(
                                "error", "Paytm gateway is not configured",
                                "message", "Please contact support"
                        ));
            }

            Long orderId = request.getOrderId();
            String userEmail = authentication.getName();

            // Find order
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            // Find user for phone
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Create payment record
            Payment payment = new Payment();
            payment.setOrder(order);
            payment.setAmount(order.getTotalAmount());
            payment.setMethod(com.celebrationpoint.backend.entity.PaymentMethod.ONLINE);
            payment.setProvider("PAYTM");
            payment.setStatus(com.celebrationpoint.backend.entity.PaymentStatus.INITIATED);
            payment.setCreatedAt(LocalDateTime.now());

            payment = paymentRepository.save(payment);

            // Generate Paytm transaction parameters
            Map<String, String> paytmParams = paytmService.generateTransactionParams(
                    orderId,
                    order.getTotalAmount().toPlainString(),
                    userEmail,
                    user.getPhoneNumber() != null ? user.getPhoneNumber() : "9999999999"
            );

            // Build response
            PaytmInitiateResponse response = new PaytmInitiateResponse(
                    payment.getId(),
                    orderId,
                    order.getTotalAmount().toPlainString()
            );
            response.setPaytmParams(paytmParams);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "error", "Failed to initiate payment",
                            "message", e.getMessage()
                    ));
        }
    }

    // =================================================
    // âœ… PAYTM CALLBACK (WEBHOOK)
    // =================================================
    /**
     * POST /api/paytm/callback
     * 
     * Paytm calls this endpoint after user completes payment.
     * This is CRITICAL for security - we verify checksum here.
     * 
     * Flow:
     * 1. Receive transaction parameters from Paytm
     * 2. Verify checksum signature (proves it's from Paytm)
     * 3. Check transaction status (SUCCESS / FAILED)
     * 4. Update payment and order status atomically
     * 5. Redirect user to frontend /orders page
     * 
     * NEVER trust frontend payment success - only backend verification counts
     */
    @PostMapping("/callback")
    public ResponseEntity<?> paytmCallback(
            @RequestParam Map<String, String> paytmResponse,
            HttpServletResponse response
    ) throws java.io.IOException {
        try {
            // Extract checksum
            String receivedChecksum = paytmResponse.get("CHECKSUMHASH");
            if (receivedChecksum == null || receivedChecksum.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Checksum missing in callback"));
            }

            // Verify checksum (validates that callback is from Paytm)
            if (!paytmService.verifyChecksum(paytmResponse, receivedChecksum)) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Checksum verification failed - Invalid transaction"));
            }

            // Extract transaction details
            String orderId = paytmResponse.get("CUST_ID");
            String txnStatus = paytmResponse.get("STATUS");
            String txnId = paytmResponse.get("TXNID");
            String bankTxnId = paytmResponse.get("BANKTXNID");

            // Log transaction ID for audit
            System.out.println("[PAYTM CALLBACK] Order: " + orderId + 
                    " | Status: " + txnStatus + 
                    " | TxnId: " + txnId + 
                    " | BankTxnId: " + bankTxnId);

            if (orderId == null || txnStatus == null) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Invalid callback parameters"));
            }

            // Find payment by order
            Long orderIdLong = Long.valueOf(orderId);
            Order order = orderRepository.findById(orderIdLong)
                    .orElseThrow(() -> new RuntimeException("Order not found in callback"));

            Payment payment = paymentRepository.findByOrder(order)
                    .orElseThrow(() -> new RuntimeException("Payment not found in callback"));

            // Handle SUCCESS
            if ("TXN_SUCCESS".equalsIgnoreCase(txnStatus)) {
                // Update payment with transaction IDs
                payment.setStatus(com.celebrationpoint.backend.entity.PaymentStatus.SUCCESS);
                payment.setTransactionId(txnId);
                payment.setProviderPaymentId(bankTxnId != null ? bankTxnId : txnId);
                paymentRepository.save(payment);

                // Update order status to PAID (not COMPLETED - COMPLETED is for order fulfillment)
                order.setStatus(com.celebrationpoint.backend.entity.OrderStatus.PAID);
                orderRepository.save(order);

                // Redirect to orders page using centralized frontend URL
                response.sendRedirect(frontendConfig.getCallbackUrl("/paytm-callback?status=SUCCESS&orderId=" + orderId));
                return ResponseEntity.ok(Map.of("status", "redirected"));
            } else {
                // Handle FAILED
                payment.setStatus(com.celebrationpoint.backend.entity.PaymentStatus.FAILED);
                payment.setTransactionId(txnId);
                paymentRepository.save(payment);

                response.sendRedirect(frontendConfig.getCallbackUrl("/paytm-callback?status=FAILED&orderId=" + orderId));
                return ResponseEntity.ok(Map.of("status", "redirected"));
            }

        } catch (Exception e) {
            System.err.println("[PAYTM CALLBACK ERROR] " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Callback processing failed",
                            "message", e.getMessage()
                    ));
        }
    }

    // =================================================
    // ðŸ"— PAYTM GATEWAY REDIRECT URL
    // =================================================
    /**
     * GET /api/paytm/gateway-url
     * Returns the Paytm Secure Gateway URL to redirect user to
     */
    @GetMapping("/gateway-url")
    public ResponseEntity<?> getGatewayUrl() {
        return ResponseEntity.ok(Map.of(
                "gatewayUrl", paytmConfig.getGatewayUrl() + "/order/process"
        ));
    }
}







