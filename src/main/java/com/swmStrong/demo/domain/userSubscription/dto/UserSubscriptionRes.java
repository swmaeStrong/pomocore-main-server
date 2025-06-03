package com.swmStrong.demo.domain.userSubscription.dto;

import com.swmStrong.demo.domain.subscriptionPlan.entity.SubscriptionPlan;
import com.swmStrong.demo.domain.subscriptionPlan.entity.SubscriptionPlanType;
import com.swmStrong.demo.domain.userSubscription.entity.UserSubscription;
import com.swmStrong.demo.domain.userSubscription.entity.UserSubscriptionStatus;

import java.time.LocalDateTime;

public record UserSubscriptionRes(
        String userId,
        String userSubscriptionId,
        String subscriptionPlanId,
        UserSubscriptionStatus userSubscriptionStatus,
        SubscriptionPlanType subscriptionPlanType,
        Integer price,
        LocalDateTime startTime,
        LocalDateTime endTime
) {


    public static UserSubscriptionRes from (UserSubscription userSubscription, SubscriptionPlan subscriptionPlan) {
        return new UserSubscriptionRes(
                userSubscription.getUser().getId(),
                userSubscription.getId(),
                subscriptionPlan.getId(),
                userSubscription.getUserSubscriptionStatus(),
                subscriptionPlan.getSubscriptionPlanType(),
                subscriptionPlan.getPrice(),
                userSubscription.getStartTime(),
                userSubscription.getEndTime()
        );
    }
}


