package com.swmStrong.demo.domain.userSubscription.repository;

import com.swmStrong.demo.domain.user.entity.User;
import com.swmStrong.demo.domain.userSubscription.entity.UserSubscription;
import com.swmStrong.demo.domain.userSubscription.entity.UserSubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, String> {
    UserSubscription findByPaymentId(String paymentId);
    UserSubscription findByUserId(String userId);
    List<UserSubscription> findByUserSubscriptionStatusAndEndTimeBefore(UserSubscriptionStatus userSubscriptionStatus, LocalDateTime now);
    boolean existsByUserSubscriptionStatusAndUserId(UserSubscriptionStatus userSubscriptionStatus, String userId);
    List<UserSubscription> findByUserSubscriptionStatusInAndUserId(
            List<UserSubscriptionStatus> userSubscriptionStatusList, String userId);
}
