package com.swmStrong.demo.domain.goal.controller;

import com.swmStrong.demo.common.exception.code.SuccessCode;
import com.swmStrong.demo.common.response.ApiResponse;
import com.swmStrong.demo.common.response.CustomResponseEntity;
import com.swmStrong.demo.config.security.principal.SecurityPrincipal;
import com.swmStrong.demo.domain.goal.dto.DeleteUserGoalDto;
import com.swmStrong.demo.domain.goal.dto.GoalResponseDto;
import com.swmStrong.demo.domain.goal.dto.SaveUserGoalDto;
import com.swmStrong.demo.domain.goal.service.GoalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "목표")
@RequestMapping("/goal")
@RestController
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @Operation(
            security = @SecurityRequirement(name = "bearerAuth"),
            summary = "목표 설정",
            description =
                    "<p> 유저의 목표를 설정한다. </p>" +
                    "<p> period에 넣을 수 있는 값: daily, weekly, monthly </p>"
    )
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> save(
            @AuthenticationPrincipal SecurityPrincipal principal,
            @RequestBody SaveUserGoalDto saveUserGoalDto
    ) {
        goalService.saveUserGoal(principal.userId(), saveUserGoalDto);
        return CustomResponseEntity.of(
                SuccessCode._OK
        );
    }

    @Operation(
            security = @SecurityRequirement(name = "bearerAuth"),
            summary = "목표 삭제",
            description =
                    "<p> 유저의 목표를 삭제한다. </p>" +
                    "<p> period에 넣을 수 있는 값: daily, weekly, monthly </p>"
    )
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal SecurityPrincipal principal,
            @RequestBody DeleteUserGoalDto deleteUserGoalDto
    ) {
        goalService.deleteUserGoal(principal.userId(), deleteUserGoalDto);
        return CustomResponseEntity.of(
                SuccessCode._NO_CONTENT
        );
    }

    @Operation(
            security = @SecurityRequirement(name = "bearerAuth"),
            summary = "목표 조회",
            description =
                    "<p> 목표와 달성치를 조회한다. </p>"
    )
    @GetMapping("/{period}")
    public ResponseEntity<ApiResponse<List<GoalResponseDto>>> getUserGoals(
            @AuthenticationPrincipal SecurityPrincipal principal,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return CustomResponseEntity.of(
                SuccessCode._OK,
                goalService.getUserGoals(principal.userId(), date)
        );
    }
}
