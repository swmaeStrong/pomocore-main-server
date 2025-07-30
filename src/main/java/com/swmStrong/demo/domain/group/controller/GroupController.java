package com.swmStrong.demo.domain.group.controller;

import com.swmStrong.demo.common.exception.code.SuccessCode;
import com.swmStrong.demo.common.response.ApiResponse;
import com.swmStrong.demo.common.response.CustomResponseEntity;
import com.swmStrong.demo.config.security.principal.SecurityPrincipal;
import com.swmStrong.demo.domain.group.dto.CreateGroupDto;
import com.swmStrong.demo.domain.group.dto.GroupListResponseDto;
import com.swmStrong.demo.domain.group.dto.UpdateGroupDto;
import com.swmStrong.demo.domain.group.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name="그룹")
@RestController
@RequestMapping("/group")
public class GroupController {
    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @Operation(
            security = @SecurityRequirement(name = "bearerAuth"),
            summary = "그룹 생성",
            description =
                    "<p> 해당 유저가 그룹장이 되어 그룹을 생성한다. </p>" +
                    "<p> 비회원은 그룹을 생성할 수 없다. </p>"
    )
    @PreAuthorize("hasAuthority('USER')")
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createGroup(
            @AuthenticationPrincipal SecurityPrincipal principal,
            @RequestBody CreateGroupDto createGroupDto
    ) {
        groupService.createGroup(principal.userId(), createGroupDto);
        return CustomResponseEntity.of(
                SuccessCode._CREATED
        );
    }

    @Operation(
            summary = "그룹 조회",
            description =
                    "<p> 그룹 전체를 조회한다. </p>" +
                    "<p> 웹뷰/네이티브 구현 여부에 따라 검색 구현이 달라질 수 있어 전체 반환만 우선 작성 </p>"

    )
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<GroupListResponseDto>>> getGroup() {
        return CustomResponseEntity.of(
                SuccessCode._OK,
                groupService.getGroups()
        );
    }

    @Operation(
            security = @SecurityRequirement(name = "bearerAuth"),
            summary = "그룹 조회",
            description =
                    "<p> 그룹 전체를 조회한다. </p>" +
                    "<p> 웹뷰/네이티브 구현 여부에 따라 검색 구현이 달라질 수 있어 전체 반환만 우선 작성 </p>"
    )
    @PatchMapping("/{groupId}")
    public ResponseEntity<ApiResponse<Void>> updateGroup(
            @AuthenticationPrincipal SecurityPrincipal principal,
            @PathVariable Long groupId,
            @RequestBody UpdateGroupDto updateGroupDto
    ) {
        groupService.updateGroup(principal.userId(), groupId, updateGroupDto);
        return CustomResponseEntity.of(
                SuccessCode._OK
        );
    }
}
