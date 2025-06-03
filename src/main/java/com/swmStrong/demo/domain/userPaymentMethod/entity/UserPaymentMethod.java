package com.swmStrong.demo.domain.userPaymentMethod.entity;
import com.swmStrong.demo.domain.common.entity.BaseEntity;
import com.swmStrong.demo.domain.user.entity.User;
import com.swmStrong.demo.domain.userPaymentMethod.converter.BillingKeyCryptoConverter;
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

    @Column(name = "billing_key")
    @Convert(converter = BillingKeyCryptoConverter.class)
    private String billingKey;

    private String pgProvider;

    private String issuer;

    private boolean isDeleted;

    @Column(name = "number")
    @Convert(converter = BillingKeyCryptoConverter.class)
    private String number;

    @Builder
    public UserPaymentMethod(User user, String billingKey, String pgProvider, String issuer, String number) {
        this.user = user;
        this.billingKey = billingKey;
        this.pgProvider = pgProvider;
        this.issuer = issuer;
        this.number = number;
        this.isDeleted = false;
    }

    public void setIsDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
}
