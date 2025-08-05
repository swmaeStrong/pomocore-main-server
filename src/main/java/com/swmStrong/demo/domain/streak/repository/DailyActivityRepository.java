package com.swmStrong.demo.domain.streak.repository;

import com.swmStrong.demo.domain.streak.entity.DailyActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyActivityRepository extends JpaRepository<DailyActivity, Long> {
    Optional<DailyActivity> findByUserIdAndActivityDate(String userId, LocalDate activityDate);

    List<DailyActivity> findByUserIdAndActivityDateBetween(String userId, LocalDate activityDateAfter, LocalDate activityDateBefore);

    @Query("SELECT SUM(d.activityCount) FROM DailyActivity d WHERE d.user.id = :userId")
    Integer sumActivityCountByUserId(String userId);
}
