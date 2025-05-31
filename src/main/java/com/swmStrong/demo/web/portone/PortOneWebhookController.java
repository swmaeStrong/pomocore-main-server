package com.swmStrong.demo.web.portone;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swmStrong.demo.domain.userSubscription.repository.UserSubscriptionRepository;
import com.swmStrong.demo.domain.userSubscription.service.UserSubscriptionService;
import com.swmStrong.demo.domain.web.entity.WebhookLog;
import com.swmStrong.demo.domain.web.repository.WebhookLogRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.Map;

@Tag(name = "포트원 웹훅 컨트롤러 ( 서버 결제 예약 및 로그 남기는 용도 )")
@RestController
public class PortOneWebhookController {
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final WebhookLogRepository webhookLogRepository;
    private final UserSubscriptionService userSubscriptionService;

    public PortOneWebhookController(
            WebhookLogRepository webhookLogRepository,
            UserSubscriptionService userSubscriptionService,
            UserSubscriptionRepository userSubscriptionRepository) {
        this.webhookLogRepository = webhookLogRepository;
        this.userSubscriptionService = userSubscriptionService;
        this.userSubscriptionRepository = userSubscriptionRepository;
    }

    @PostMapping("/webhook/portone")
    public void handlePortOneWebhook(@RequestBody Map<String, Object> payload)
    {
        // 올바르게 갱신된 경우 -> 유저의 기존 구독 정보 만료로 시켜주고, 바꿔주고 다시 결제 예약
        try {
            // 우선 몽고 디비로 로그 다 넘기기
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.valueToTree(payload);
            String type = root.has("type") ? root.get("type").asText() : "";
            String timestamp = root.has("timestamp") ? root.get("timestamp").asText() : "";

            WebhookLog log = WebhookLog.builder()
                    .type(type)
                    .timestamp(timestamp)
                    .body(root.asText()) // 원본 그대로 저장
                    .receivedAt(OffsetDateTime.now())
                    .build();

            webhookLogRepository.save(log);



            JsonNode dataNode = root.get("data");
            String paymentId = dataNode.get("paymentId").asText();
            String userId = "나중에 토큰으로 까서 가져오는 걸로 교체";
            switch (type){
                case "Transaction.Paid":
                    // 구독제 요금 결제 완료일 경우에는 이후 다음 구독 예약 결제 로직 수행
                    if (userSubscriptionRepository.existsByPaymentId(paymentId)) {
                        userSubscriptionService.scheduleUserSubscription(userId, paymentId);
                    }
            }


        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

    }
}
