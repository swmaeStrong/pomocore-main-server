package com.swmStrong.demo.domain.common.util;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class TimeZoneUtil {
    
    public static final ZoneId UTC = ZoneId.of("UTC");
    public static final ZoneId KOREA_TIMEZONE = ZoneId.of("Asia/Seoul");
    
    public static LocalDateTime convertUnixToLocalDateTime(double unixTimestamp, ZoneId targetZone) {
        return LocalDateTime.ofInstant(
            Instant.ofEpochSecond((long) unixTimestamp),
            targetZone != null ? targetZone : KOREA_TIMEZONE
        );
    }
    
    public static LocalDate convertUnixToLocalDate(double unixTimestamp, ZoneId targetZone) {
        return convertUnixToLocalDateTime(unixTimestamp, targetZone).toLocalDate();
    }
    
    public static long convertLocalDateTimeToUnix(LocalDateTime dateTime, ZoneId sourceZone) {
        return dateTime.atZone(sourceZone != null ? sourceZone : KOREA_TIMEZONE)
                .toInstant()
                .getEpochSecond();
    }
    
    public static ZoneId parseTimezone(String timezone) {
        if (timezone == null || timezone.isEmpty()) {
            return KOREA_TIMEZONE;
        }
        try {
            return ZoneId.of(timezone);
        } catch (DateTimeException e) {
            return KOREA_TIMEZONE;
        }
    }
    
    public static LocalDateTime nowInTimezone(ZoneId zone) {
        return LocalDateTime.now(zone != null ? zone : KOREA_TIMEZONE);
    }
    
    public static LocalDate todayInTimezone(ZoneId zone) {
        return LocalDate.now(zone != null ? zone : KOREA_TIMEZONE);
    }
}