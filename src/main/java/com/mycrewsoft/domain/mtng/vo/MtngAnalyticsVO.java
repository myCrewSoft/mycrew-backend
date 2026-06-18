package com.mycrewsoft.domain.mtng.vo;

import lombok.Getter;

@Getter
public class MtngAnalyticsVO {

    private String category; // 집계 기준값 (유형코드, 월, 시간대 등 쿼리마다 다르게 매핑)

    private Integer cnt;     // 해당 기준의 회의 건수
}