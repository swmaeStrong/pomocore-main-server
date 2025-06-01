package com.swmStrong.demo.domain.usageLog.controller;

import com.swmStrong.demo.config.security.principal.SecurityPrincipal;
import com.swmStrong.demo.domain.usageLog.dto.CategoryUsageDto;
import com.swmStrong.demo.domain.usageLog.dto.SaveUsageLogDto;
import com.swmStrong.demo.domain.usageLog.dto.UsageLogResponseDto;
import com.swmStrong.demo.domain.usageLog.service.UsageLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;


@Tag(name = "사용 로그")
@RestController
@RequestMapping("/usage-log")
public class UsageLogController {
    private final UsageLogService usageLogService;

    public UsageLogController(UsageLogService usageLogService) {
        this.usageLogService = usageLogService;
    }

    @Operation(
            security = @SecurityRequirement(name = "bearerAuth"),
            summary = "유저 사용 로그 저장",
            description =
                "<p> 유저의 사용 로그를 저장한다. </p>"+
                "<p> 배열 안에 json이 있는 형태로 보내면 된다.</p>"
    )
    @PostMapping
    public ResponseEntity<Void> saveUsageLog(
            @AuthenticationPrincipal SecurityPrincipal securityPrincipal,
            @RequestBody List<SaveUsageLogDto> usageLogDtoList
    ) {
        usageLogService.saveAll(securityPrincipal.getUserId(), usageLogDtoList);
        return ResponseEntity.ok().build();
    }

    @Operation(
            security = @SecurityRequirement(name = "bearerAuth"),
            summary = "유저 사용 로그 조회 (전체)",
            description =
                "<p> 유저의 전체 사용 로그를 조회한다. </p>" +
                "<p> 정리되지 않은 로우 데이터를 반환한다. </p>"
    )
    @GetMapping("/all")
    public ResponseEntity<List<UsageLogResponseDto>> getUsageLogById(
            @AuthenticationPrincipal SecurityPrincipal securityPrincipal
    ) {
        return ResponseEntity.ok(usageLogService.getUsageLogByUserId(securityPrincipal.getUserId()));
    }



    @Operation(
            summary = "날짜별 유저 사용 로그 조회",
            description =
                "<p> 유저의 당일 사용 로그를 조회한다. </p>" +
                "<p> 정리가 되어 있다. </p>" +
                "<p> 날짜를 입력하지 않으면 당일 날짜로 들어간다. </p>"
    )
    @GetMapping
    public ResponseEntity<List<CategoryUsageDto>> getUsageLogByIdToday(
            @RequestParam
            String userId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return ResponseEntity.ok(usageLogService.getUsageLogByUserIdAndDate(userId, date));
    }
}
