package com.mycrewsoft.domain.mtng.vo;

import java.time.LocalDateTime;
import lombok.Getter;

// TB_MTNG을 TB_MTNG_PTCPT, TB_VIDEO_CONF 등과 JOIN한 목록 조회 결과
@Getter
public class MtngListVO {

    private Long mtngId; // 회의ID

    private String mtngNm; // 회의명

    private String mtngTypeCd; // 회의진행방식코드

    private LocalDateTime beginDt; // 시작일시

    private LocalDateTime endDt; // 종료일시

    private Long crtrId; // 작성자ID

    private String crtrNm; // 작성자명 (직원 테이블 JOIN)

    private Long vconfId; // 화상회의ID, 오프라인이면 NULL

    private String roomNm; // LiveKit room명, 오프라인이면 NULL

    private String confRmNm; // 회의실명, 미사용 시 null

    private String momSttusCd; // 회의록 상태코드, 회의록 없으면 NULL

    private Integer ptcptCnt; // 참여자 수 (COUNT)
}