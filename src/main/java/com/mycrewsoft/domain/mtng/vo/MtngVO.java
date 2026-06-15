package com.mycrewsoft.domain.mtng.vo;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
public class MtngVO {

    @Setter
    private Long mtngId; // 회의ID

    private String mtngNm; // 회의명

    private String mtngTypeCd; // 회의진행방식코드

    private Long crtrId; // 작성자ID

    private LocalDateTime beginDt; // 시작일시

    private LocalDateTime endDt; // 종료일시

    @Setter
    private Long confRmRsrvId; // 회의실예약ID

    private LocalDateTime creatDt; // 생성일시

    private String delYn; // 삭제여부
}