package com.swmStrong.demo.domain.pomodoro.controller;

import com.swmStrong.demo.common.exception.code.SuccessCode;
import com.swmStrong.demo.common.response.ApiResponse;
import com.swmStrong.demo.common.response.CustomResponseEntity;
import com.swmStrong.demo.config.security.principal.SecurityPrincipal;
import com.swmStrong.demo.domain.pomodoro.dto.PomodoroUsageLogsDto;
import com.swmStrong.demo.domain.pomodoro.service.PomodoroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
