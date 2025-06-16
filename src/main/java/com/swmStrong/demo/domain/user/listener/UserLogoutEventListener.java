package com.swmStrong.demo.domain.user.listener;

import com.swmStrong.demo.domain.user.facade.UserDeleteProvider;
import com.swmStrong.demo.message.event.UnregisteredUserLogoutEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class UserLogoutEventListener {

    private final UserDeleteProvider userDeleteProvider;

    public UserLogoutEventListener(UserDeleteProvider userDeleteProvider) {
        this.userDeleteProvider = userDeleteProvider;
    }

    @EventListener
    public void handleUnregisteredUserLogoutEvent(UnregisteredUserLogoutEvent event) {
        userDeleteProvider.deleteById(event.userId());
    }
}
