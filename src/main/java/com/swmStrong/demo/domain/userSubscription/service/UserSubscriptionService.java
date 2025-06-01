package com.swmStrong.demo.domain.userSubscription.service;


public interface UserSubscriptionService {
    void createUserSubscription(String userId, String subscriptionPlanId, String billingKey);
    void scheduleUserSubscription(String userId, String paymentId);
    void cancelCurrentSubscription(String userSubscriptionId, String reason);
    void cancelScheduledSubscription(String userSubscriptionId);
}
