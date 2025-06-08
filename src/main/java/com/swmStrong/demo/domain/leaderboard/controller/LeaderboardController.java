package com.swmStrong.demo.domain.leaderboard.controller;

import com.swmStrong.demo.common.exception.code.SuccessCode;
import com.swmStrong.demo.common.response.ApiResponse;
import com.swmStrong.demo.common.response.CustomResponseEntity;
import com.swmStrong.demo.domain.common.enums.PeriodType;
import com.swmStrong.demo.domain.leaderboard.dto.LeaderboardResponseDto;
import com.swmStrong.demo.domain.leaderboard.service.LeaderboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Tag(name = "리더보드")
@RestController
@RequestMapping("/leaderboard")
public class LeaderboardController {
    private final LeaderboardService leaderboardService;

    public LeaderboardController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @Operation(
            summary = "리더보드 조회(일별)",
            description =
                "<p> 카테고리별로 N등 까지의 유저와 점수, 순위를 출력한다. </p>" +
                "<p> page 의 기본값은 1이다. </p>" +
                "<p> 날짜를 입력하지 않는 경우, 오늘을 기준으로 한다. </p>"
    )
    @GetMapping("/{category}/daily")
    public ResponseEntity<ApiResponse<List<LeaderboardResponseDto>>> getTopUsersByCategoryDaily(
            @PathVariable String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
            ) {
        return CustomResponseEntity.of(
                SuccessCode._OK,
                leaderboardService.getLeaderboardPage(category, page, size, date, PeriodType.DAILY)
        );
    }

    @Operation(
            summary = "리더보드 조회(주간)",
            description =
                "<p> 카테고리별로 N등 까지의 유저와 점수, 순위를 출력한다. </p>" +
                "<p> page 의 기본값은 1이다. </p>" +
                "<p> 날짜를 입력하지 않는 경우, 오늘을 기준으로 한다. </p>"
    )
    @GetMapping("/{category}/weekly")
    public ResponseEntity<ApiResponse<List<LeaderboardResponseDto>>> getTopUsersByCategoryWeekly(
            @PathVariable String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return CustomResponseEntity.of(
                SuccessCode._OK,
                leaderboardService.getLeaderboardPage(category, page, size, date, PeriodType.WEEKLY)
        );
    }

    @Operation(
            summary = "리더보드 조회(월간)",
            description =
                "<p> 카테고리별로 N등 까지의 유저와 점수, 순위를 출력한다. </p>" +
                "<p> page 의 기본값은 1이다. </p>" +
                "<p> 날짜를 입력하지 않는 경우, 오늘을 기준으로 한다. </p>"
    )
    @GetMapping("/{category}/monthly")
    public ResponseEntity<ApiResponse<List<LeaderboardResponseDto>>> getTopUsersByCategoryMonthly(
            @PathVariable String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return CustomResponseEntity.of(
                SuccessCode._OK,
                leaderboardService.getLeaderboardPage(category, page, size, date, PeriodType.MONTHLY)
        );
    }

    @Operation(
            summary = "카테고리의 모든 유저 조회",
            description =
                "<p> 카테고리별로, 모든 유저의 점수, 순위를 출력한다. </p>" +
                "<p> 페이징 없는 버전이다. </p>"
    )
    @GetMapping("/{category}/all")
    public ResponseEntity<ApiResponse<List<LeaderboardResponseDto>>> getUsersByCategory(
            @PathVariable String category,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return CustomResponseEntity.of(
                SuccessCode._OK,
                leaderboardService.getAllLeaderboard(category, date)
        );
    }

    @Operation(
            summary = "유저의 점수 조회 (일간)",
            description =
                "<p> 유저의 카테고리 별 점수와 등수를 조회한다. </p>" +
                "<p> 날짜를 입력하지 않는 경우, 오늘을 기준으로 한다. </p>" +
                "<p> 랭크가 0인 경우, 점수가 없다는 뜻이다. </p>"

    )
    @GetMapping("/{category}/user-info/daily")
    public ResponseEntity<ApiResponse<LeaderboardResponseDto>> getUserInfoDaily(
            @PathVariable String category,
            @RequestParam String userId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return CustomResponseEntity.of(
                SuccessCode._OK,
                leaderboardService.getUserScoreInfo(category, userId, date, PeriodType.DAILY)
        );
    }

    @Operation(
            summary = "유저의 점수 조회 (주간)",
            description =
                    "<p> 유저의 카테고리 별 점수와 등수를 조회한다. </p>" +
                    "<p> 날짜를 입력하지 않는 경우, 오늘을 기준으로 한다. </p>" +
                    "<p> 랭크가 0인 경우, 점수가 없다는 뜻이다. </p>"

    )
    @GetMapping("/{category}/user-info/weekly")
    public ResponseEntity<ApiResponse<LeaderboardResponseDto>> getUserInfoWeekly(
            @PathVariable String category,
            @RequestParam String userId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return CustomResponseEntity.of(
                SuccessCode._OK,
                leaderboardService.getUserScoreInfo(category, userId, date, PeriodType.WEEKLY)
        );
    }

    @Operation(
            summary = "유저의 점수 조회 (월간)",
            description =
                    "<p> 유저의 카테고리 별 점수와 등수를 조회한다. </p>" +
                    "<p> 날짜를 입력하지 않는 경우, 오늘을 기준으로 한다. </p>" +
                    "<p> 랭크가 0인 경우, 점수가 없다는 뜻이다. </p>"

    )
    @GetMapping("/{category}/user-info/monthly")
    public ResponseEntity<ApiResponse<LeaderboardResponseDto>> getUserInfoMonthly(
            @PathVariable String category,
            @RequestParam String userId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return CustomResponseEntity.of(
                SuccessCode._OK,
                leaderboardService.getUserScoreInfo(category, userId, date, PeriodType.MONTHLY)
        );
    }

    @Operation(
            summary = "전체 카테고리 10등까지 조회",
            description =
                "<p> 전체 카테고리의 1등부터 10등까지 조회한다. </p>" +
                "<p> 일단 당일만 조회 가능하다. </p>"
    )
    @GetMapping("/top-users")
    public ResponseEntity<ApiResponse<Map<String, List<LeaderboardResponseDto>>>> getLeaderboards() {
        return CustomResponseEntity.of(
                SuccessCode._OK,
                leaderboardService.getLeaderboards()
        );
    }
}
