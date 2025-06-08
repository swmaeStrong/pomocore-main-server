package com.swmStrong.demo.domain.user.dto;

import jakarta.validation.constraints.Pattern;

public record NicknameRequestDto(
        @Pattern(
                regexp = "^[a-zA-Z가-힣0-9]{2,10}$",
                message = "닉네임은 2~10자 이내의 한글, 영어, 숫자만을 사용해야 합니다."
        )
        String nickname
) {
}
