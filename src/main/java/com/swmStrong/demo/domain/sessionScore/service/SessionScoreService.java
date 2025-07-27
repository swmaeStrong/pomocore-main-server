package com.swmStrong.demo.domain.sessionScore.service;

import com.swmStrong.demo.domain.sessionScore.dto.SessionDashboardDto;
import com.swmStrong.demo.domain.sessionScore.dto.SessionScoreResponseDto;

import java.time.LocalDate;
import java.util.List;

public interface SessionScoreService {
    List<SessionScoreResponseDto> getByUserIdAndSessionDate(String userId, LocalDate date);
    List<SessionDashboardDto> getScoreByUserIdAndSessionDate(String userId, LocalDate date);
}
