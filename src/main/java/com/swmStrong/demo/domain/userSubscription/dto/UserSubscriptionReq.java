package com.swmStrong.demo.domain.userSubscription.dto;

public record UserSubscriptionReq (
        String userId,
        String subscriptionPlanId,
        String billingKey
){
}
