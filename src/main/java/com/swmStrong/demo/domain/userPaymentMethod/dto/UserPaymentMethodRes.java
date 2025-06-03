package com.swmStrong.demo.domain.userPaymentMethod.dto;

import com.swmStrong.demo.domain.userPaymentMethod.entity.UserPaymentMethod;


public record UserPaymentMethodRes(
        String id,
        String pgProvider,
        String issuer,
        String number
) {

    public static UserPaymentMethodRes from (UserPaymentMethod userPaymentMethod) {
        return new UserPaymentMethodRes(
                userPaymentMethod.getId(),
                userPaymentMethod.getPgProvider(),
                userPaymentMethod.getIssuer(),
                userPaymentMethod.getNumber()
        );
    }
}
