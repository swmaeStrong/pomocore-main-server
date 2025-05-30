package com.swmStrong.demo.domain.subscriptionPlan.service;

import com.swmStrong.demo.domain.subscriptionPlan.dto.req.SubscriptionPlanReq;
import com.swmStrong.demo.domain.subscriptionPlan.entity.SubscriptionPlan;
import com.swmStrong.demo.domain.subscriptionPlan.repository.SubscriptionPlanRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SubscriptionPlanServiceImpl implements SubscriptionPlanService {
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    public SubscriptionPlanServiceImpl(SubscriptionPlanRepository subscriptionPlanRepository) {
        this.subscriptionPlanRepository = subscriptionPlanRepository;
    }

    public void addSubscriptionPlan(SubscriptionPlanReq req) {
        subscriptionPlanRepository.save(
                SubscriptionPlan.builder().
                        subscriptionPlanId(UUID.randomUUID().toString())
                        .subscriptionPlanType(req.subscriptionPlanType())
                        .billingCycle(req.billingCycle())
                        .price(req.price())
                        .isAvail(true)
                        .build());

    }
}
