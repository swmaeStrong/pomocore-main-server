package com.swmStrong.demo.domain.subscriptionPlan.dto.req;

import com.swmStrong.demo.domain.subscriptionPlan.entity.BillingCycle;
import com.swmStrong.demo.domain.subscriptionPlan.entity.SubscriptionPlan;

public record SubscriptionPlanRes(
        String subscriptionPlanId,
        String description,
        BillingCycle billingCycle,
        int price
) {

    public static SubscriptionPlanRes from (SubscriptionPlan subscriptionPlan) {
        return new SubscriptionPlanRes(
                subscriptionPlan.getId(),
                subscriptionPlan.getSubscriptionPlanType().getDescription(),
                subscriptionPlan.getBillingCycle(),
                subscriptionPlan.getPrice()
        );
    }
}
