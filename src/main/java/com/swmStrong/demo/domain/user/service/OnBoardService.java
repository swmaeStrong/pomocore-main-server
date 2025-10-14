package com.swmStrong.demo.domain.user.service;

import com.swmStrong.demo.domain.user.dto.OnBoardRequestDto;
import com.swmStrong.demo.domain.user.dto.OnBoardResponseDto;
import com.swmStrong.demo.domain.user.dto.OnBoardStatisticsResponseDto;

import java.util.List;

public interface OnBoardService {
    void save(String userId, OnBoardRequestDto onBoardRequestDto);
    List<OnBoardStatisticsResponseDto> getCount();
    public OnBoardResponseDto getUserOnBoard(String userId);
}
