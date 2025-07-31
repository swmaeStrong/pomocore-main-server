package com.swmStrong.demo.domain.pomodoro.repository;

import com.swmStrong.demo.domain.pomodoro.entity.CategorizedData;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CategorizedDataRepository extends MongoRepository<CategorizedData, ObjectId> {
}
