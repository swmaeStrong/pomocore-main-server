package com.swmStrong.demo.domain.userSubscription.entity;

import com.swmStrong.demo.domain.subscriptionPlan.entity.SubscriptionPlan;
import com.swmStrong.demo.domain.user.entity.User;
import com.swmStrong.demo.domain.userPaymentMethod.entity.UserPaymentMethod;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor()
public class UserSubscription {
    @Id
    private final String id = UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private SubscriptionPlan subscriptionPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    private UserPaymentMethod userPaymentMethod;

    private String paymentId;

    private boolean autoUpdate;

    @Enumerated(EnumType.STRING)
    private UserSubscriptionStatus userSubscriptionStatus;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Builder
    public UserSubscription(User user,
                            SubscriptionPlan subscriptionPlan,
                            String paymentId,
                            UserPaymentMethod userPaymentMethod,
                            boolean autoUpdate,
                            UserSubscriptionStatus userSubscriptionStatus,
                            LocalDateTime startTime,
                            LocalDateTime endTime) {
        this.user = user;
        this.subscriptionPlan = subscriptionPlan;
        this.paymentId = paymentId;
        this.userPaymentMethod = userPaymentMethod;
        this.autoUpdate = autoUpdate;
        this.userSubscriptionStatus = userSubscriptionStatus;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public void setUserSubscriptionStatus(UserSubscriptionStatus userSubscriptionStatus) {
        this.userSubscriptionStatus = userSubscriptionStatus;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }


}
