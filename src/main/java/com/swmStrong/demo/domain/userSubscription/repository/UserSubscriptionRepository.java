package com.swmStrong.demo.domain.userSubscription.repository;

import com.swmStrong.demo.domain.user.entity.User;
import com.swmStrong.demo.domain.userSubscription.entity.UserSubscription;
import com.swmStrong.demo.domain.userSubscription.entity.UserSubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, String> {
    UserSubscription findByPaymentId(String paymentId);

    Optional<UserSubscription> findUserSubscriptionByUserSubscriptionStatus(UserSubscriptionStatus userSubscriptionStatus);
    List<UserSubscription> findByUserSubscriptionStatusAndEndTimeBefore(UserSubscriptionStatus userSubscriptionStatus, LocalDateTime now);
    boolean existsByUserSubscriptionStatusAndUserId(UserSubscriptionStatus userSubscriptionStatus, String userId);
    List<UserSubscription> findByUserSubscriptionStatusInAndUserId(
            List<UserSubscriptionStatus> userSubscriptionStatusList, String userId);
    List<UserSubscription> findUserSubscriptionByUserId(String userId);
}
