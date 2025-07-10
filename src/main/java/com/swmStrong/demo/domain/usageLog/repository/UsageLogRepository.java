package com.swmStrong.demo.domain.usageLog.repository;

import com.swmStrong.demo.domain.usageLog.dto.CategoryHourlyUsageDto;
import com.swmStrong.demo.domain.usageLog.dto.CategoryUsageDto;
import com.swmStrong.demo.domain.usageLog.entity.UsageLog;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UsageLogRepository extends MongoRepository<UsageLog, ObjectId> {
    List<UsageLog> findByUserId(String userId);

    @Aggregation(
            pipeline = {
            "{ $match: { userId: ?0, timestamp: { $gte: ?1, $lt: ?2 } } }",
            "{ $group: { _id: '$categoryId', duration: { $sum: '$duration' } } }",
            "{ $lookup: { from: 'category_pattern', localField: '_id', foreignField: '_id', as: 'patternDocs' } }",
            "{ $unwind: { path: '$patternDocs', preserveNullAndEmptyArrays: true } }",
            "{ $project: { category: '$patternDocs.category', duration: 1 } }"
            }
    )
    List<CategoryUsageDto> findByUserIdAndTimestampBetween(
            String userId,
            double start,
            double end
    );

    @Aggregation(
            pipeline = {
            "{ $match: { userId: ?0, timestamp: { $gte: ?1, $lt: ?2 } } }",
            "{ $addFields: { timestampDate: { $toDate: { $multiply: [ '$timestamp', 1000 ] } } } }",
            "{ $group: { _id: { hour: { $dateTrunc: { date: '$timestampDate', unit: 'minute', binSize: ?3 } }, category: '$categoryId' }, totalDuration: { $sum: '$duration' } } }",
            "{ $lookup: { from: 'category_pattern', localField: '_id.category', foreignField: '_id', as: 'patternDocs' } }",
            "{ $unwind: { path: '$patternDocs', preserveNullAndEmptyArrays: true } }",
            "{ $project: { hour: '$_id.hour', category: '$patternDocs.category', totalDuration: 1 } }",
            "{ $sort: { hour: 1 } }"
            }
    )
    List<CategoryHourlyUsageDto> findHourlyCategoryUsageByUserIdAndTimestampBetween(
            String userId,
            double start,
            double end,
            Integer binSize
    );

    List<UsageLog> findUsageLogByUserIdAndTimestampBetween(String userId, double start, double end);
    
    Optional<UsageLog> findTopByUserIdOrderByTimestampDesc(String userId);
}
