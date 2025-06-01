package com.swmStrong.demo.domain.userPaymentMethod.entity;
import com.swmStrong.demo.domain.common.entity.BaseEntity;
import com.swmStrong.demo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserPaymentMethod extends BaseEntity {

    @Id
    private final String id = UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.LAZY)
    private User user; // 유저와 N:1 관계

    @Column(nullable = false, unique = true)
    private String billingKey;

    private String paymentMethod;

    @Builder
    public UserPaymentMethod(User user, String billingKey, String paymentMethod) {
        this.user = user;
        this.billingKey = billingKey;
        this.paymentMethod = paymentMethod;
    }
}
