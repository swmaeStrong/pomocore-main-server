package com.swmStrong.demo.domain.subscriptionPlan.entity;

import com.swmStrong.demo.domain.common.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
public class SubscriptionPlan extends BaseEntity {
    @Id
    private final String id = UUID.randomUUID().toString();

    @Enumerated(EnumType.STRING)
    private SubscriptionPlanType subscriptionPlanType;

    @Enumerated(EnumType.STRING)
    private BillingCycle billingCycle;

    private Integer price;

    private boolean availability;

    @Builder
    public SubscriptionPlan(SubscriptionPlanType subscriptionPlanType,
                            BillingCycle billingCycle,
                            Integer price,
                            boolean availability) {
        this.subscriptionPlanType = subscriptionPlanType;
        this.billingCycle = billingCycle;
        this.price = price;
        this.availability = availability; // 처음 생성할 때에는 구독정책 항상 true
    }



}
