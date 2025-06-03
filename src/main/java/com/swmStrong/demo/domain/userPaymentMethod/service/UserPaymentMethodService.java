package com.swmStrong.demo.domain.userPaymentMethod.service;
import com.swmStrong.demo.domain.user.entity.User;
import com.swmStrong.demo.domain.userPaymentMethod.dto.UserPaymentMethodRes;

import java.util.List;

public interface UserPaymentMethodService {
    List<UserPaymentMethodRes> getMyPaymentMethods(String userId);
    void storeMyPaymentMethod(String userId, String billingKey);
}
