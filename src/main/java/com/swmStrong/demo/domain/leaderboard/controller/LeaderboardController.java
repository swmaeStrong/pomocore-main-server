package com.swmStrong.demo.domain.leaderboard.controller;

import com.swmStrong.demo.domain.leaderboard.dto.LeaderboardResponseDto;
import com.swmStrong.demo.domain.leaderboard.service.LeaderboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Tag(name = "리더보드")
@RestController
@RequestMapping("/leaderboard")
public class LeaderboardController {
    private final LeaderboardService leaderboardService;

    public LeaderboardController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @Operation(
            summary = "리더보드 조회",
            description =
                "<p> 카테고리별로 N등 까지의 유저와 점수, 순위를 출력한다. </p>" +
                "<p> page 의 기본값은 1이다. </p>" +
                "<p> 날짜를 입력하지 않는 경우, 오늘을 기준으로 한다. </p>"
    )
    @GetMapping("/{category}")
    public ResponseEntity<List<LeaderboardResponseDto>> getTopUsersByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
            ) {
        return ResponseEntity.ok(leaderboardService.getLeaderboardPage(category, page, size, date));
    }

    @Operation(
            summary = "유저의 점수 조회",
            description =
                "<p> 유저의 카테고리 별 점수와 등수를 조회한다. </p>" +
                "<p> 날짜를 입력하지 않는 경우, 오늘을 기준으로 한다. </p>"
    )
    @GetMapping("/{category}/user-info")
    public ResponseEntity<LeaderboardResponseDto> getUserInfo(
            @PathVariable String category,
            @RequestParam String userId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return ResponseEntity.ok(leaderboardService.getUserScoreInfo(category, userId, date));
    }
}
