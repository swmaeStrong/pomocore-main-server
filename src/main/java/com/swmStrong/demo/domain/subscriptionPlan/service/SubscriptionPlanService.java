package com.swmStrong.demo.domain.subscriptionPlan.service;

import com.swmStrong.demo.domain.subscriptionPlan.dto.req.SubscriptionPlanReq;
import com.swmStrong.demo.domain.subscriptionPlan.dto.req.SubscriptionPlanRes;
import com.swmStrong.demo.domain.subscriptionPlan.entity.SubscriptionPlan;

import java.util.List;

public interface SubscriptionPlanService {
    void addSubscriptionPlan(SubscriptionPlanReq subscriptionPlanreq);
    List<SubscriptionPlanRes> getSubscriptionPlans();
}
