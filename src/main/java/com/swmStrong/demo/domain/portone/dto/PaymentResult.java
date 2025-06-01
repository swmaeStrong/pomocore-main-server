package com.swmStrong.demo.domain.portone.dto;

public class PaymentResult {
    private final boolean success;
    private final String paymentId;
    private final String errorType;

    private PaymentResult(boolean success, String paymentId, String errorType) {
        this.success = success;
        this.paymentId = paymentId;
        this.errorType = errorType;
    }

    public static PaymentResult of(boolean success, String paymentId, String errorType) {
        return new PaymentResult(success, paymentId, errorType);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public String getErrorType() {
        return errorType;
    }
}
