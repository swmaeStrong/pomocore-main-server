package com.swmStrong.demo.infra.portone;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swmStrong.demo.common.exception.ApiException;
import com.swmStrong.demo.common.exception.code.ErrorCode;
import com.swmStrong.demo.domain.portone.dto.PaymentMethod;
import com.swmStrong.demo.domain.portone.dto.PaymentResult;
import com.swmStrong.demo.infra.json.JsonLoader;
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
    private final JsonLoader jsonLoader;

    public PortOnePaymentClient(JsonLoader jsonLoader) {
        this.jsonLoader = jsonLoader;
    }

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
                JsonNode errorNode = jsonLoader.toJsonTree(responseBody);
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

            JsonNode resBody = jsonLoader.toJsonTree(res.getBody());
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
                    return PaymentResult.of(false, paymentId, "");
            }

        } catch (HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            try {
                JsonNode errorJson = jsonLoader.toJsonTree(responseBody);
                String errorType = errorJson.has("type") ? errorJson.get("type").asText() : "UNKNOWN";
                String errorMessage = errorJson.has("message") ? errorJson.get("message").asText() : "No message";
                String errorCode = errorJson.has("code") ? errorJson.get("code").asText() : "";

                return PaymentResult.of(false, "", errorType + ": " + errorMessage + " [" + errorCode + "]");
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public PaymentMethod getPaymentMethodFromPortOne(String billingKey) {
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

            JsonNode root = jsonLoader.toJsonTree(res.getBody());
            JsonNode responses = root.get("pgBillingKeyIssueResponses");


            if (responses != null && responses.isArray()) {
                for (JsonNode response : responses) {
                    JsonNode methodNode = response.get("method");
                    JsonNode channelNode = response.get("channel");

                    if (methodNode == null || channelNode == null) continue;

                    String methodType = methodNode.has("type") ? methodNode.get("type").asText() : "";
                    String pgProvider = channelNode.has("pgProvider") ? channelNode.get("pgProvider").asText() : "";
                    switch (methodType) {
                        // 이지 페이 수단을 사용하는 경우
                        case "BillingKeyPaymentMethodEasyPay":
                            return PaymentMethod.of(pgProvider, "", "");

                        // 카드 정보를 등록한 경우 카드사, 카드번호 정보까지 더 가져옴
                        case "BillingKeyPaymentMethodCard":
                            JsonNode cardNode = methodNode.get("card");
                            String issuer = "";
                            String number = "";
                            if (cardNode != null) {
                                issuer = cardNode.has("issuer") ? cardNode.get("issuer").asText() : "";
                                number = cardNode.has("number") ? cardNode.get("number").asText() : "";
                            }
                            return PaymentMethod.of(pgProvider, issuer, number);
                        default:
                            // 필요시 알 수 없는 타입 처리
                            break;
                    }


                }
            }
            throw new ApiException(ErrorCode.BILLING_KEY_NOT_FOUND);

        } catch (HttpClientErrorException e) {
            throw new ApiException(ErrorCode.BILLING_KEY_NOT_FOUND);
        }
    }
}
