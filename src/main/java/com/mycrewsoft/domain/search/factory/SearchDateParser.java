package com.mycrewsoft.domain.search.factory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/** Oracle CHAR(8) 또는 문자열로 조회된 날짜를 LocalDate로 변환합니다. */
final class SearchDateParser {
    private SearchDateParser() {
    }

    static LocalDate parse(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim();
        try {
            if (normalized.length() >= 10 && normalized.charAt(4) == '-') {
                return LocalDate.parse(normalized.substring(0, 10));
            }
            if (normalized.length() == 8) {
                return LocalDate.parse(normalized, DateTimeFormatter.BASIC_ISO_DATE);
            }
            return LocalDate.parse(normalized, DateTimeFormatter.ofPattern("yyyy.MM.dd"));
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }
}
