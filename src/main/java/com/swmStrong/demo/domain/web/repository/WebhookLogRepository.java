package com.swmStrong.demo.domain.web.repository;

import com.swmStrong.demo.domain.web.entity.WebhookLog;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WebhookLogRepository extends MongoRepository<WebhookLog, ObjectId> {
}
