package com.swmStrong.demo.domain.user.dto;

import java.util.List;

public record OnBoardStatisticsResponseDto(
    String question,
    List<OnBoardAnswerCountDto> answers
) {
}
