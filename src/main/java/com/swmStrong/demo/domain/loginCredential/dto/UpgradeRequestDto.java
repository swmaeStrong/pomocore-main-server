package com.swmStrong.demo.domain.loginCredential.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpgradeRequestDto(
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        @NotBlank(message = "이메일은 필수 입력 값입니다.")
        String email,
        @Pattern(
                regexp = "(?=.*[0-9])(?=.*[a-zA-Z])(?=.*\\W)(?=\\S+$).{8,25}",
                message = "비밀번호는 8~25자 영문 대 소문자, 숫자, 특수문자를 사용하세요."
        )
        String password
) {
}
