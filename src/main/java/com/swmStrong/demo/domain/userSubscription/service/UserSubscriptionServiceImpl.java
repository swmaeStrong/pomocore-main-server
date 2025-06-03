package com.swmStrong.demo.domain.userSubscription.service;
import com.swmStrong.demo.common.exception.ApiException;
import com.swmStrong.demo.common.exception.code.ErrorCode;
import com.swmStrong.demo.domain.portone.dto.PaymentMethod;
import com.swmStrong.demo.domain.portone.dto.ScheduledPaymentResult;
import com.swmStrong.demo.domain.subscriptionPlan.entity.SubscriptionPlan;
import com.swmStrong.demo.domain.subscriptionPlan.entity.SubscriptionPlanType;
import com.swmStrong.demo.domain.subscriptionPlan.repository.SubscriptionPlanRepository;
import com.swmStrong.demo.domain.user.entity.User;
import com.swmStrong.demo.domain.user.repository.UserRepository;
import com.swmStrong.demo.domain.userPaymentMethod.dto.UserPaymentMethodRes;
import com.swmStrong.demo.domain.userPaymentMethod.entity.UserPaymentMethod;
import com.swmStrong.demo.domain.userPaymentMethod.repository.UserPaymentMethodRepository;
import com.swmStrong.demo.domain.userPaymentMethod.service.UserPaymentMethodService;
import com.swmStrong.demo.domain.userSubscription.dto.UserSubscriptionRes;
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
import java.util.stream.Collectors;


@Service
public class UserSubscriptionServiceImpl implements UserSubscriptionService {
    private final UserRepository userRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final PortOneBillingClient portOneBillingClient;
    private final UserPaymentMethodRepository userPaymentMethodRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final UserPaymentMethodService userPaymentMethodService;
    public UserSubscriptionServiceImpl(UserRepository userRepository,
                                       SubscriptionPlanRepository subscriptionPlanRepository,
                                       PortOneBillingClient portOneBillingClient,
                                       UserPaymentMethodRepository userPaymentMethodRepository,
                                       UserSubscriptionRepository userSubscriptionRepository,
                                       UserPaymentMethodService userPaymentMethodService) {
        this.userRepository = userRepository;
        this.subscriptionPlanRepository = subscriptionPlanRepository;
        this.portOneBillingClient = portOneBillingClient;
        this.userPaymentMethodRepository = userPaymentMethodRepository;
        this.userSubscriptionRepository = userSubscriptionRepository;
        this.userPaymentMethodService = userPaymentMethodService;
    }


