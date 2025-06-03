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
    private final UserSubscriptionService userSubscriptionService;
    public UserSubscriptionSchedulerService(UserSubscriptionRepository userSubscriptionRepository,
                                            UserSubscriptionService userSubscriptionService) {
        this.userSubscriptionRepository = userSubscriptionRepository;
        this.userSubscriptionService = userSubscriptionService;
    }

    // 매 정시마다 구독 만료시킴
    @Scheduled(cron = "0 0 * * * *")
    public void checkAndExpireSubscriptions() {
        LocalDateTime now = LocalDateTime.now();
        List<UserSubscription> expiredList = userSubscriptionRepository.
                findByUserSubscriptionStatusAndEndTimeBefore(
                        UserSubscriptionStatus.ACTIVE, now);
        List<UserSubscription> userSubscriptions = new java.util.ArrayList<>(List.of());

        for (UserSubscription sub : expiredList) {
            if (sub.isAutoUpdate()) {
                // 자동 업데이트 필요한 유저는 따로 추가
                userSubscriptions.add(sub);
            }
            sub.setUserSubscriptionStatus(UserSubscriptionStatus.EXPIRED);
            userSubscriptionRepository.save(sub);
        }

        userSubscriptionService.extendUserSubscriptions(userSubscriptions);
    }
}
