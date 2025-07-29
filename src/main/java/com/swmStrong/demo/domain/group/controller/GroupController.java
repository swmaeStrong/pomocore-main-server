package com.swmStrong.demo.domain.group.controller;

import com.swmStrong.demo.common.exception.code.SuccessCode;
import com.swmStrong.demo.common.response.ApiResponse;
import com.swmStrong.demo.common.response.CustomResponseEntity;
import com.swmStrong.demo.config.security.principal.SecurityPrincipal;
import com.swmStrong.demo.domain.group.dto.CreateGroupDto;
import com.swmStrong.demo.domain.group.service.GroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/group")
public class GroupController {
    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createGroup(
            @AuthenticationPrincipal SecurityPrincipal principal,
            @RequestBody CreateGroupDto createGroupDto
    ) {
        groupService.createGroup(principal.userId(), createGroupDto);
        return CustomResponseEntity.of(
                SuccessCode._OK
        );
    }
}
