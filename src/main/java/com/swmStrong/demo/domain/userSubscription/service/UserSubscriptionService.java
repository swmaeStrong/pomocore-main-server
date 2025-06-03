package com.swmStrong.demo.domain.userSubscription.service;


import com.swmStrong.demo.domain.subscriptionPlan.entity.SubscriptionPlanType;
import com.swmStrong.demo.domain.userSubscription.dto.UserSubscriptionRes;

import java.util.List;

public interface UserSubscriptionService {
    void createUserSubscriptionWithBillingKey(String userId, String subscriptionPlanId, String billingKey);
    void createUserSubscriptionWithPaymentMethod(String userId, String subscriptionPlanId, String userPaymentMethodId);
    void scheduleUserSubscription(String userId, String paymentId);
    void cancelCurrentSubscription(String userSubscriptionId, String reason);
    void cancelScheduledSubscription(String userSubscriptionId);
    UserSubscriptionRes getCurrentSubscription(String userId);
    List <UserSubscriptionRes> getAllSubscriptions(String userId);
}
