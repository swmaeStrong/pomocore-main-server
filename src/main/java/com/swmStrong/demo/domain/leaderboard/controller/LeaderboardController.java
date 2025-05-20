package com.swmStrong.demo.domain.leaderboard.controller;

import com.swmStrong.demo.domain.leaderboard.dto.LeaderboardResponseDto;
import com.swmStrong.demo.domain.leaderboard.service.LeaderboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/leaderboard")
public class LeaderboardController {
    private final LeaderboardService leaderboardService;

    public LeaderboardController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @GetMapping("/{category}")
    public ResponseEntity<List<LeaderboardResponseDto>> getTopUsersByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "10") int top
    ) {
        return ResponseEntity.ok(leaderboardService.getTopUsers(category, top));
    }

    @GetMapping("/{category}/user/{userId}")
    public ResponseEntity<Optional<LeaderboardResponseDto>> getUserByCategory(
            @PathVariable String category,
            @PathVariable String userId
    ) {
        return ResponseEntity.ok(leaderboardService.getUserScoreInfo(category, userId));
    }
}
