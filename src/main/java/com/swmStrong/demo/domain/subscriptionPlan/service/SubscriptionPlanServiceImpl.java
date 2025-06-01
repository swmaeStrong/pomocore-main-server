package com.swmStrong.demo.domain.subscriptionPlan.service;

import com.swmStrong.demo.domain.subscriptionPlan.dto.req.SubscriptionPlanReq;
import com.swmStrong.demo.domain.subscriptionPlan.entity.SubscriptionPlan;
import com.swmStrong.demo.domain.subscriptionPlan.repository.SubscriptionPlanRepository;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionPlanServiceImpl implements SubscriptionPlanService {
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    public SubscriptionPlanServiceImpl(SubscriptionPlanRepository subscriptionPlanRepository) {
        this.subscriptionPlanRepository = subscriptionPlanRepository;
    }

    public void addSubscriptionPlan(SubscriptionPlanReq req) {
        subscriptionPlanRepository.save(
                SubscriptionPlan.builder()
                        .subscriptionPlanType(req.subscriptionPlanType())
                        .billingCycle(req.billingCycle())
                        .price(req.price())
                        .isAvail(true)
                        .build());

    }
}