    @Transactional
    public void createUserSubscriptionWithBillingKey(String userId, String subscriptionPlanId, String billingKey){

        // 1. 유저, 플랜 조회 (예외는 그대로)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        SubscriptionPlan plan = subscriptionPlanRepository.findById(subscriptionPlanId)
                .orElseThrow(() -> new ApiException(ErrorCode.SUBSCRIPTION_PLAN_NOT_FOUND));

        // 2. 중복 구독 검사
        if (userSubscriptionRepository.existsByUserSubscriptionStatusAndUserId(
                UserSubscriptionStatus.ACTIVE, userId)) {
            throw new ApiException(ErrorCode.DUPLICATE_USER_SUBSCRIPTION);
        }

        // 3. 결제수단 등록 (없는 경우만)
        UserPaymentMethod userPaymentMethod = userPaymentMethodService.
                storeMyPaymentMethod(userId, billingKey);

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
                    .autoUpdate(true)
                    .userPaymentMethod(userPaymentMethod)
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

    //TODO: 결제에 대한 응답이 error 응답인 경우에는 구독 연장이 되지 않는데, 다음에 재시도 로직 넣으면 좋을 것 같다.
    public void createUserSubscriptionWithPaymentMethod(
            String userId,
            String subscriptionPlanId,
            String userPaymentMethodId){

        // 1. 유저, 플랜 조회 (예외는 그대로)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        SubscriptionPlan plan = subscriptionPlanRepository.findById(subscriptionPlanId)
                .orElseThrow(() -> new ApiException(ErrorCode.SUBSCRIPTION_PLAN_NOT_FOUND));

        // 2. 중복 구독 검사
        if (userSubscriptionRepository.existsByUserSubscriptionStatusAndUserId(
                UserSubscriptionStatus.ACTIVE, userId)) {
            throw new ApiException(ErrorCode.DUPLICATE_USER_SUBSCRIPTION);
        }

        // 3. 결제 수단 로드
        UserPaymentMethod userPaymentMethod =
                userPaymentMethodRepository.findById(userPaymentMethodId).
                        orElseThrow(()-> new ApiException(ErrorCode.PAYMENT_METHOD_NOT_FOUND));

        // 4. 결제 시도
        PaymentResult result = portOneBillingClient.requestPayment(
                UUID.randomUUID().toString(),
                userPaymentMethod.getBillingKey(),
                userId,
                plan.getSubscriptionPlanType().getDescription(),
                plan.getPrice()
        );

        // 5. 결제 성공시 구독 등록
        if (result.isSuccess()) {
            UserSubscription subscription = UserSubscription.builder()
                    .user(user)
                    .subscriptionPlan(plan)
                    .userPaymentMethod(userPaymentMethod)
                    .autoUpdate(true)
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
    public void extendUserSubscriptions(List<UserSubscription> userSubscriptions) {
        for (UserSubscription userSubscription : userSubscriptions) {
            try {
                UserPaymentMethod userPaymentMethod = userSubscription.getUserPaymentMethod();
                if (!userPaymentMethod.isDeleted()) {
                    String userPaymentMethodId = userPaymentMethod.getId();
                    String subscriptionPlanId = userSubscription.getSubscriptionPlan().getId();
                    String userId = userSubscription.getUser().getId();
                    createUserSubscriptionWithPaymentMethod(userId, subscriptionPlanId, userPaymentMethodId);
                }
            } catch (Exception e) {
                // 오류 생길 만한 부분이 결제 쪽인데, 웹훅 및 포트원 관리자 콘솔을 통해 로깅이 되어서 큰 문제가 없다고 판단
                ;
            }
        }
    }


    //TODO: 외부 API 문서상으로는 바로 결제 취소가 되지 않을 수 있고 결제 취소 요청중인 상태가 될 수 있다고 한다.
    //TODO: 아직까지는 바로 취소 응답이 오기 때문에 이 부분도 추후 처리
    public void cancelCurrentSubscription(String userSubscriptionId, String reason){

        UserSubscription userSubscription = userSubscriptionRepository.findById(userSubscriptionId).
                orElseThrow(() -> new RuntimeException("subscription not found"));

        PaymentResult paymentResult =
                portOneBillingClient.cancelLastPayment(userSubscription.getPaymentId(), reason);

        if (paymentResult.isSuccess()) {
            userSubscription.setUserSubscriptionStatus(UserSubscriptionStatus.CANCELLED);
            userSubscription.setEndTime(LocalDateTime.now());
            userSubscriptionRepository.save(userSubscription);
        }
    }

    public UserSubscriptionRes getCurrentSubscription(String userId){
        UserSubscription userSubscription =
                userSubscriptionRepository.findUserSubscriptionByUserSubscriptionStatus(
                        UserSubscriptionStatus.ACTIVE).
                        orElseThrow(() -> new ApiException(ErrorCode.USER_SUBSCRIPTION_NOT_FOUND) );

        return UserSubscriptionRes.from(userSubscription, userSubscription.getSubscriptionPlan());
    }

    public List<UserSubscriptionRes> getAllSubscriptions(String userId){
        List<UserSubscription> userSubscriptions=
        userSubscriptionRepository.findUserSubscriptionByUserId(userId);

        return userSubscriptions.stream()
                .map(us -> UserSubscriptionRes.from(us, us.getSubscriptionPlan()))
                .toList();
    }

}
