package com.swmStrong.demo.domain.userPaymentMethod.service;
import com.swmStrong.demo.domain.portone.dto.PaymentMethod;
import com.swmStrong.demo.domain.user.entity.User;
import com.swmStrong.demo.domain.userPaymentMethod.dto.UserPaymentMethodRes;
import com.swmStrong.demo.domain.userPaymentMethod.entity.UserPaymentMethod;

import java.util.List;

public interface UserPaymentMethodService {
    List<UserPaymentMethodRes> getMyPaymentMethods(String userId);
    UserPaymentMethod storeMyPaymentMethod(String userId, String billingKey);
    void deleteMyPaymentMethod(String userPaymentMethodId);
}
