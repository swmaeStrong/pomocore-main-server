package com.swmStrong.demo.domain.userPaymentMethod.repository;
import com.swmStrong.demo.domain.user.entity.User;
import com.swmStrong.demo.domain.userPaymentMethod.entity.UserPaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPaymentMethodRepository extends JpaRepository<UserPaymentMethod, String> {
    UserPaymentMethod findByUser(User user);
    boolean existsByBillingKeyAndUserId(String billingKey, String userId);
    Optional<UserPaymentMethod> findByBillingKeyAndUserId(String billingKey, String userId);
}
