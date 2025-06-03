package com.swmStrong.demo.domain.web.service;
import com.fasterxml.jackson.databind.JsonNode;
import com.swmStrong.demo.domain.userSubscription.entity.UserSubscription;
import com.swmStrong.demo.domain.userSubscription.repository.UserSubscriptionRepository;
import com.swmStrong.demo.domain.userSubscription.service.UserSubscriptionService;
import com.swmStrong.demo.domain.web.entity.WebhookLog;
import com.swmStrong.demo.domain.web.repository.WebhookLogRepository;
import com.swmStrong.demo.infra.json.JsonLoader;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class WebhookServiceImpl implements WebhookService {
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final WebhookLogRepository webhookLogRepository;
    private final UserSubscriptionService userSubscriptionService;
    private final JsonLoader jsonLoader;

    public WebhookServiceImpl(
            WebhookLogRepository webhookLogRepository,
            UserSubscriptionService userSubscriptionService,
            UserSubscriptionRepository userSubscriptionRepository,
            JsonLoader jsonLoader) {
        this.webhookLogRepository = webhookLogRepository;
        this.userSubscriptionService = userSubscriptionService;
        this.userSubscriptionRepository = userSubscriptionRepository;
        this.jsonLoader = jsonLoader;
    }



    public void handlePortOneWebhook(Map<String, Object> payload) {
        // 올바르게 갱신된 경우 -> 유저의 기존 구독 정보 만료로 시켜주고, 바꿔주고 다시 결제 예약
        try {
            // 우선 몽고 디비로 로그 다 넘기기
            JsonNode root = jsonLoader.toJsonTree(payload);
            String type = root.has("type") ? root.get("type").asText() : "";
            String timestamp = root.has("timestamp") ? root.get("timestamp").asText() : "";

            WebhookLog log = WebhookLog.builder()
                    .type(type)
                    .timestamp(timestamp)
                    .body(root.toString()) // 원본 그대로 저장
                    .receivedAt(LocalDateTime.now())
                    .build();

            webhookLogRepository.save(log);



            JsonNode dataNode = root.get("data");
            String paymentId = dataNode.get("paymentId").asText();
            UserSubscription userSubscription = userSubscriptionRepository.findByPaymentId(paymentId);

            switch (type){
                case "Transaction.Paid":
                    // 구독제 요금 결제 완료일 경우에는 이후 다음 구독 예약 결제 로직 수행
                    if (userSubscription != null) {
                        ;
                    }
            }


        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

    }
}
