package com.swmStrong.demo.domain.subscriptionPlan.entity;

public enum SubscriptionPlanType {
    BASIC("기본 플랜"),
    PREMIUM("프리미엄 플랜");


    private final String description;

    SubscriptionPlanType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
