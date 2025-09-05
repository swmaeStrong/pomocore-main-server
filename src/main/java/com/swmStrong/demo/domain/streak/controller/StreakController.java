package com.swmStrong.demo.domain.streak.controller;

import com.swmStrong.demo.common.exception.code.SuccessCode;
import com.swmStrong.demo.common.response.ApiResponse;
import com.swmStrong.demo.common.response.CustomResponseEntity;
import com.swmStrong.demo.config.security.principal.SecurityPrincipal;
import com.swmStrong.demo.domain.streak.dto.DailyActivityResponseDto;
import com.swmStrong.demo.domain.streak.dto.StreakDashboardDto;
import com.swmStrong.demo.domain.streak.dto.StreakResponseDto;
import com.swmStrong.demo.domain.streak.service.StreakService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "스트릭")
@RequestMapping("/streak")
@RestController
public class StreakController {

    private final StreakService streakService;

    public StreakController(StreakService streakService) {
        this.streakService = streakService;
    }

    @Operation(
            security = @SecurityRequirement(name = "bearerAuth"),
            summary = "유저 최대 스트릭 조회",
            description =
                    "<p> 유저의 최대 스트릭과 현재 스트릭을 조회한다. </p>" +
                    "<p> 이거 만든 이후로의 스트릭만 조회 가능 </p>"
    )
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<StreakResponseDto>> getCount(@AuthenticationPrincipal SecurityPrincipal principal) {
        return CustomResponseEntity.of(
                SuccessCode._OK,
                streakService.getStreakCount(principal.userId())
        );
    }

    @Operation(
            security = @SecurityRequirement(name = "bearerAuth"),
            summary = "유저 스트릭 조회 (달력형)",
            description =
                    "<p> 유저의 스트릭 달력을 조회한다. (듀오링고 스타일) </p>" +
                    "<p> 날짜를 넣으면 자동으로 그 달의 1일 ~ 말일 까지의 데이터를 반환한다. 없으면 반환 안한다. </p>"
    )
    @GetMapping("/calendar")
    public ResponseEntity<ApiResponse<List<DailyActivityResponseDto>>> getActivitiesByMonth(
            @AuthenticationPrincipal SecurityPrincipal principal,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return CustomResponseEntity.of(
                SuccessCode._OK,
                streakService.getDailyActivitiesByMonth(principal.userId(), date)
        );
    }

    @Operation(
            security = @SecurityRequirement(name = "bearerAuth"),
            summary = "유저 스트릭 조회(격자형)",
            description =
                    "<p> 유저의 스트릭 격자를 조회한다. (깃허브 스타일) </p>" +
                    "<p> 날짜를 넣으면 자동으로 이전일까지를 조회한다. 없으면 반환 안한다. </p>" +
                    "<p> days-before 에 값을 넣지 않을 시의 기본값은 100일이다. </p>"
    )
    @GetMapping("/matrix")
    public ResponseEntity<ApiResponse<StreakDashboardDto>> getDailyActivitiesBetweenDateAndDaysBefore(
            @AuthenticationPrincipal SecurityPrincipal principal,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @RequestParam(name = "days-before", required = false, defaultValue = "100")
            Long daysBefore
    ) {
        return CustomResponseEntity.of(
                SuccessCode._OK,
                streakService.getDailyActivitiesBetweenDateAndDaysBefore(principal.userId(), date, daysBefore)
        );
    }
}
