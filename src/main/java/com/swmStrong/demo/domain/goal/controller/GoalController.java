package com.swmStrong.demo.domain.goal.controller;

import com.swmStrong.demo.common.exception.code.SuccessCode;
import com.swmStrong.demo.common.response.ApiResponse;
import com.swmStrong.demo.common.response.CustomResponseEntity;
import com.swmStrong.demo.config.security.principal.SecurityPrincipal;
import com.swmStrong.demo.domain.goal.dto.GoalResponseDto;
import com.swmStrong.demo.domain.goal.dto.SaveUserGoalDto;
import com.swmStrong.demo.domain.goal.service.GoalService;
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

    @GetMapping("/{periodType}")
    public ResponseEntity<ApiResponse<List<GoalResponseDto>>> getUserGoals(
            @AuthenticationPrincipal SecurityPrincipal principal,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            String period
    ) {
        return CustomResponseEntity.of(
                SuccessCode._OK,
                goalService.getUserGoals(principal.userId(), date, period)
        );
    }
}
