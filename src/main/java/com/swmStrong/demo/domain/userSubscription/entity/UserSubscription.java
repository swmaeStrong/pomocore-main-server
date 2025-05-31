package com.swmStrong.demo.domain.userSubscription.entity;

import com.swmStrong.demo.domain.subscriptionPlan.entity.SubscriptionPlan;
import com.swmStrong.demo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor()
public class UserSubscription {
    @Id
    private final String id = UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private SubscriptionPlan subscriptionPlan;

    private String paymentId;

    private String scheduledId;

    @Enumerated(EnumType.STRING)
    private UserSubscriptionStatus userSubscriptionStatus;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Builder
    public UserSubscription(User user, SubscriptionPlan subscriptionPlan, String paymentId, String scheduledId, UserSubscriptionStatus userSubscriptionStatus, LocalDateTime startTime, LocalDateTime endTime) {
        this.user = user;
        this.subscriptionPlan = subscriptionPlan;
        this.paymentId = paymentId;
        this.scheduledId = scheduledId;
        this.userSubscriptionStatus = userSubscriptionStatus;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public void setScheduledId(String scheduledId) {
        this.scheduledId = scheduledId;
    }

    public void setUserSubscriptionStatus(UserSubscriptionStatus userSubscriptionStatus) {
        this.userSubscriptionStatus = userSubscriptionStatus;
    }

    public SubscriptionPlan getSubscriptionPlan() {
        return subscriptionPlan;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public String getScheduledId() {
        return scheduledId;
    }

}
