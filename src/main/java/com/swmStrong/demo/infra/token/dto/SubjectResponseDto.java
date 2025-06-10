package com.swmStrong.demo.infra.token.dto;

public record SubjectResponseDto(
        String supabaseId,
        String email
) {
    public static SubjectResponseDto of(String supabaseId, String email) {
        return new SubjectResponseDto(supabaseId, email);
    }
}
