package com.swmStrong.demo.domain.leaderboard.controller;

import com.swmStrong.demo.domain.leaderboard.dto.LeaderboardResponseDto;
import com.swmStrong.demo.domain.leaderboard.service.LeaderboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
                "<p> 입력하지 않은 경우, 기본값은 10이다. </p>"
    )
    @GetMapping("/{category}")
    public ResponseEntity<List<LeaderboardResponseDto>> getTopUsersByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "10") int rank
    ) {
        return ResponseEntity.ok(leaderboardService.getTopUsers(category, rank));
    }

    @Operation(
            summary = "유저의 점수 조회",
            description = "<p> 유저의 카테고리 별 점수를 조회한다. </p>"
    )
    @GetMapping("/{category}/user/{userId}")
    public ResponseEntity<Optional<LeaderboardResponseDto>> getUserByCategory(
            @PathVariable String category,
            @PathVariable String userId
    ) {
        return ResponseEntity.ok(leaderboardService.getUserScoreInfo(category, userId));
    }
}
