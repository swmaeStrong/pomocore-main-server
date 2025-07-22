package com.swmStrong.demo.domain.sessionScore.service;

import com.swmStrong.demo.domain.sessionScore.dto.SessionScoreResponseDto;

import java.time.LocalDate;
import java.util.List;

public interface SessionScoreService {
    List<SessionScoreResponseDto> get(String userId, LocalDate date);
}
