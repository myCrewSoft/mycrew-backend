package com.mycrewsoft.domain.mtng.service;

import com.mycrewsoft.domain.mtng.enums.MtngTypeCode;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class MtngCreateContext {

    private String mtngNm; // 회의명

    private MtngTypeCode mtngTypeCd; // 회의 진행방식 (ONLINE / OFFLINE / HYBRID)

    private Long crtrId; // 작성자(회의 생성자) 직원ID

    private LocalDateTime beginDt; // 시작일시

    private LocalDateTime endDt; // 종료일시

    private Long confRmId; // 사용할 회의실ID, 회의실을 안 쓰면 null

    private String rsrvPurps; // 회의실 예약 목적(회의명을 그대로 써도 됨)

    private List<Long> ptcptEmpIds; // 참여자 직원ID 목록
}