package com.swmStrong.demo.domain.usageLog.controller;

import com.swmStrong.demo.domain.usageLog.dto.CategoryUsageDto;
import com.swmStrong.demo.domain.usageLog.dto.SaveUsageLogDto;
import com.swmStrong.demo.domain.usageLog.dto.UsageLogResponseDto;
import com.swmStrong.demo.domain.usageLog.service.UsageLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            summary = "유저 사용 로그 저장",
            description =
                "<p> 유저의 사용 로그를 저장한다. </p>"+
                "<p> 배열 안에 json이 있는 형태로 보내면 된다.</p>"
    )
    @PostMapping
    public ResponseEntity<Void> saveUsageLog(@RequestBody List<SaveUsageLogDto> usageLogDtoList) {
        usageLogService.saveAll(usageLogDtoList);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "유저 사용 로그 조회",
            description =
                "<p> 유저의 전체 사용 로그를 조회한다. </p>" +
                "<p> 정리되지 않은 로우 데이터를 반환한다. </p>"
    )
    @GetMapping
    public ResponseEntity<List<UsageLogResponseDto>> getUsageLogById(@RequestParam String userId) {
        return ResponseEntity.ok(usageLogService.getUsageLogByUserId(userId));
    }

    @Operation(
            summary = "유저 사용 로그 조회(당일)",
            description =
                "<p> 유저의 당일 사용 로그를 조회한다. </p>" +
                "<p> 정리가 되어 있다. </p>"
    )
    @GetMapping("/today")
    public ResponseEntity<List<CategoryUsageDto>> getUsageLogByIdToday(@RequestParam String userId) {
        return ResponseEntity.ok(usageLogService.getUsageLogByUserIdToday(userId));
    }
}
