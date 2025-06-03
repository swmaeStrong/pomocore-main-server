package com.swmStrong.demo.domain.subscriptionPlan.service;

import com.swmStrong.demo.domain.subscriptionPlan.dto.req.SubscriptionPlanReq;
import com.swmStrong.demo.domain.subscriptionPlan.dto.req.SubscriptionPlanRes;
import com.swmStrong.demo.domain.subscriptionPlan.entity.SubscriptionPlan;
import com.swmStrong.demo.domain.subscriptionPlan.repository.SubscriptionPlanRepository;
import com.swmStrong.demo.domain.userPaymentMethod.dto.UserPaymentMethodRes;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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
                        .availability(true)
                        .build());

    }

    public List<SubscriptionPlanRes> getSubscriptionPlans(){
        List<SubscriptionPlan> subscriptionPlans = subscriptionPlanRepository.findAllByAvailability(true);
        return subscriptionPlans.stream()
                .map(SubscriptionPlanRes::from)
                .collect(Collectors.toList());
    }

}
