package com.swmStrong.demo.domain.pomodoro.repository;

import com.swmStrong.demo.domain.pomodoro.dto.CategoryUsageDto;
import com.swmStrong.demo.domain.pomodoro.entity.PomodoroUsageLog;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface PomodoroUsageLogRepository extends MongoRepository<PomodoroUsageLog, ObjectId> {
    List<PomodoroUsageLog> findByUserIdAndSessionDate(String userId, LocalDate sessionDate);
    
    List<PomodoroUsageLog> findByUserIdAndSessionAndSessionDateOrderByTimestamp(String userId, int session, LocalDate sessionDate);

    @Aggregation(
            pipeline = {
                    "{ $match: { userId: ?0, timestamp: { $gte: ?1, $lt: ?2 } } }",
                    "{ $group: { _id: '$categoryId', duration: { $sum: '$duration' } } }",
                    "{ $lookup: { from: 'category_pattern', localField: '_id', foreignField: '_id', as: 'patternDocs' } }",
                    "{ $unwind: { path: '$patternDocs', preserveNullAndEmptyArrays: true } }",
                    "{ $project: { category: '$patternDocs.category', duration: 1 } }",
                    "{ $match:  { category:  { $ne : 'AFK' } } }",
            }
    )
    List<CategoryUsageDto> findByUserIdAndTimestampBetween(
            String userId,
            double start,
            double end
    );

    List<PomodoroUsageLog> findByUserIdAndSessionDateAndSession(String userId, LocalDate sessionDate, int session);

    List<PomodoroUsageLog> findByUserIdAndSessionDateBetween(String userId, LocalDate sessionDateAfter, LocalDate sessionDateBefore);
}
