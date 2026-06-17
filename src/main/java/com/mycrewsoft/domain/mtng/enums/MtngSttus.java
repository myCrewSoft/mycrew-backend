package com.mycrewsoft.domain.mtng.enums;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// TB_MTNG.BEGIN_DT/END_DT와 현재 시각을 비교해 산출하는 상태값
// DB 컬럼이 아니라 응답 시점에 계산되는 값이므로 ErrorCode 검증 대상이 아니다
@Getter
@RequiredArgsConstructor
public enum MtngSttus {

    SCHEDULED("scheduled"), // 시작 전
    LIVE("live"),           // 진행 중
    ENDED("ended");         // 종료

    private final String value;

    public static MtngSttus of(LocalDateTime beginDt, LocalDateTime endDt, LocalDateTime now) {
        if (now.isBefore(beginDt)) {
            return SCHEDULED;
        }
        if (now.isAfter(endDt)) {
            return ENDED;
        }
        return LIVE;
    }
}