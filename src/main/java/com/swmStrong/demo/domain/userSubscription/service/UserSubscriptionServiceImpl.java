package com.swmStrong.demo.domain.userSubscription.service;
import com.swmStrong.demo.common.exception.ApiException;
import com.swmStrong.demo.common.exception.code.ErrorCode;
import com.swmStrong.demo.domain.portone.dto.ScheduledPaymentResult;
import com.swmStrong.demo.domain.subscriptionPlan.entity.SubscriptionPlan;
import com.swmStrong.demo.domain.subscriptionPlan.repository.SubscriptionPlanRepository;
import com.swmStrong.demo.domain.user.entity.User;
import com.swmStrong.demo.domain.user.repository.UserRepository;
import com.swmStrong.demo.domain.userPayment.entity.UserPayment;
import com.swmStrong.demo.domain.userPayment.repository.UserPaymentRepository;
import com.swmStrong.demo.domain.userSubscription.entity.UserSubscription;
import com.swmStrong.demo.domain.userSubscription.entity.UserSubscriptionStatus;
import com.swmStrong.demo.domain.userSubscription.repository.UserSubscriptionRepository;
import com.swmStrong.demo.domain.portone.dto.PaymentResult;
import com.swmStrong.demo.infra.portone.PortOneBillingClient;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Service
public class UserSubscriptionServiceImpl implements UserSubscriptionService {
    private final UserRepository userRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final PortOneBillingClient portOneBillingClient;
    private final UserPaymentRepository userPaymentRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;

    public UserSubscriptionServiceImpl(UserRepository userRepository,
                                       SubscriptionPlanRepository subscriptionPlanRepository,
                                       PortOneBillingClient portOneBillingClient,
                                       UserPaymentRepository userPaymentRepository,
                                       UserSubscriptionRepository userSubscriptionRepository) {
        this.userRepository = userRepository;
        this.subscriptionPlanRepository = subscriptionPlanRepository;
        this.portOneBillingClient = portOneBillingClient;
        this.userPaymentRepository = userPaymentRepository;
        this.userSubscriptionRepository = userSubscriptionRepository;
    }

    @Transactional
    public void createUserSubscription(String userId, String subscriptionPlanId, String billingKey){

        // validation 로직
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        SubscriptionPlan subscriptionPlan = subscriptionPlanRepository.findById(subscriptionPlanId)
                .orElseThrow(() -> new ApiException(ErrorCode.SUBSCRIPTION_PLAN_NOT_FOUND));

        // 등록되지 않았던 결제수단이었을 경우에는 billingKey로 paymentMethod 획득 및 결제 수단 등록
        if (!userPaymentRepository.existsByBillingKeyAndUserId(billingKey, userId)) {
            String paymentMethod = portOneBillingClient.getPaymentMethod(billingKey);
            userPaymentRepository.save(
                    UserPayment.builder().
                            user(user).
                            billingKey(billingKey).
                            paymentMethod(paymentMethod).
                            build());
        }

        // 결제 수단 이용해서 결제 로직 진행
        PaymentResult paymentResult =
                portOneBillingClient.requestPayment(
                        UUID.randomUUID().toString(),
                        billingKey,
                        userId,
                        subscriptionPlan.getSubscriptionPlanType().getDescription(),
                        subscriptionPlan.getPrice());

        // 유저 구독 정보 새로 등록
        if (paymentResult.isSuccess()){
            UserSubscription userSubscription = UserSubscription.
                    builder().
                    user(user).
                    subscriptionPlan(subscriptionPlan).
                    paymentId(paymentResult.getPaymentId()).
                    userSubscriptionStatus(UserSubscriptionStatus.ACTIVE).
                    startTime(LocalDateTime.now()).
                    endTime(LocalDateTime.now().plusDays(subscriptionPlan.getBillingCycle().getDays())).
                    build();
            userSubscriptionRepository.save(userSubscription);
        } else {
            throw new RuntimeException(paymentResult.getErrorType());
        }

    }

    @Transactional
    public void scheduleUserSubscription(String userId, String paymentId){
        User user = userRepository.findById(userId).
                orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        // 기존 예약 구독내역 활성화로 변경
        UserSubscription userSubscription = userSubscriptionRepository.findByPaymentId(paymentId);
        userSubscription.setScheduledId("");
        userSubscription.setUserSubscriptionStatus(UserSubscriptionStatus.ACTIVE);


        // 기존 유저 결제 정보 및 구독 정보 가져오기
        UserPayment userPayment = userPaymentRepository.findByUser(user);
        String billingKey = userPayment.getBillingKey();

        SubscriptionPlan subscriptionPlan = userSubscription.getSubscriptionPlan();
        String newPaymentId = UUID.randomUUID().toString();

        // 새로운 예약 신청 생성
        ScheduledPaymentResult scheduledPaymentResult = portOneBillingClient.requestScheduledPayment(
                newPaymentId,
                billingKey,
                user.getId(),
                subscriptionPlan.getSubscriptionPlanType().getDescription(),
                subscriptionPlan.getPrice(),
                subscriptionPlan.getBillingCycle());

        if (scheduledPaymentResult.isSuccess()) {
            UserSubscription newUserSubscription =
                    UserSubscription.builder().
                            user(user).
                            subscriptionPlan(subscriptionPlan).
                            paymentId(newPaymentId).
                            scheduledId(scheduledPaymentResult.getScheduledIds().get(0)).
                            startTime(LocalDateTime.now()).
                            endTime(LocalDateTime.now().plusDays(subscriptionPlan.getBillingCycle().getDays())).
                            userSubscriptionStatus(UserSubscriptionStatus.SCHEDULED).build();
            userSubscriptionRepository.save(newUserSubscription);
        } else {
            throw new RuntimeException(scheduledPaymentResult.getErrorType());
        }
    }

    public void cancelCurrentSubscription(String userSubscriptionId, String reason){

        UserSubscription userSubscription = userSubscriptionRepository.findById(userSubscriptionId).
                orElseThrow(() -> new RuntimeException("subscription not found"));

        PaymentResult paymentResult =
                portOneBillingClient.cancelLastPayment(userSubscription.getPaymentId(), reason);

        if (paymentResult.isSuccess()) {
            userSubscription.setUserSubscriptionStatus(UserSubscriptionStatus.CANCELLED);
            userSubscriptionRepository.save(userSubscription);
        }
    }


    public void cancelScheduledSubscription(String userSubscriptionId){
        UserSubscription userSubscription = userSubscriptionRepository.findById(userSubscriptionId).
                orElseThrow(() -> new RuntimeException("subscription not found"));

        ScheduledPaymentResult scheduledPaymentResult =
                portOneBillingClient.cancelScheduledPayment(List.of(userSubscription.getScheduledId()));

        if (scheduledPaymentResult.isSuccess()) {
            userSubscriptionRepository.delete(userSubscription);
        }
    }
}
