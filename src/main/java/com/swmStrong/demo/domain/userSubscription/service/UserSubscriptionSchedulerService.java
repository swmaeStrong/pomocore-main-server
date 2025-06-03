package com.swmStrong.demo.domain.userSubscription.service;

import com.swmStrong.demo.domain.userSubscription.entity.UserSubscription;
import com.swmStrong.demo.domain.userSubscription.entity.UserSubscriptionStatus;
import com.swmStrong.demo.domain.userSubscription.repository.UserSubscriptionRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserSubscriptionSchedulerService {
    private final UserSubscriptionRepository userSubscriptionRepository;

    public UserSubscriptionSchedulerService(UserSubscriptionRepository userSubscriptionRepository) {
        this.userSubscriptionRepository = userSubscriptionRepository;
    }

    // 매 정시마다 구독 만료시킴
    @Scheduled(cron = "0 0 * * * *")
    public void checkAndExpireSubscriptions() {
        LocalDateTime now = LocalDateTime.now();
        List<UserSubscription> expiredList = userSubscriptionRepository.
                findByUserSubscriptionStatusAndEndTimeBefore(
                        UserSubscriptionStatus.ACTIVE, now);

        for (UserSubscription sub : expiredList) {
            sub.setUserSubscriptionStatus(UserSubscriptionStatus.EXPIRED);
            userSubscriptionRepository.save(sub);
        }
    }
}
