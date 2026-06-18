package com.mycrewsoft.domain.holiday.vo;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HolidayVO {

    private Long holidayId;

    private LocalDate holidayDt;

    private String holidayNm;

    private String isHolidayYn;

    private String genTypeCd;

    private LocalDateTime syncDt;

    private String delYn;

    private LocalDateTime frstRegDt;
}