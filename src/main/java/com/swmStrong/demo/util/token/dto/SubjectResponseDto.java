package com.swmStrong.demo.util.token.dto;

public record SubjectResponseDto(
        String supabaseId,
        String email
) {
    public static SubjectResponseDto of(String supabaseId, String email) {
        return new SubjectResponseDto(supabaseId, email);
    }
}
