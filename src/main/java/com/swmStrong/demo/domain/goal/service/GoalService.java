package com.swmStrong.demo.domain.goal.service;

import com.swmStrong.demo.domain.goal.dto.GoalResponseDto;
import com.swmStrong.demo.domain.goal.dto.SaveUserGoalDto;

import java.time.LocalDate;
import java.util.List;

public interface GoalService {
    void saveUserGoal(String userId, SaveUserGoalDto saveUserGoalDto);
    List<GoalResponseDto> getUserGoals(String userId, LocalDate date, String periodType);
}
