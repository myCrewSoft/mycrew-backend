package com.mycrewsoft.domain.mtng.vo;

import lombok.Getter;

// 참여자 상세 조회용 JOIN VO: TB_MTNG_PTCPT + 직원/부서/직급 정보
@Getter
public class MtngPtcptDetailVO {

    private Long mtngPtcptId; // 회의참여자ID

    private Long empId; // 직원ID

    private String empNm; // 직원명

    private String deptNm; // 부서명

    private String jobGrdNm; // 직급명

    private String prflImgFileId; // 프로필 이미지 URL
}