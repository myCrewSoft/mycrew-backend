package com.mycrewsoft.common.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

/**
 * 날짜/시간 관련 유틸리티 클래스.
 * 팀 전체에서 사용하는 날짜 포맷을 한 곳에서 통일한다.
 * 포맷 상수 외의 형식으로 날짜를 처리하는 것을 금지한다.
 * 모든 메서드는 static 이며, 인스턴스 생성을 금지한다.
 */
public final class DateUtil {

    private DateUtil() {}

    /** API 통신 날짜 포맷: 2024-05-07 */
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /** API 통신 일시 포맷: 2024-05-07 14:30:00 */
    public static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 화면 표시용 날짜 포맷: 2024년 05월 07일 */
    public static final DateTimeFormatter DISPLAY_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");

    /** 화면 표시용 일시 포맷: 2024년 05월 07일 14시 30분 */
    public static final DateTimeFormatter DISPLAY_DATETIME_FORMAT = DateTimeFormatter
            .ofPattern("yyyy년 MM월 dd일 HH시 mm분");

    /** 파일명 생성용 포맷: 20240507_143000 */
    public static final DateTimeFormatter FILE_NAME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    /** 현재 날짜 반환 */
    public static LocalDate today() {
        return LocalDate.now();
    }

    /** 현재 날짜와 시간 반환 */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    /** LocalDate → "2024-05-07" */
    public static String format(LocalDate date) {
        if (date == null) return StringUtils.EMPTY;
        
        return date.format(DATE_FORMAT);
    }

    /** LocalDateTime → "2024-05-07 14:30:00" */
    public static String format(LocalDateTime dateTime) {
        if (dateTime == null) return StringUtils.EMPTY;

        return dateTime.format(DATETIME_FORMAT);
    }
    
    /** LocalDate → "2024년 05월 07일" */
    public static String formatDisplay(LocalDate date) {
        if (date == null) return StringUtils.EMPTY;

        return date.format(DISPLAY_DATE_FORMAT);
    }

    /** LocalDateTime → "2024년 05월 07일 14시 30분" */
    public static String formatDisplay(LocalDateTime dateTime) {
        if (dateTime == null) return StringUtils.EMPTY;

        return dateTime.format(DISPLAY_DATETIME_FORMAT);
    }

    /** LocalDateTime → "20240507_143000" (파일명용) */
    public static String formatForFileName(LocalDateTime dateTime) {
        if (dateTime == null) return StringUtils.EMPTY;

        return dateTime.format(FILE_NAME_FORMAT);
    }

    /** "2024-05-07" → LocalDate. 형식 오류 시 null 반환. */
    public static LocalDate parseDate(String dateStr) {
        if (StringUtils.isBlank(dateStr)) return null;

        try {
            return LocalDate.parse(dateStr, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** "2024-05-07 14:30:00" → LocalDateTime. 형식 오류 시 null 반환. */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        if (StringUtils.isBlank(dateTimeStr)) return null;

        try {
            return LocalDateTime.parse(dateTimeStr, DATETIME_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** 두 날짜 사이의 일수 차이를 계산한다. */
    public static long daysBetween(LocalDate start, LocalDate end) {
        return ChronoUnit.DAYS.between(start, end);
    }

    /** 생년월일 기준 만 나이를 계산한다. */
    public static int calculateAge(LocalDate birthDate) {
        if (birthDate == null) return 0;

        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    /**
     * 초(seconds) 단위를 "HH:mm:ss" 형식으로 변환한다.
     * 예) 3661초 → "01:01:01"
     */
    public static String formatDuration(long seconds) {
        return DurationFormatUtils.formatDuration(seconds * 1000, "HH:mm:ss");
    }

    /**
     * SNS 스타일 상대적 시간 표현으로 변환한다.
     * 10초 미만 → 방금 전 / N초 전 / N분 전 / N시간 전 / N일 전 / 절대 날짜
     */
    public static String timeAgo(LocalDateTime dateTime) {
        if (dateTime == null) return StringUtils.EMPTY;
        long seconds = ChronoUnit.SECONDS.between(dateTime, LocalDateTime.now());
        if (seconds < 10) return "방금 전";
        if (seconds < 60) return seconds + "초 전";
        long minutes = seconds / 60;
        if (minutes < 60) return minutes + "분 전";
        long hours = minutes / 60;
        if (hours < 24) return hours + "시간 전";
        long days = hours / 24;
        if (days < 7) return days + "일 전";

        return format(dateTime.toLocalDate());
    }
    
    /**
     * D-day 계산
     * @param targetDate
     * @return
     */
    public static String dDay(LocalDate targetDate) {
        if (targetDate == null) return StringUtils.EMPTY;
        long days = ChronoUnit.DAYS.between(LocalDate.now(), targetDate);
        if (days == 0) return "D-Day";
        if (days > 0) return "D-" + days;
        return "D+" + Math.abs(days);
    }
}