package com.swmStrong.demo.message.event;

public record UnregisteredUserLogoutEvent(
        String userId
) {
    public static UnregisteredUserLogoutEvent of(String userId){
        return new UnregisteredUserLogoutEvent(userId);
    }
}
