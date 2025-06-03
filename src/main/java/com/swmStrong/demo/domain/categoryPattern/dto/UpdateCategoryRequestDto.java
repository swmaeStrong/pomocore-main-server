package com.swmStrong.demo.domain.categoryPattern.dto;

import com.swmStrong.demo.common.annotation.HexColor;

public record UpdateCategoryRequestDto(
        String category,
        @HexColor
        String color
) {
}
