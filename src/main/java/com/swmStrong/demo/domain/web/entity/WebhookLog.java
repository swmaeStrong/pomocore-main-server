package com.swmStrong.demo.domain.web.entity;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;
import java.time.LocalDateTime;

@Getter
@Builder
@Document(collection = "webhook_logs")
public class WebhookLog {
    @Id
    private ObjectId id;
    private String type;
    private String timestamp;
    private String body; // 원본 JSON 문자열로 저장
    private LocalDateTime receivedAt;
}
