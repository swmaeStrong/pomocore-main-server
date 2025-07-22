package com.swmStrong.demo.domain.sessionScore.controller;

import com.swmStrong.demo.common.exception.code.SuccessCode;
import com.swmStrong.demo.common.response.ApiResponse;
import com.swmStrong.demo.common.response.CustomResponseEntity;
import com.swmStrong.demo.config.security.principal.SecurityPrincipal;
import com.swmStrong.demo.domain.sessionScore.dto.SessionScoreResponseDto;
import com.swmStrong.demo.domain.sessionScore.service.SessionScoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RequestMapping("/session")
@RestController
public class SessionScoreController {

    private final SessionScoreService sessionScoreService;
    public SessionScoreController(SessionScoreService sessionScoreService) {
        this.sessionScoreService = sessionScoreService;
    }

    @Operation(
            security = @SecurityRequirement(name = "bearerAuth"),
            summary = "유저 통계 정보 확인",
            description =
                    "<p> 일단 기본정보 서빙 </p>"
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<SessionScoreResponseDto>>> get(
            @AuthenticationPrincipal SecurityPrincipal principal,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDate date
    ) {
        return CustomResponseEntity.of(
                SuccessCode._OK,
                sessionScoreService.get(principal.userId(), date)
        );
    }
}
