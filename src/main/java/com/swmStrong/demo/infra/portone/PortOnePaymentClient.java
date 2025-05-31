package com.swmStrong.demo.infra.portone;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swmStrong.demo.domain.portone.dto.PaymentResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

@Component
public class PortOnePaymentClient {
    @Value("${portone.secret}")
    private String secret;

    private final RestTemplate restTemplate = new RestTemplate();

    public PaymentResult requestPaymentToPortOne(String paymentId, String billingKey, String userId, String orderName, Integer amount) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "PortOne " + secret);

        Map<String, Object> body = Map.ofEntries(
                Map.entry("billingKey", billingKey),
                Map.entry("orderName", orderName),
                Map.entry("customer", Map.of("id", userId)),
                Map.entry("amount", Map.of("total", amount)),
                Map.entry("currency", "KRW")
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        String url = "https://api.portone.io/payments/{paymentId}/billing-key";

        try {
            restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    Map.class,
                    paymentId
            );

            // 실전에서는 응답값 파싱해서 실제 필요한 필드에 할당
            return PaymentResult.of(true, paymentId, "결제 성공");

        } catch (HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();

            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode errorNode = objectMapper.readTree(responseBody);

                // 예: type, message, code 등 원하는 필드 꺼내기
                String errorType = errorNode.has("type") ? errorNode.get("type").asText() : "UNKNOWN";
                String errorMessage = errorNode.has("message") ? errorNode.get("message").asText() : "No message";
                String errorCode = errorNode.has("code") ? errorNode.get("code").asText() : "";

                // 상세 에러 메시지로 결제 결과 리턴
                return PaymentResult.of(false, paymentId, errorType + ": " + errorMessage + " [" + errorCode + "]");
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    // 취소 쪽은 테스트를 못함 아직..
    public PaymentResult cancelLastPaymentToPortOne(String paymentId, String reason) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "PortOne " + secret);

        Map<String, Object> body = Map.ofEntries(
                Map.entry("reason", reason));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        String url = "https://api.portone.io/payments/{paymentId}/cancel";

        try {
            ResponseEntity<Map> res = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    Map.class,
                    paymentId
            );

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode resBody = objectMapper.valueToTree(res.getBody());
            System.out.println("resBody = " + resBody);

            // cancellation 객체 꺼내기 (null 체크 안전!)
            JsonNode paymentRes = resBody.get("cancellation");
            String status = paymentRes != null && paymentRes.has("status") ? paymentRes.get("status").asText() : "";

            switch (status) {
                case "FAILED":
                    return PaymentResult.of(false, paymentId, "결제 취소 실패");
                case "REQUESTED":
                    return PaymentResult.of(true, paymentId, "결제 취소 요청됨");
                case "SUCCEEDED":
                    return PaymentResult.of(true, paymentId, "결제 취소 성공");
                default:
                    return PaymentResult.of(true, paymentId, "");
            }

        } catch (HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode errorJson = objectMapper.readTree(responseBody);

                String errorType = errorJson.has("type") ? errorJson.get("type").asText() : "UNKNOWN";
                String errorMessage = errorJson.has("message") ? errorJson.get("message").asText() : "No message";
                String errorCode = errorJson.has("code") ? errorJson.get("code").asText() : "";

                return PaymentResult.of(false, "", errorType + ": " + errorMessage + " [" + errorCode + "]");
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public String getPaymentMethodFromPortOne(String billingKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "PortOne " + secret);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(headers);
        String url = "https://api.portone.io/billing-keys/{billingKey}";

        try {
            ResponseEntity<Map> res = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    Map.class,
                    billingKey
            );

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.valueToTree(res.getBody());

            // pgBillingKeyIssueResponses 배열 접근
            JsonNode responses = root.get("pgBillingKeyIssueResponses");
            if (responses != null && responses.isArray() && !responses.isEmpty()) {
                JsonNode firstResponse = responses.get(0);

                // channel 객체에서 pgProvider 추출
                JsonNode channel = firstResponse.get("channel");
                if (channel != null && channel.has("pgProvider")) {
                    return channel.get("pgProvider").asText();
                }
            }
            return "";

        } catch (HttpClientErrorException e) {
            return "";
        }
    }
}
