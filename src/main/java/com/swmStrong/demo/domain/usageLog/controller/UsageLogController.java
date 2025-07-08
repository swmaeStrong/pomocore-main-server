package com.swmStrong.demo.domain.usageLog.controller;

import com.swmStrong.demo.common.exception.code.SuccessCode;
import com.swmStrong.demo.common.response.ApiResponse;
import com.swmStrong.demo.common.response.CustomResponseEntity;
import com.swmStrong.demo.config.security.principal.SecurityPrincipal;
import com.swmStrong.demo.domain.usageLog.dto.*;
import com.swmStrong.demo.domain.usageLog.service.UsageLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    public ResponseEntity<ApiResponse<Void>> saveUsageLog(
            @AuthenticationPrincipal SecurityPrincipal securityPrincipal,
            @RequestBody List<SaveUsageLogDto> usageLogDtoList
    ) {
        usageLogService.saveAll(securityPrincipal.userId(), usageLogDtoList);
        return CustomResponseEntity.of(SuccessCode._OK);
    }

    @Operation(
            security = @SecurityRequirement(name = "bearerAuth"),
            summary = "최근 유저 사용 로그 조회",
            description =
                "<p> 유저의 전체 사용 로그를 조회한다. </p>" +
                "<p> 현재는 전체를 반환하고 있다. </p>" +
                "<p> 상세한 데이터를 포함하고 있기에 인증을 통해 본인 정보만 확인할 수 있게 한다. </p>"
    )
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<CategorizedUsageLogDto>>> getUsageLogById(
            @AuthenticationPrincipal SecurityPrincipal securityPrincipal,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate date
    ) {
        return CustomResponseEntity.of(
                SuccessCode._OK,
                usageLogService.getCategorizedUsageLogByUserId(securityPrincipal.userId(), date)
        );
    }

    @Operation(
            summary = "최근 유저 사용 타임라인 조회",
            description =
                    "<p> 유저의 사용 로그 타임라인을 조회한다. </p>"
    )
    @GetMapping("/time-line")
    public ResponseEntity<ApiResponse<List<MergedCategoryUsageLogDto>>> getUsageTimeLineById(
            @RequestParam String userId,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate date
    ) {
        return CustomResponseEntity.of(
                SuccessCode._OK,
                usageLogService.getMergedCategoryUsageLogByUserId(userId, date)
        );
    }

    @Operation(
            summary = "날짜별 유저 사용 로그 조회",
            description =
                "<p> 유저의 당일 사용 로그를 조회한다. </p>" +
                "<p> 정리가 되어 있다. </p>" +
                "<p> 날짜를 입력하지 않으면 당일 날짜로 들어간다. </p>"
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryUsageDto>>> getUsageLogByIdToday(
            @RequestParam
            String userId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return CustomResponseEntity.of(
                SuccessCode._OK,
                usageLogService.getUsageLogByUserIdAndDate(userId, date)
        );
    }

    @Operation(
            summary = "시간별 유저 사용 로그 조회",
            description =
                "<p> 유저의 시간별 사용 로그를 조회한다. </p>" +
                "<p> 정리가 되어 있다. </p>" +
                "<p> 날짜를 입력하지 않으면 당일 날짜로 들어간다. </p>"
    )
    @GetMapping("/hour")
    public ResponseEntity<ApiResponse<List<CategoryHourlyUsageDto>>> getUsageLogByHour(
            @RequestParam
            String userId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @RequestParam
            Integer binSize
    ) {
        return CustomResponseEntity.of(
                SuccessCode._OK,
                usageLogService.getUsageLogByUserIdAndDateHourly(userId, date, binSize)
        );
    }

    @Operation(
            security = @SecurityRequirement(name = "bearerAuth"),
            summary = "기존 데이터 암호화",
            description = 
                "<p> 기존 암호화되지 않은 데이터를 암호화하는 일회성 마이그레이션 엔드포인트입니다. </p>" +
                "<p> 한 번만 실행하면 됩니다. </p>" +
                "<p> 암호화 프로세스는 백그라운드에서 실행됩니다. </p>"
    )
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/encrypt-existing-data")
    public ResponseEntity<ApiResponse<EncryptionStatusDto>> encryptExistingData() {
        usageLogService.encryptExistingData();
        return CustomResponseEntity.of(SuccessCode._OK, EncryptionStatusDto.started());
    }
}
