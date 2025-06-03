package com.swmStrong.demo.domain.subscriptionPlan.repository;

import com.swmStrong.demo.domain.subscriptionPlan.entity.SubscriptionPlan;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Table(
    uniqueConstraints = {
        @UniqueConstraint(
            name = "unique_plan_type_billing_price",
            columnNames = {"subscriptionPlanType", "billingCycle", "price"}
        )
    }
)

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, String> {
    List<SubscriptionPlan> findAllByAvail(boolean isAvail);
}
