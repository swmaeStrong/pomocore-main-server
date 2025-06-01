package com.swmStrong.demo.infra.portone;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swmStrong.demo.domain.portone.dto.ScheduledPaymentResult;
import com.swmStrong.demo.domain.subscriptionPlan.entity.BillingCycle;
import com.swmStrong.demo.infra.json.JsonLoader;
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
    private final JsonLoader jsonLoader;
    private final RestTemplate restTemplate = new RestTemplate();

    public PortOneScheduleClient(JsonLoader jsonLoader) {
        this.jsonLoader = jsonLoader;
    }

    public ScheduledPaymentResult requestScheduledPaymentToPortOne(String paymentId, String billingKey, String userId, String orderName, Integer amount, BillingCycle billingCycle) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "PortOne " + secret);

        Map<String, Object> payment = Map.ofEntries(
                Map.entry("billingKey", billingKey),
                Map.entry("orderName", orderName),
                Map.entry("customer", Map.of("id", userId)),
                Map.entry("amount", Map.of("total", amount)),
                Map.entry("currency", "KRW")
        );
        Map<String, Object> body = new HashMap<>();
        body.put("payment", payment);
        body.put("timeToPay", OffsetDateTime.now().plusDays(billingCycle.getDays())
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
            JsonNode resBody = jsonLoader.toJsonTree(res.getBody());

            JsonNode paymentRes = resBody.get("schedule");
            String scheduledId = paymentRes != null && paymentRes.has("id") ? paymentRes.get("id").asText() : "";
            List<String> scheduledIds = scheduledId.isEmpty() ? List.of() : List.of(scheduledId);

            return ScheduledPaymentResult.of(true, scheduledIds, "예약 결제 성공");

        } catch (HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            try {
                JsonNode errorJson = jsonLoader.toJsonTree(responseBody);

                String errorType = errorJson.has("type") ? errorJson.get("type").asText() : "UNKNOWN";
                String errorMessage = errorJson.has("message") ? errorJson.get("message").asText() : "";
                return ScheduledPaymentResult.of(false, List.of(""), errorType + (errorMessage.isEmpty() ? "" : " : " + errorMessage));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    // 취소 쪽은 테스트를 못함 아직..
    public ScheduledPaymentResult cancelScheduledPaymentToPortOne(List<String> scheduledIds) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "PortOne " + secret);

        Map<String, Object> body = Map.ofEntries(
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

            JsonNode resBody = jsonLoader.toJsonTree(res.getBody());

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
                JsonNode errorJson = jsonLoader.toJsonTree(responseBody);
                String errorType = errorJson.has("type") ? errorJson.get("type").asText() : "UNKNOWN";
                String errorMessage = errorJson.has("message") ? errorJson.get("message").asText() : "";
                return ScheduledPaymentResult.of(false, List.of(""), errorType + (errorMessage.isEmpty() ? "" : " : " + errorMessage));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
