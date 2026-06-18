package com.mycrewsoft.domain.mtng.vo;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// TB_MTNG을 TB_MTNG_PTCPT, TB_VIDEO_CONF 등과 JOIN한 목록 조회 결과
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MtngListVO {

    private Long mtngId; // 회의ID

    private String mtngNm; // 회의명

    private String mtngTypeCd; // 회의진행방식코드

    private LocalDateTime beginDt; // 시작일시

    private LocalDateTime endDt; // 종료일시

    private Long crtrId; // 작성자ID

    private String crtrNm; // 작성자명 (직원 테이블 JOIN)

    private String vconfSttus; // 화상회의 상태코드 (TB_VIDEO_CONF JOIN, 오프라인이면 NULL)

    private Long vconfId;      // 화상회의ID (오프라인이면 NULL)

    private String roomNm;     // LiveKit room명 (오프라인이면 NULL)

    private String confRmNm;   // 회의실명 (TB_CONF_RM JOIN, 온라인이면 NULL)

    private String momSttusCd; // 회의록 상태코드 (TB_MTNG_MOM JOIN, 없으면 NULL)

    private Integer ptcptCnt;  // 참여자 수 (TB_MTNG_PTCPT COUNT)

    private String delYn;      // 삭제여부 (관리자 목록에서 사용)
}