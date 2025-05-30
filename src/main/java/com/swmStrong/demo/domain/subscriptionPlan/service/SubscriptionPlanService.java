package com.swmStrong.demo.domain.subscriptionPlan.service;

import com.swmStrong.demo.domain.subscriptionPlan.dto.req.SubscriptionPlanReq;
import com.swmStrong.demo.domain.subscriptionPlan.entity.SubscriptionPlan;

public interface SubscriptionPlanService {
    void addSubscriptionPlan(SubscriptionPlanReq subscriptionPlanreq);
}
