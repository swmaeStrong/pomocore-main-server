package com.swmStrong.demo.domain.common.util;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class TimeZoneUtil {
    
    public static final ZoneId UTC = ZoneId.of("UTC");
    public static final ZoneId KOREA_TIMEZONE = ZoneId.of("Asia/Seoul");
    
    public static LocalDateTime convertUnixToLocalDateTime(long unixTimestamp, ZoneId targetZone) {
        return LocalDateTime.ofInstant(
            Instant.ofEpochSecond(unixTimestamp),
            targetZone != null ? targetZone : UTC
        );
    }
    
    public static LocalDate convertUnixToLocalDate(long unixTimestamp, ZoneId targetZone) {
        return convertUnixToLocalDateTime(unixTimestamp, targetZone).toLocalDate();
    }
    
    public static long convertLocalDateTimeToUnix(LocalDateTime dateTime, ZoneId sourceZone) {
        return dateTime.atZone(sourceZone != null ? sourceZone : UTC)
                .toInstant()
                .getEpochSecond();
    }
    
    public static ZoneId parseTimezone(String timezone) {
        if (timezone == null || timezone.isEmpty()) {
            return UTC;
        }
        try {
            return ZoneId.of(timezone);
        } catch (DateTimeException e) {
            return UTC;
        }
    }
    
    public static LocalDateTime nowInTimezone(ZoneId zone) {
        return LocalDateTime.now(zone != null ? zone : UTC);
    }
    
    public static LocalDate todayInTimezone(ZoneId zone) {
        return LocalDate.now(zone != null ? zone : UTC);
    }
}