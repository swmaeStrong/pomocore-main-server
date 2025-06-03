package com.swmStrong.demo.domain.portone.dto;

public record PaymentMethod (
        String pgProvider,
        String issuer,
        String number
) {
    public static PaymentMethod of(String pgProvider, String issuer, String number) {
        return new PaymentMethod(pgProvider, issuer, number);
    }
}
