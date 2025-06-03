package com.swmStrong.demo.domain.userSubscription.service;


import com.swmStrong.demo.domain.subscriptionPlan.entity.SubscriptionPlanType;
import com.swmStrong.demo.domain.userSubscription.dto.UserSubscriptionRes;
import com.swmStrong.demo.domain.userSubscription.entity.UserSubscription;

import java.util.List;

public interface UserSubscriptionService {
    void createUserSubscriptionWithBillingKey(String userId, String subscriptionPlanId, String billingKey);
    void createUserSubscriptionWithPaymentMethod(String userId, String subscriptionPlanId, String userPaymentMethodId);
    void cancelCurrentSubscription(String userSubscriptionId, String reason);
    UserSubscriptionRes getCurrentSubscription(String userId);
    List <UserSubscriptionRes> getAllSubscriptions(String userId);
    void extendUserSubscriptions(List<UserSubscription> userSubscriptions);
}
