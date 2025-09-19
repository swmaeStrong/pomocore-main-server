package com.swmStrong.demo.domain.user.dto;

import lombok.Getter;

import java.util.List;

public record OnBoardRequestDto(
        List<QuestionDto> questions
) {
    @Getter
    public static class QuestionDto {
        private String question;
        private String answer;
    }
}
