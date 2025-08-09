package com.swmStrong.demo.domain.group.dto;

import java.util.List;

public record EmailsRequestDto(
        List<String> emails
) {
}