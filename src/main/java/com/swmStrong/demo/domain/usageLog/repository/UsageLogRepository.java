package com.swmStrong.demo.domain.usageLog.repository;

import com.swmStrong.demo.domain.usageLog.dto.CategoryHourlyUsageDto;
import com.swmStrong.demo.domain.usageLog.dto.CategoryUsageDto;
import com.swmStrong.demo.domain.usageLog.entity.UsageLog;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface UsageLogRepository extends MongoRepository<UsageLog, ObjectId> {
    List<UsageLog> findByUserId(String userId);

    @Aggregation(pipeline = {
            "{ $match: { userId: ?0, timestamp: { $gte: ?1, $lt: ?2 } } }",
            "{ $unwind: '$categories' }",
            "{ $group: { _id: '$categories', duration: { $sum: '$duration' } } }",
            "{ $lookup: {from: 'category_pattern', localField: '_id', foreignField: '_id', as: 'patternDocs' } }",
            "{ $unwind: { path: '$patternDocs', preserveNullAndEmptyArrays: true } }",
            "{ $project: { category: '$patternDocs.category', duration: 1, color: '$patternDocs.color' } }"
    })
    List<CategoryUsageDto> findByUserIdAndTimestampBetween(
            String userId,
            LocalDateTime start,
            LocalDateTime end
    );

    @Aggregation( pipeline = {
            "{ $match:  { userId:  ?0, timestamp:  { $gte:  ?1, $lt:  ?2 } } }",
            "{ $unwind:  '$categories' }",
            "{ $group:  { _id:  { hour:  { $dateTrunc:  { date:  '$timestamp', unit:  'minute', binSize: ?3 } }, category:  '$categories' }, totalDuration:  { $sum:  '$duration' } } }",
            "{ $lookup:  { from:  'category_pattern', localField:  '_id.category', foreignField:  '_id', as:  'patternDocs' } }",
            "{ $unwind:  { path:  '$patternDocs', preserveNullAndEmptyArrays:  true } }",
            "{ $project:  { hour:  '$_id.hour', category:  '$patternDocs.category', color:  '$patternDocs.color', totalDuration:  1 } }",
            "{ $sort:  { hour:  1 } }"
    })
    List<CategoryHourlyUsageDto> findHourlyCategoryUsageByUserIdAndTimestampBetween(
            String userId,
            LocalDateTime start,
            LocalDateTime end,
            Integer binSize
    );
}
