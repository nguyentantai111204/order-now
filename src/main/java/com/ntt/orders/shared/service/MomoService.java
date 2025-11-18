package com.ntt.orders.shared.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ntt.orders.config.MomoConfig;
import com.ntt.orders.order.entity.Order;
import com.ntt.orders.order.repository.OrderRepository;
import com.ntt.orders.payment.dto.request.MomoRequest;
import com.ntt.orders.payment.dto.response.MomoResponse;
import com.ntt.orders.payment.dto.response.PaymentResponse;
import com.ntt.orders.payment.entity.Payment;
import com.ntt.orders.payment.repository.PaymentRepository;
import com.ntt.orders.shared.common.enums.OrderStatus;
import com.ntt.orders.shared.common.enums.PaymentStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MomoService {
    private final MomoConfig momoConfig;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;

    private final CloseableHttpClient httpClient = HttpClients.createDefault();

    public String createSignature(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    momoConfig.getSecretKey().getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            log.error("Error creating signature", e);
            throw new RuntimeException("Error creating signature", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    @Transactional
    public MomoResponse createPayment(MomoRequest requestDTO) {
        try {
            Order order = orderRepository.findById(requestDTO.getOrderId())
                    .orElseThrow(() -> new RuntimeException("Order not found: " + requestDTO.getOrderId()));

            // Validate amount
            if (requestDTO.getAmount().compareTo(order.getTotalAmount()) != 0) {
                throw new RuntimeException("Payment amount doesn't match order total");
            }

            String requestId = UUID.randomUUID().toString();
            String orderId = "ORDER_" + order.getId() + "_" + System.currentTimeMillis();

            // Create or update payment record
            Payment payment = paymentRepository.findByOrderId(order.getId())
                    .orElse(Payment.builder()
                            .order(order)
                            .paymentStatus(PaymentStatus.PENDING)
                            .amount(requestDTO.getAmount())
                            .paymentMethod("MOMO")
                            .build());

            payment.setTransactionId(requestId);
            paymentRepository.save(payment);

            // Prepare MoMo request
            Map<String, Object> momoRequest = new HashMap<>();
            momoRequest.put("partnerCode", momoConfig.getPartnerCode());
            momoRequest.put("partnerName", "Restaurant Order System");
            momoRequest.put("storeId", momoConfig.getPartnerCode());
            momoRequest.put("requestId", requestId);
            momoRequest.put("amount", requestDTO.getAmount().longValue());
            momoRequest.put("orderId", orderId);
            momoRequest.put("orderInfo", requestDTO.getOrderId());
            momoRequest.put("redirectUrl", momoConfig.getReturnUrl());
            momoRequest.put("ipnUrl", momoConfig.getNotifyUrl());
            momoRequest.put("lang", "vi");
            momoRequest.put("requestType", "captureWallet");
            momoRequest.put("extraData", "");

            // Create signature
            String rawHash = "accessKey=" + momoConfig.getAccessKey() +
                    "&amount=" + requestDTO.getAmount().longValue() +
                    "&extraData=" +
                    "&ipnUrl=" + momoConfig.getNotifyUrl() +
                    "&orderId=" + orderId +
                    "&orderInfo=" + requestDTO.getOrderId() +
                    "&partnerCode=" + momoConfig.getPartnerCode() +
                    "&redirectUrl=" + momoConfig.getReturnUrl() +
                    "&requestId=" + requestId +
                    "&requestType=captureWallet";

            String signature = createSignature(rawHash);
            momoRequest.put("signature", signature);

            // Send request to MoMo
            String jsonRequest = objectMapper.writeValueAsString(momoRequest);
            String response = sendPostRequest(momoConfig.getEndpoint(), jsonRequest);

            MomoResponse responseDTO = objectMapper.readValue(response, MomoResponse.class);

            if (responseDTO.getResultCode() == 0) {
                log.info("MoMo payment created successfully for order: {}", order.getId());
            } else {
                log.error("MoMo payment creation failed for order: {}, error: {}",
                        order.getId(), responseDTO.getMessage());
            }

            return responseDTO;

        } catch (Exception e) {
            log.error("Error creating MoMo payment for order: {}", requestDTO.getOrderId(), e);
            throw new RuntimeException("Failed to create payment: " + e.getMessage());
        }
    }

    private String sendPostRequest(String url, String jsonBody) throws Exception {
        HttpPost post = new HttpPost(url);
        post.setHeader("Content-Type", "application/json");
        post.setEntity(new StringEntity(jsonBody, StandardCharsets.UTF_8));

        try (CloseableHttpResponse response = httpClient.execute(post)) {
            return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        }
    }

    @Transactional
    public PaymentResponse handlePaymentReturn(Map<String, String> params) {
        try {
            String orderId = params.get("orderId");
            String resultCode = params.get("resultCode");
            String transactionId = params.get("transId");
            String message = params.get("message");

            // Extract original order ID from MoMo orderId
            String originalOrderId = extractOriginalOrderId(orderId);

            Payment payment = paymentRepository.findByOrderId(originalOrderId)
                    .orElseThrow(() -> new RuntimeException("Payment not found for order: " + originalOrderId));

            Order order = payment.getOrder();

            if ("0".equals(resultCode)) {
                // Payment successful
                payment.setPaymentStatus(PaymentStatus.SUCCESS);
                payment.setPaymentTime(LocalDateTime.now());
                payment.setTransactionId(transactionId);

                // Update order status
                order.setOrderStatus(OrderStatus.COMPLETED);
                order.setCompletedTime(LocalDateTime.now());

                log.info("Payment successful for order: {}, transaction: {}", originalOrderId, transactionId);

                return PaymentResponse.builder()
                        .orderId(originalOrderId)
                        .transactionId(transactionId)
                        .status(PaymentStatus.SUCCESS)
                        .message("Payment successful")
                        .build();
            } else {
                // Payment failed
                payment.setPaymentStatus(PaymentStatus.FAILED);

                log.warn("Payment failed for order: {}, error: {}", originalOrderId, message);

                return PaymentResponse.builder()
                        .orderId(originalOrderId)
                        .transactionId(transactionId)
                        .status(PaymentStatus.FAILED)
                        .message(message)
                        .build();
            }

        } catch (Exception e) {
            log.error("Error handling payment return", e);
            throw new RuntimeException("Error processing payment return: " + e.getMessage());
        }
    }

    @Transactional
    public void handleIPNNotification(MomoResponse responseDTO) {
        try {
            // Verify signature
            if (!verifySignature(responseDTO)) {
                log.error("Invalid signature in IPN notification");
                return;
            }

            String originalOrderId = extractOriginalOrderId(responseDTO.getOrderId());
            Payment payment = paymentRepository.findByOrderId(originalOrderId)
                    .orElseThrow(() -> new RuntimeException("Payment not found for order: " + originalOrderId));

            Order order = payment.getOrder();

            if (responseDTO.getResultCode() == 0) {
                // Payment successful
                payment.setPaymentStatus(PaymentStatus.SUCCESS);
                payment.setPaymentTime(LocalDateTime.now());
                payment.setTransactionId(responseDTO.getRequestId());

                order.setOrderStatus(OrderStatus.COMPLETED);
                order.setCompletedTime(LocalDateTime.now());

                log.info("IPN: Payment successful for order: {}", originalOrderId);
            } else {
                // Payment failed
                payment.setPaymentStatus(PaymentStatus.FAILED);
                log.warn("IPN: Payment failed for order: {}, error: {}",
                        originalOrderId, responseDTO.getMessage());
            }

            paymentRepository.save(payment);
            orderRepository.save(order);

        } catch (Exception e) {
            log.error("Error processing IPN notification", e);
        }
    }

    private boolean verifySignature(MomoResponse response) {
        try {
            String rawHash = "accessKey=" + momoConfig.getAccessKey() +
                    "&amount=" + response.getAmount() +
                    "&extraData=" +
                    "&message=" + response.getMessage() +
                    "&orderId=" + response.getOrderId() +
                    "&orderInfo=" +
                    "&partnerCode=" + response.getPartnerCode() +
                    "&payUrl=" + (response.getPayUrl() != null ? response.getPayUrl() : "") +
                    "&requestId=" + response.getRequestId() +
                    "&responseTime=" + response.getResponseTime() +
                    "&resultCode=" + response.getResultCode();

            String signature = createSignature(rawHash);
            return signature.equals(response.getSignature());
        } catch (Exception e) {
            log.error("Error verifying signature", e);
            return false;
        }
    }

    private String extractOriginalOrderId(String momoOrderId) {
        // MoMo orderId format: ORDER_{originalOrderId}_{timestamp}
        String[] parts = momoOrderId.split("_");
        if (parts.length >= 2) {
            return parts[1];
        }
        throw new RuntimeException("Invalid orderId format: " + momoOrderId);
    }

    @Transactional
    public PaymentStatus getPaymentStatus(String orderId) {
        return paymentRepository.findByOrderId(orderId)
                .map(Payment::getPaymentStatus)
                .orElse(PaymentStatus.PENDING);
    }
}
