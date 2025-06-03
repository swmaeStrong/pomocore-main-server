package com.swmStrong.demo.domain.userSubscription.dto;

public record NewUserSubscriptionReq(
        String subscriptionPlanId,
        String billingKey
){
}
