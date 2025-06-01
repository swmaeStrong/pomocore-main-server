package com.swmStrong.demo.domain.userPayment.repository;
import com.swmStrong.demo.domain.user.entity.User;
import com.swmStrong.demo.domain.userPayment.entity.UserPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPaymentRepository extends JpaRepository<UserPayment, String> {
    UserPayment findByUser(User user);
    boolean existsByBillingKeyAndUserId(String billingKey, String userId);
}
