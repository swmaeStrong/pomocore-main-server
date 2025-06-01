package com.swmStrong.demo.web.portone;
import com.fasterxml.jackson.databind.JsonNode;
import com.swmStrong.demo.domain.userSubscription.entity.UserSubscription;
import com.swmStrong.demo.domain.userSubscription.repository.UserSubscriptionRepository;
import com.swmStrong.demo.domain.userSubscription.service.UserSubscriptionService;
import com.swmStrong.demo.domain.web.entity.WebhookLog;
import com.swmStrong.demo.domain.web.repository.WebhookLogRepository;
import com.swmStrong.demo.domain.web.service.WebhookService;
import com.swmStrong.demo.infra.json.JsonLoader;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.Map;

@Tag(name = "포트원 웹훅 컨트롤러")
@RestController
public class PortOneWebhookController {
    private final WebhookService webhookService;

    public PortOneWebhookController(
            WebhookService webhookService) {
        this.webhookService = webhookService;
    }
    @Operation(
            summary = "포트원으로부터 받는 웹훅을 처리한다.",
            description = "<p> 모든 결제에 대한 로그를 남기고</p>" +
                          "<p> 구독 결제일 경우에는 추가 연장 로직을 실행한다. </p>"
    )
    @PostMapping("/webhook/portone")
    public void handlePortOneWebhook(@RequestBody Map<String, Object> payload)
    {
        webhookService.handlePortOneWebhook(payload);
    }
}
