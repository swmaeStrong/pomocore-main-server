package com.swmStrong.demo.domain.user.dto;

import com.swmStrong.demo.domain.user.entity.OnBoard;

import java.util.List;

public record OnBoardResponseDto(
        List<OnBoard.Question> questions
) {
    public static OnBoardResponseDto from(OnBoard onboard) {
        return new OnBoardResponseDto(
                onboard.getQuestions()
        );
    }
}
