package com.mycrewsoft.domain.messenger.enums;

import java.util.Arrays;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ParticipantStatus {

    ONLINE("STS1"),
    AWAY("STS2"),
    BUSY("STS3"),
    OFFLINE("STS4");

    private final String code;

    public static ParticipantStatus fromCode(String code) {
        String normalizedCode = code == null ? null : code.trim();

        return Arrays.stream(values())
                .filter(status -> status.code.equals(normalizedCode))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INPUT_VALUE));
    }

    public static ParticipantStatus fromCodeOrDefault(
            String code,
            ParticipantStatus defaultStatus
    ) {
        return code == null || code.isBlank() ? defaultStatus : fromCode(code);
    }
}
