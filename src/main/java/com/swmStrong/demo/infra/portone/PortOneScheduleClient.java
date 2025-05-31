package com.swmStrong.demo.infra.portone;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swmStrong.demo.domain.portone.dto.ScheduledPaymentResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PortOneScheduleClient {

    @Value("${portone.secret}")
    private String secret;

    private final RestTemplate restTemplate = new RestTemplate();

    public ScheduledPaymentResult requestScheduledPaymentToPortOne(String paymentId, String billingKey, Integer amount, String userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "PortOne " + secret);

        Map<String, Object> payment = Map.ofEntries(
                Map.entry("billingKey", billingKey),
                Map.entry("orderName", "구독 결제"),
                Map.entry("customer", Map.of("id", userId)),
                Map.entry("amount", Map.of("total", amount)),
                Map.entry("currency", "KRW")
        );
        Map<String, Object> body = new HashMap<>();
        body.put("payment", payment);
        body.put("timeToPay", OffsetDateTime.now().plusMinutes(5)
                .withNano(0)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        String url = "https://api.portone.io/payments/{paymentId}/schedule";

        try {
            ResponseEntity<Map> res = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    Map.class,
                    paymentId
            );

            // Map → JsonNode 변환
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode resBody = objectMapper.valueToTree(res.getBody());

            JsonNode paymentRes = resBody.get("schedule");
            String scheduledId = paymentRes != null && paymentRes.has("id") ? paymentRes.get("id").asText() : "";
            List<String> scheduledIds = scheduledId.isEmpty() ? List.of() : List.of(scheduledId);

            return ScheduledPaymentResult.of(true, scheduledIds, "예약 결제 성공");

        } catch (HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode errorJson = objectMapper.readTree(responseBody);

                String errorType = errorJson.has("type") ? errorJson.get("type").asText() : "UNKNOWN";
                String errorMessage = errorJson.has("message") ? errorJson.get("message").asText() : "";
                return ScheduledPaymentResult.of(false, List.of(""), errorType + (errorMessage.isEmpty() ? "" : " : " + errorMessage));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public ScheduledPaymentResult cancelScheduledPaymentToPortOne(String billingKey, List<String> scheduledIds) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "PortOne " + secret);

        Map<String, Object> body = Map.ofEntries(
                Map.entry("billingKey", billingKey),
                Map.entry("scheduleIds", scheduledIds));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        String url = "https://api.portone.io/payment-schedules";

        try {
            ResponseEntity<Map> res = restTemplate.exchange(
                    url,
                    HttpMethod.DELETE,
                    request,
                    Map.class
            );

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode resBody = objectMapper.valueToTree(res.getBody());

            // revokedScheduleIds가 배열 형태로 응답
            List<String> revokedScheduleIds = new ArrayList<>();
            JsonNode revokedNode = resBody.get("revokedScheduleIds");
            if (revokedNode != null && revokedNode.isArray()) {
                for (JsonNode idNode : revokedNode) {
                    revokedScheduleIds.add(idNode.asText());
                }
            }

            return ScheduledPaymentResult.of(true, revokedScheduleIds, "");

        } catch (HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode errorJson = objectMapper.readTree(responseBody);

                String errorType = errorJson.has("type") ? errorJson.get("type").asText() : "UNKNOWN";
                String errorMessage = errorJson.has("message") ? errorJson.get("message").asText() : "";
                return ScheduledPaymentResult.of(false, List.of(""), errorType + (errorMessage.isEmpty() ? "" : " : " + errorMessage));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
