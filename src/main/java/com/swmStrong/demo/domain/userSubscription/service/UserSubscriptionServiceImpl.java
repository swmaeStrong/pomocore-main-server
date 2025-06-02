package com.swmStrong.demo.domain.userSubscription.service;
import com.swmStrong.demo.common.exception.ApiException;
import com.swmStrong.demo.common.exception.code.ErrorCode;
import com.swmStrong.demo.domain.portone.dto.ScheduledPaymentResult;
import com.swmStrong.demo.domain.subscriptionPlan.entity.SubscriptionPlan;
import com.swmStrong.demo.domain.subscriptionPlan.repository.SubscriptionPlanRepository;
import com.swmStrong.demo.domain.user.entity.User;
import com.swmStrong.demo.domain.user.repository.UserRepository;
import com.swmStrong.demo.domain.userPaymentMethod.entity.UserPaymentMethod;
import com.swmStrong.demo.domain.userPaymentMethod.repository.UserPaymentMethodRepository;
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
    private final UserPaymentMethodRepository userPaymentMethodRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;

    public UserSubscriptionServiceImpl(UserRepository userRepository,
                                       SubscriptionPlanRepository subscriptionPlanRepository,
                                       PortOneBillingClient portOneBillingClient,
                                       UserPaymentMethodRepository userPaymentMethodRepository,
                                       UserSubscriptionRepository userSubscriptionRepository) {
        this.userRepository = userRepository;
        this.subscriptionPlanRepository = subscriptionPlanRepository;
        this.portOneBillingClient = portOneBillingClient;
        this.userPaymentMethodRepository = userPaymentMethodRepository;
        this.userSubscriptionRepository = userSubscriptionRepository;
    }

    @Transactional
    public void createUserSubscription(String userId, String subscriptionPlanId, String billingKey){

        // 1. 유저, 플랜 조회 (예외는 그대로)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        SubscriptionPlan plan = subscriptionPlanRepository.findById(subscriptionPlanId)
                .orElseThrow(() -> new ApiException(ErrorCode.SUBSCRIPTION_PLAN_NOT_FOUND));

        // 2. 중복 구독 검사
        if (userSubscriptionRepository.existsByUserSubscriptionStatusAndUserId(
                UserSubscriptionStatus.ACTIVE, userId)) {
            throw new RuntimeException("User already has an active subscription");
        }

        // 3. 결제수단 등록 (없는 경우만)
        userPaymentMethodRepository.findByBillingKeyAndUserId(billingKey, userId)
                .orElseGet(() -> {
                    String paymentMethod = portOneBillingClient.getPaymentMethod(billingKey);
                    return userPaymentMethodRepository.save(
                            UserPaymentMethod.builder()
                                    .user(user)
                                    .billingKey(billingKey)
                                    .paymentMethod(paymentMethod)
                                    .build());
                });

        // 4. 결제 시도
        PaymentResult result = portOneBillingClient.requestPayment(
                UUID.randomUUID().toString(),
                billingKey,
                userId,
                plan.getSubscriptionPlanType().getDescription(),
                plan.getPrice()
        );

        // 5. 결제 성공시 구독 등록
        if (result.isSuccess()) {
            UserSubscription subscription = UserSubscription.builder()
                    .user(user)
                    .subscriptionPlan(plan)
                    .paymentId(result.getPaymentId())
                    .userSubscriptionStatus(UserSubscriptionStatus.ACTIVE)
                    .startTime(LocalDateTime.now())
                    .endTime(LocalDateTime.now().plusDays(plan.getBillingCycle().getDays()))
                    .build();
            userSubscriptionRepository.save(subscription);
        } else {
            throw new RuntimeException(result.getErrorType());
        }

    }

    @Transactional
    public void scheduleUserSubscription(String userId, String paymentId){
        User user = userRepository.findById(userId).
                orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        // ACTIVE, SCHEDULED, PENDING 한 상태들 다 가져옴
        List<UserSubscriptionStatus> searchStatuses = List.of(
                UserSubscriptionStatus.ACTIVE,
                UserSubscriptionStatus.SCHEDULED,
                UserSubscriptionStatus.PENDING
        );

        List<UserSubscription> userSubscriptions =
                userSubscriptionRepository.findByUserSubscriptionStatusInAndUserId(searchStatuses, user.getId());

        // (ACTIVE => EXPIRED), (SCHEDULED || PENDING => ACTIVE)
        for (UserSubscription sub : userSubscriptions) {
            UserSubscriptionStatus current = sub.getUserSubscriptionStatus();
            UserSubscriptionStatus newStatus =
                    (current == UserSubscriptionStatus.SCHEDULED || current == UserSubscriptionStatus.PENDING)
                            ? UserSubscriptionStatus.ACTIVE
                            : UserSubscriptionStatus.EXPIRED;
            sub.setUserSubscriptionStatus(newStatus);
        }
        // 모두 저장
        userSubscriptionRepository.saveAll(userSubscriptions);


        UserSubscription userSubscription = userSubscriptionRepository.findByPaymentId(paymentId);
        userSubscription.setScheduledId("");
        userSubscription.setUserSubscriptionStatus(UserSubscriptionStatus.ACTIVE);


        // 기존 유저 결제 정보 및 구독 정보 가져오기
        UserPaymentMethod userPaymentMethod = userPaymentMethodRepository.findByUser(user);
        String billingKey = userPaymentMethod.getBillingKey();

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
