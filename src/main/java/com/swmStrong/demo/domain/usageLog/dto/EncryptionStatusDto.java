package com.swmStrong.demo.domain.usageLog.dto;

public record EncryptionStatusDto(
    String status,
    String message
) {
    public static EncryptionStatusDto started() {
        return new EncryptionStatusDto("STARTED", "Encryption process has been started in the background");
    }
}