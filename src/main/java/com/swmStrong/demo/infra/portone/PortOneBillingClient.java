package com.swmStrong.demo.infra.portone;
import com.swmStrong.demo.domain.portone.dto.PaymentMethod;
import com.swmStrong.demo.domain.portone.dto.PaymentResult;
import com.swmStrong.demo.domain.portone.dto.ScheduledPaymentResult;
import com.swmStrong.demo.domain.subscriptionPlan.entity.BillingCycle;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class PortOneBillingClient {

    @Value("${portone.secret}")
    private String secret;
    private final PortOnePaymentClient portOnePaymentClient;

    public PortOneBillingClient(PortOnePaymentClient portOnePaymentClient) {
        this.portOnePaymentClient = portOnePaymentClient;
    }

    public PaymentResult requestPayment(String paymentId, String billingKey, String userId, String orderName, Integer amount) {
        return portOnePaymentClient.requestPaymentToPortOne(paymentId, billingKey, userId, orderName, amount);
    }

    public PaymentResult cancelLastPayment(String paymentId, String reason) {
        return portOnePaymentClient.cancelLastPaymentToPortOne(paymentId, reason);
    }

    public PaymentMethod getPaymentMethod(String billingKey) {
        return portOnePaymentClient.getPaymentMethodFromPortOne(billingKey);
    }

}
