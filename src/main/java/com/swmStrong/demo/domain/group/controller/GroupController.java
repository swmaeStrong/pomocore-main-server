package com.swmStrong.demo.domain.group.controller;

import com.swmStrong.demo.common.exception.code.SuccessCode;
import com.swmStrong.demo.common.response.ApiResponse;
import com.swmStrong.demo.common.response.CustomResponseEntity;
import com.swmStrong.demo.config.security.principal.SecurityPrincipal;
import com.swmStrong.demo.domain.common.enums.PeriodType;
import com.swmStrong.demo.domain.group.dto.*;
import com.swmStrong.demo.domain.group.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
            security = @SecurityRequirement(name = "bearerAuth"),
            summary = "유저 강퇴",
            description =
                    "<p> 그룹에서 유저를 강퇴한다. </p>" +
                    "<p> 그룹장만 사용할 수 있다. </p>"
    )
    @DeleteMapping("/{groupId}/ban")
    public ResponseEntity<ApiResponse<Void>> banUserInGroup(
            @AuthenticationPrincipal SecurityPrincipal principal,
            @PathVariable Long groupId,
            @RequestBody BanMemberDto banMemberDto
    ) {
        groupService.banMember(principal.userId(), groupId, banMemberDto);
        return CustomResponseEntity.of(
                SuccessCode._OK
        );
    }

    @Operation(
            security = @SecurityRequirement(name = "bearerAuth"),
            summary = "그룹장 권한 위임",
            description =
                    "<p> 그룹장 권한을 위임한다. </p>" +
                    "<p> 그룹장만 사용할 수 있다. </p>"
    )
    @PatchMapping("/{groupId}/authorize")
    public ResponseEntity<ApiResponse<Void>> authorizeUserInGroup(
            @AuthenticationPrincipal SecurityPrincipal principal,
            @PathVariable Long groupId,
            @RequestBody AuthorizeMemberDto authorizeMemberDto
    ) {
        groupService.authorizeOwner(principal.userId(), groupId, authorizeMemberDto);
        return CustomResponseEntity.of(
                SuccessCode._OK
        );
    }

    @Operation(
            security = @SecurityRequirement(name = "bearerAuth"),
            summary = "그룹 상세 조회",
            description =
                    "<p> 선택한 그룹의 상세 정보를 조회한다. </p>" +
                    "<p> TODO: 뷰 구현에 따라 여기는 가입된 유저만 볼 수 있도록 처리해야 한다. </p>"
    )
    @GetMapping("/{groupId}")
    public ResponseEntity<ApiResponse<GroupDetailsDto>> getGroupDetails(@PathVariable Long groupId) {
        return CustomResponseEntity.of(
                SuccessCode._OK,
                groupService.getGroupDetails(groupId)
        );
    }

    @Operation(
            security = @SecurityRequirement(name = "bearerAuth"),
            summary = "내 그룹 조회",
            description =
                    "<p> 내가 속한 그룹을 조회한다. </p>"
    )
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<GroupListResponseDto>>> getMyGroups(@AuthenticationPrincipal SecurityPrincipal principal) {
        return CustomResponseEntity.of(
                SuccessCode._OK,
                groupService.getMyGroups(principal.userId())
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
            summary = "그룹 가입",
            description =
                    "<p> 해당 그룹에 가입한다. </p>" +
                    "<p> 이미 가입된 그룹일 경우 에러를 반환한다. </p>"
    )
    @PreAuthorize("hasAuthority('USER')")
    @PostMapping("/{groupId}/join")
    public ResponseEntity<ApiResponse<Void>> joinGroup(
            @AuthenticationPrincipal SecurityPrincipal principal,
            @PathVariable Long groupId,
            @RequestBody(required = false) PasswordRequestDto passwordRequestdto
    ) {
        groupService.joinGroup(principal.userId(), groupId, passwordRequestdto);
        return CustomResponseEntity.of(
                SuccessCode._OK
        );
    }

    @Operation(
            security = @SecurityRequirement(name = "bearerAuth"),
            summary = "그룹 탈퇴",
            description =
                    "<p> 해당 그룹에서 탈퇴한다. </p>" +
                    "<p> 그룹장은 탈퇴할 수 없다. </p>"
    )
    @PreAuthorize("hasAuthority('USER')")
    @DeleteMapping("/{groupId}/quit")
    public ResponseEntity<ApiResponse<Void>> quitGroup(
            @AuthenticationPrincipal SecurityPrincipal principal,
            @PathVariable Long groupId
    ) {
        groupService.quitGroup(principal.userId(), groupId);
        return CustomResponseEntity.of(
                SuccessCode._OK
        );
    }

    @Operation(
            security = @SecurityRequirement(name = "bearerAuth"),
            summary = "그룹 수정",
            description =
                    "<p> 그룹 정보를 수정한다. </p>" +
                    "<p> 그룹장만 수정할 수 있다. </p>"
    )
    @PreAuthorize("hasAuthority('USER')")
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

    @Operation(
            security = @SecurityRequirement(name = "bearerAuth"),
            summary = "그룹 삭제",
            description =
                    "<p> 그룹을 삭제한다. </p>" +
                    "<p> 그룹장만 삭제할 수 있으며, 그룹에 혼자만 남았을 때만 삭제 가능하다. </p>"
    )
    @PreAuthorize("hasAuthority('USER')")
    @DeleteMapping("/{groupId}")
    public ResponseEntity<ApiResponse<Void>> deleteGroup(
            @AuthenticationPrincipal SecurityPrincipal principal,
            @PathVariable Long groupId
    ) {
        groupService.deleteGroup(principal.userId(), groupId);
        return CustomResponseEntity.of(
                SuccessCode._NO_CONTENT
        );
    }

    @Operation(
            security = @SecurityRequirement(name = "bearerAuth"),
            summary = "그룹 이름 유효성 검사",
            description =
                    "<p> 그룹 이름을 검사한다. </p>"
    )
    @GetMapping("/name/check")
    public ResponseEntity<ApiResponse<Boolean>> checkGroupName(@RequestParam String name) {
        return CustomResponseEntity.of(
                SuccessCode._OK,
                groupService.validateGroupName(name)
        );
    }

    @Operation(
            security = @SecurityRequirement(name = "bearerAuth"),
            summary = "그룹 목표 설정",
            description =
                    "<p> 그룹의 목표를 설정한다. </p>" +
                    "<p> 그룹장만 사용할 수 있다. </p>"
    )
    @PostMapping("/{groupId}/goal")
    public ResponseEntity<ApiResponse<Void>> setGroupGoal(
            @AuthenticationPrincipal SecurityPrincipal principal,
            @PathVariable Long groupId,
            @RequestBody SaveGroupGoalDto saveGroupGoalDto
    ) {
        groupService.setGroupGoal(principal.userId(), groupId, saveGroupGoalDto);
        return CustomResponseEntity.of(
                SuccessCode._OK
        );
    }

    @Operation(
            security = @SecurityRequirement(name = "bearerAuth"),
            summary = "그룹 목표 조회",
            description =
                    "<p> 그룹의 목표를 조회한다. </p>" +
                    "<p> 아직 어떻게 나오는지 잘 모르겠다. </p>"
    )
    @GetMapping("/{groupId}/goal")
    public ResponseEntity<ApiResponse<List<GroupGoalResponseDto>>> getGroupGoals(
            @PathVariable Long groupId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return CustomResponseEntity.of(
                SuccessCode._OK,
                groupService.getGroupGoals(groupId, date)
        );
    }

    @Operation(
            security = @SecurityRequirement(name = "bearerAuth"),
            summary = "그룹 목표 삭제",
            description =
                    "<p> 그룹의 목표를 삭제한다. </p>" +
                    "<p> 그룹장만 삭제할 수 있다. </p>"
    )
    @DeleteMapping("/{groupId}/goal")
    public ResponseEntity<ApiResponse<Void>> deleteGroupGoal(
            @AuthenticationPrincipal SecurityPrincipal principal,
            @PathVariable Long groupId,
            @RequestBody DeleteGroupGoalDto deleteGroupGoalDto
    ) {
        groupService.deleteGroupGoal(principal.userId(), groupId, deleteGroupGoalDto);
        return CustomResponseEntity.of(
                SuccessCode._OK
        );
    }

    @GetMapping("/{groupId}/leaderboard")
    public ResponseEntity<ApiResponse<GroupLeaderboardDto>> getGroupLeaderboard(
            @PathVariable Long groupId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return CustomResponseEntity.of(
                SuccessCode._OK,
                groupService.getGroupLeaderboard(groupId, "work", PeriodType.DAILY ,date)
        );
    }
}
