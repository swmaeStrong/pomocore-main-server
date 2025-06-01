package com.swmStrong.demo.domain.subscriptionPlan.dto.req;

import com.swmStrong.demo.domain.subscriptionPlan.entity.BillingCycle;
import com.swmStrong.demo.domain.subscriptionPlan.entity.SubscriptionPlanType;
import jakarta.validation.constraints.Min;

public record SubscriptionPlanReq (
    SubscriptionPlanType subscriptionPlanType,
     BillingCycle billingCycle,
    @Min(0) Integer price // 0 이상만 허용
){ }
