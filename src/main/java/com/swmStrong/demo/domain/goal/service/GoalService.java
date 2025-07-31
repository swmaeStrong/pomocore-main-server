package com.swmStrong.demo.domain.goal.service;

import com.swmStrong.demo.domain.goal.dto.DeleteUserGoalDto;
import com.swmStrong.demo.domain.goal.dto.GoalResponseDto;
import com.swmStrong.demo.domain.goal.dto.SaveUserGoalDto;

import java.util.List;

public interface GoalService {
    void saveUserGoal(String userId, SaveUserGoalDto saveUserGoalDto);
    List<GoalResponseDto> getCurrentGoals(String userId);
    void deleteUserGoal(String userId, DeleteUserGoalDto deleteUserGoalDto);
}
