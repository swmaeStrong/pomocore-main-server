package com.swmStrong.demo.infra.portone;
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
    private final PortOneScheduleClient portOneScheduleClient;
    private final PortOnePaymentClient portOnePaymentClient;

    public PortOneBillingClient(PortOneScheduleClient portOneScheduleClient, PortOnePaymentClient portOnePaymentClient) {
        this.portOneScheduleClient = portOneScheduleClient;
        this.portOnePaymentClient = portOnePaymentClient;
    }

    public PaymentResult requestPayment(String paymentId, String billingKey, String userId, String orderName, Integer amount) {
        return portOnePaymentClient.requestPaymentToPortOne(paymentId, billingKey, userId, orderName, amount);
    }

    public ScheduledPaymentResult requestScheduledPayment(String paymentId, String billingKey, String userId, String orderName, Integer amount, BillingCycle billingCycle) {
        return portOneScheduleClient.requestScheduledPaymentToPortOne(paymentId, billingKey, userId, orderName, amount, billingCycle);
    }

    public PaymentResult cancelLastPayment(String paymentId, String reason) {
        return portOnePaymentClient.cancelLastPaymentToPortOne(paymentId, reason);
    }

    public ScheduledPaymentResult cancelScheduledPayment(List<String> scheduledIds) {
        return portOneScheduleClient.cancelScheduledPaymentToPortOne(scheduledIds);
    }

    public String getPaymentMethod(String billingKey) {
        return portOnePaymentClient.getPaymentMethodFromPortOne(billingKey);
    }

}
