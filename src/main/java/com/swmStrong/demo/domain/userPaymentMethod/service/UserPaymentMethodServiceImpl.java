package com.swmStrong.demo.domain.userPaymentMethod.service;

import com.swmStrong.demo.common.exception.ApiException;
import com.swmStrong.demo.common.exception.code.ErrorCode;
import com.swmStrong.demo.domain.portone.dto.PaymentMethod;
import com.swmStrong.demo.domain.user.entity.User;
import com.swmStrong.demo.domain.user.repository.UserRepository;
import com.swmStrong.demo.domain.userPaymentMethod.dto.UserPaymentMethodRes;
import com.swmStrong.demo.domain.userPaymentMethod.entity.UserPaymentMethod;
import com.swmStrong.demo.domain.userPaymentMethod.repository.UserPaymentMethodRepository;
import com.swmStrong.demo.infra.portone.PortOneBillingClient;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserPaymentMethodServiceImpl implements UserPaymentMethodService {
    private final UserPaymentMethodRepository userPaymentMethodRepository;
    private final PortOneBillingClient portOneBillingClient;
    private final UserRepository userRepository;

    public UserPaymentMethodServiceImpl(UserPaymentMethodRepository userPaymentMethodRepository,
                                        PortOneBillingClient portOneBillingClient, UserRepository userRepository) {
        this.userPaymentMethodRepository = userPaymentMethodRepository;
        this.portOneBillingClient = portOneBillingClient;
        this.userRepository = userRepository;
    }

    public List<UserPaymentMethodRes> getMyPaymentMethods(String userId) {
        List<UserPaymentMethod> userPaymentMethods = userPaymentMethodRepository.findByUserId(userId);

        return userPaymentMethods.stream()
                .map(UserPaymentMethodRes::from)
                .collect(Collectors.toList());
    }

    public UserPaymentMethod storeMyPaymentMethod(String userId, String billingKey) {

        User user = userRepository.findById(userId).
                orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        List<UserPaymentMethod> paymentMethods = userPaymentMethodRepository.findByUserId(userId);
        for (UserPaymentMethod paymentMethod : paymentMethods) {
            if (paymentMethod.getBillingKey().equals(billingKey)) {
                throw new ApiException(ErrorCode.DUPLICATE_BILLING_KEY);
            }
        }

        PaymentMethod paymentMethod = portOneBillingClient.getPaymentMethod(billingKey);
        UserPaymentMethod userPaymentMethod = UserPaymentMethod.builder()
                .user(user)
                .billingKey(billingKey)
                .pgProvider(paymentMethod.pgProvider())
                .issuer(paymentMethod.issuer())
                .number(paymentMethod.number())
                .build();
        userPaymentMethodRepository.save(userPaymentMethod);
        return userPaymentMethod;
    }

    public void deleteMyPaymentMethod(String userPaymentMethodId){
        UserPaymentMethod userPaymentMethod = userPaymentMethodRepository.findById(userPaymentMethodId).
                orElseThrow(() -> new ApiException(ErrorCode.PAYMENT_METHOD_NOT_FOUND));
        userPaymentMethodRepository.delete(userPaymentMethod);
    }

}
