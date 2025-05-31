package com.swmStrong.demo.domain.userPayment.entity;
import com.swmStrong.demo.domain.common.entity.BaseEntity;
import com.swmStrong.demo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserPayment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id; // PK

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // 유저와 N:1 관계

    @Column(nullable = false, unique = true)
    private String billingKey;

    private String paymentMethod;

    @Builder
    public UserPayment(User user, String billingKey, String paymentMethod) {
        this.user = user;
        this.billingKey = billingKey;
        this.paymentMethod = paymentMethod;
    }
}
