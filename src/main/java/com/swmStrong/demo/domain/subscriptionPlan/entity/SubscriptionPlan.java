package com.swmStrong.demo.domain.subscriptionPlan.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Getter
@NoArgsConstructor
public class SubscriptionPlan {
    @Id
    private String subscriptionPlanId;

    @Enumerated(EnumType.STRING)
    private SubscriptionPlanType subscriptionPlanType;

    @Enumerated(EnumType.STRING)
    private BillingCycle billingCycle;

    private Integer price;

    private boolean isAvail;

    @Builder
    public SubscriptionPlan(String subscriptionPlanId,
                            SubscriptionPlanType subscriptionPlanType,
                            BillingCycle billingCycle,
                            Integer price,
                            boolean isAvail) {
        this.subscriptionPlanId = subscriptionPlanId;
        this.subscriptionPlanType = subscriptionPlanType;
        this.billingCycle = billingCycle;
        this.price = price;
        this.isAvail = true; // 처음 생성할 때에는 구독정책 항상 true
    }



}
