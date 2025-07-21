package com.swmStrong.demo.domain.goal.controller;

import com.swmStrong.demo.common.exception.code.SuccessCode;
import com.swmStrong.demo.common.response.ApiResponse;
import com.swmStrong.demo.common.response.CustomResponseEntity;
import com.swmStrong.demo.config.security.principal.SecurityPrincipal;
import com.swmStrong.demo.domain.goal.dto.GoalResponseDto;
import com.swmStrong.demo.domain.goal.dto.SaveUserGoalDto;
import com.swmStrong.demo.domain.goal.service.GoalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

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
                    "<p> 주간, 월간 목표 같은건 아직 못정한다. </p>"
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
            summary = "목표 조회",
            description =
                    "<p> 목표와 달성치를 조회한다. </p>" +
                    "<p> 입력 가능한 period: daily, weekly, monthly </p>" +
                    "<p> 설정 목표애 대한 분기 처리를 안했기 때문에 daily를 넣으면 된다. </p>"
    )
    @GetMapping("/{period}")
    public ResponseEntity<ApiResponse<List<GoalResponseDto>>> getUserGoals(
            @AuthenticationPrincipal SecurityPrincipal principal,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @PathVariable
            String period
    ) {
        return CustomResponseEntity.of(
                SuccessCode._OK,
                goalService.getUserGoals(principal.userId(), date, period)
        );
    }
}
