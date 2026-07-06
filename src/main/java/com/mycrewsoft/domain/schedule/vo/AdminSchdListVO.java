package com.mycrewsoft.domain.schedule.vo;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminSchdListVO {

    private Long schdId;

    private String schdClsfCd;

    private String schdNm;

    private LocalDateTime beginDt;

    private LocalDateTime endDt;

    private String allDayYn;

    private String reptYn;

    private Long schdWrtrId;

    private String wrtrNm;

    private String wrtrDeptNm;

    private LocalDateTime schdRegstDt;
}