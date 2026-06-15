package com.mycrewsoft.domain.mtng.vo;

import java.time.LocalDateTime;
import lombok.Getter;

// 상세 조회용 JOIN VO: TB_MTNG + TB_VIDEO_CONF + TB_MTNG_MOM 상태
@Getter
public class MtngDetailVO {

    private Long mtngId; // 회의ID

    private String mtngNm; // 회의명

    private String mtngTypeCd; // 회의진행방식코드

    private LocalDateTime beginDt; // 시작일시

    private LocalDateTime endDt; // 종료일시

    private Long crtrId; // 작성자ID

    private String crtrNm; // 작성자명

    private Long confRmRsrvId; // 회의실예약ID

    private String confRmNm; // 회의실명 (TB_CONF_RM JOIN)

    private Long vconfId; // 화상회의ID, 오프라인이면 NULL

    private String roomNm; // LiveKit room명, 오프라인이면 NULL

    private Long momId; // 회의록ID, 없으면 NULL

    private String momSttusCd; // 회의록 상태코드

    private Long rcrdgAtchFileId; // 녹취록 첨부파일ID, 없으면 null

    private LocalDateTime rcrdgCreatDt; // 녹취록 생성일시, 없으면 null
}