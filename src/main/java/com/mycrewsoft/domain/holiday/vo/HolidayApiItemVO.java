package com.mycrewsoft.domain.holiday.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class HolidayApiItemVO {

    private String locdate;

    private String dateName;

    private String isHoliday;

    private String seq;
}