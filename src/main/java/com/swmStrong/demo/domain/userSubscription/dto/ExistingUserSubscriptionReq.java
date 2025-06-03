package com.swmStrong.demo.domain.userSubscription.dto;

public record ExistingUserSubscriptionReq(
        String subscriptionPlanId,
        String userPaymentMethodId
) {
}
