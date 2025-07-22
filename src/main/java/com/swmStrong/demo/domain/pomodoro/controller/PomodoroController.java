package com.swmStrong.demo.domain.pomodoro.controller;

import com.swmStrong.demo.common.exception.code.SuccessCode;
import com.swmStrong.demo.common.response.ApiResponse;
import com.swmStrong.demo.common.response.CustomResponseEntity;
import com.swmStrong.demo.config.security.principal.SecurityPrincipal;
import com.swmStrong.demo.domain.pomodoro.dto.PomodoroResponseDto;
import com.swmStrong.demo.domain.pomodoro.dto.PomodoroUsageLogsDto;
import com.swmStrong.demo.domain.pomodoro.service.PomodoroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "뽀모도로")
@RequestMapping("/usage-log/pomodoro")
@RestController
public class PomodoroController {

    private final PomodoroService pomodoroService;

    public  PomodoroController(PomodoroService pomodoroService) {
        this.pomodoroService = pomodoroService;
    }

    @Operation(
            security = @SecurityRequirement(name = "bearerAuth"),
            summary = "유저 사용 로그 저장",
            description =
                    "<p> 유저의 사용 로그를 저장한다. </p>"
    )
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> save(
            @AuthenticationPrincipal SecurityPrincipal securityPrincipal,
            @RequestBody PomodoroUsageLogsDto pomodoroUsageLogsDto
    ) {
        pomodoroService.save(securityPrincipal.userId(), pomodoroUsageLogsDto);
        return CustomResponseEntity.of(SuccessCode._CREATED);
    }

    @Operation(
            security = @SecurityRequirement(name = "bearerAuth"),
            summary = "유저 통계 정보 확인",
            description =
                    "<p> 일단 기본정보 서빙 </p>"
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<PomodoroResponseDto>>> getPomodoroSessionResult(
            @AuthenticationPrincipal SecurityPrincipal securityPrincipal,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDate date
    ) {
        return CustomResponseEntity.of(
                SuccessCode._OK,
                pomodoroService.getPomodoroSessionResult(securityPrincipal.userId(), date)
        );
    }
}
