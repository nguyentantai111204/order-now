package com.ntt.orders.payment.controller;


import com.ntt.orders.payment.dto.request.MomoRequest;
import com.ntt.orders.payment.dto.response.MomoResponse;
import com.ntt.orders.payment.dto.response.PaymentResponse;
import com.ntt.orders.shared.common.enums.PaymentStatus;
import com.ntt.orders.shared.service.MomoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final MomoService momoPaymentService;

    @PostMapping("/momo/create")
    public ResponseEntity<?> createMomoPayment(@RequestBody MomoRequest requestDTO) {
        try {
            MomoResponse response = momoPaymentService.createPayment(requestDTO);

            if (response.getResultCode() == 0) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Error creating MoMo payment", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/momo/return")
    public ResponseEntity<?> handleMoMoReturn(@RequestParam Map<String, String> params) {
        try {
            PaymentResponse result = momoPaymentService.handlePaymentReturn(params);

            // Redirect to frontend with result
            String redirectUrl = String.format("/payment/result?status=%s&orderId=%s&message=%s",
                    result.getStatus().toString(),
                    result.getOrderId(),
                    result.getMessage());

            return ResponseEntity.status(302)
                    .header("Location", redirectUrl)
                    .build();

        } catch (Exception e) {
            log.error("Error handling MoMo return", e);
            return ResponseEntity.status(302)
                    .header("Location", "/payment/result?status=ERROR&message=Payment processing failed")
                    .build();
        }
    }

    @PostMapping("/momo/ipn")
    public ResponseEntity<?> handleMoMoIPN(@RequestBody MomoResponse responseDTO) {
        try {
            momoPaymentService.handleIPNNotification(responseDTO);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error handling MoMo IPN", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/status/{orderId}")
    public ResponseEntity<?> getPaymentStatus(@PathVariable String orderId) {
        try {
            PaymentStatus status = momoPaymentService.getPaymentStatus(orderId);
            return ResponseEntity.ok(Map.of("status", status));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}