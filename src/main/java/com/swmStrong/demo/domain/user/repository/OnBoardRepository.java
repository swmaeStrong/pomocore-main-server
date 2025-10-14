package com.swmStrong.demo.domain.user.repository;

import com.swmStrong.demo.domain.user.entity.OnBoard;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OnBoardRepository extends MongoRepository<OnBoard, ObjectId> {

    OnBoard findByUserId(String userId);
}
