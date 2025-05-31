package com.swmStrong.demo.domain.userSubscription.repository;

import com.swmStrong.demo.domain.userSubscription.entity.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, String> {
    UserSubscription findByPaymentId(String paymentId);
    boolean existsByPaymentId(String paymentId);
}
