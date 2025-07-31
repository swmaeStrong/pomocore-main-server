package com.swmStrong.demo.domain.sessionScore.controller;

import com.swmStrong.demo.common.exception.code.SuccessCode;
import com.swmStrong.demo.common.response.ApiResponse;
import com.swmStrong.demo.common.response.CustomResponseEntity;
import com.swmStrong.demo.config.security.principal.SecurityPrincipal;
import com.swmStrong.demo.domain.sessionScore.dto.SessionDashboardDto;
import com.swmStrong.demo.domain.sessionScore.dto.SessionScoreResponseDto;
import com.swmStrong.demo.domain.sessionScore.service.SessionScoreService;
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

@Tag(name = "세션 점수")
@RequestMapping("/session")
@RestController
public class SessionScoreController {

    private final SessionScoreService sessionScoreService;
    public SessionScoreController(SessionScoreService sessionScoreService) {
        this.sessionScoreService = sessionScoreService;
    }

    @Operation(
            security = @SecurityRequirement(name = "bearerAuth"),
            summary = "유저 세션 정보 확인",
            description =
                    "<p> 해당 일자의 세션 정보를 확인한다. </p>" +
                    "<p> 제공되는 정보의 경우 usage-log/pomodoro에서 isEnd 플래그가 true인 경우 해당 로그가 자동 생성된다. </p>" +
                    "<p> title의 경우 현재 무조건 빈값으로 나가나, 이후 llm으로 생성할 수 있다. </p>" +
                    "<p> 지금 당장은 점수 계산이 문제 있을 수 있으니 피드백 ㄱㄱ </p>"
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<SessionScoreResponseDto>>> getByUserIdAndSessionDate(
            @AuthenticationPrincipal SecurityPrincipal principal,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDate date
    ) {
        return CustomResponseEntity.of(
                SuccessCode._OK,
                sessionScoreService.getByUserIdAndSessionDate(principal.userId(), date)
        );
    }

    @Operation(
            security = @SecurityRequirement(name = "bearerAuth"),
            summary = "유저 세션 정보 확인 (분석용)",
            description =
                    "<p> 해당 일자의 세션 정보를 확인한다. </p>" +
                    "<p> 해당 일자의 점수만 나열하고, 상세 정보는 이 정보를 쥐고 있다가 usage-log/pomodoro/details 쪽에 요청을 보내면서 합친다. </p>"
    )
    @GetMapping("/score")
    public ResponseEntity<ApiResponse<List<SessionDashboardDto>>> getScoreByUserIdAndSessionDate(
            @AuthenticationPrincipal SecurityPrincipal principal,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDate date
    ) {
        return CustomResponseEntity.of(
                SuccessCode._OK,
                sessionScoreService.getScoreByUserIdAndSessionDate(principal.userId(), date)
        );
    }
}
