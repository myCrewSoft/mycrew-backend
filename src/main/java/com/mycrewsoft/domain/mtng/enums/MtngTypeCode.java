package com.mycrewsoft.domain.mtng.enums;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MtngTypeCode {

    ONLINE("01", "온라인"),
    OFFLINE("02", "오프라인"),
    HYBRID("03", "혼합");

    private final String code;
    private final String label;

    public static MtngTypeCode fromCode(String code) {
        for (MtngTypeCode value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        throw new CustomException(ErrorCode.INVALID_MTNG_TYPE_CD);
    }
}