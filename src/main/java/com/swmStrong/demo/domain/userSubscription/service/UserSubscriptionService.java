package com.swmStrong.demo.domain.userSubscription.service;


public interface UserSubscriptionService {
    void createUserSubscription(String userId, String subscriptionPlanId, String billingKey);
    void extendUserSubscription(String userId, String paymentId);
}
