package com.swmStrong.demo.domain.userSubscription.service;


public interface UserSubscriptionService {
    void createUserSubscriptionWithBillingKey(String userId, String subscriptionPlanId, String billingKey);
    void createUserSubscriptionWithPaymentMethod(String userId, String subscriptionPlanId, String userPaymentMethodId);
    void scheduleUserSubscription(String userId, String paymentId);
    void cancelCurrentSubscription(String userSubscriptionId, String reason);
    void cancelScheduledSubscription(String userSubscriptionId);
}
