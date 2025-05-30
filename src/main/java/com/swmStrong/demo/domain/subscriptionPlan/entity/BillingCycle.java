package com.swmStrong.demo.domain.subscriptionPlan.entity;

public enum BillingCycle {
    MONTHLY(30),
    SIX_MONTHS(180),
    YEARLY(365); // 일수 기준 예시

    private final int days;
    BillingCycle(int days) { this.days = days; }
    public int getDays() { return days; }
}
