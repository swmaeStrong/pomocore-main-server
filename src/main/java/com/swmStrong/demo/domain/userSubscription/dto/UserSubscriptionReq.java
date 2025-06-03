package com.swmStrong.demo.domain.userSubscription.dto;

public record UserSubscriptionReq (
        String subscriptionPlanId,
        String billingKey
){
}
