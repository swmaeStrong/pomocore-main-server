package com.swmStrong.demo.domain.user.service;

import com.swmStrong.demo.domain.user.dto.OnBoardRequestDto;

public interface OnBoardService {
    void save(String userId, OnBoardRequestDto onBoardRequestDto);
}
