package com.swmStrong.demo.domain.usageLog.controller;

import com.swmStrong.demo.domain.usageLog.dto.SaveUsageLogDto;
import com.swmStrong.demo.domain.usageLog.dto.UsageLogResponseDto;
import com.swmStrong.demo.domain.usageLog.service.UsageLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usage-log")
public class UsageLogController {
    private final UsageLogService usageLogService;

    public UsageLogController(UsageLogService usageLogService) {
        this.usageLogService = usageLogService;
    }

    @PostMapping
    public ResponseEntity<Void> saveUsageLog(@RequestBody List<SaveUsageLogDto> usageLogDtoList) {
        usageLogService.saveAll(usageLogDtoList);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<UsageLogResponseDto>> getUsageLogById(@RequestParam String userId) {
        return ResponseEntity.ok(usageLogService.getUsageLogByUserId(userId));
    }
}
