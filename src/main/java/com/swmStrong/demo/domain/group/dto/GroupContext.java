package com.swmStrong.demo.domain.group.dto;

import com.swmStrong.demo.domain.group.entity.Group;
import com.swmStrong.demo.domain.user.entity.User;

public record GroupContext(
        User user,
        Group group
) {
}
