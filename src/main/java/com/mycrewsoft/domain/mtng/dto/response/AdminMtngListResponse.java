package com.mycrewsoft.domain.mtng.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminMtngListResponse {

    @Schema(description = "회의ID")
    private Long mtngId;

    @Schema(description = "회의명")
    private String mtngNm;

    @Schema(description = "회의 진행방식 코드 (01: 온라인 / 02: 오프라인 / 03: 복합)")
    private String mtngTypeCd;

    @Schema(description = "시작일시")
    private LocalDateTime beginDt;

    @Schema(description = "종료일시")
    private LocalDateTime endDt;

    @Schema(description = "작성자ID")
    private Long crtrId;

    @Schema(description = "작성자명")
    private String crtrNm;

    @Schema(description = "화상회의 상태 코드 (VC001: 예정 / VC002: 진행중 / VC003: 완료), 오프라인이면 NULL")
    private String vconfSttus;

    @Schema(description = "회의실명, 온라인이면 NULL")
    private String confRmNm;

    @Schema(description = "회의록 상태 코드 (MM001: 생성중 / MM002: 수정중 / MM003: 결재중 / MM004: 승인됨), 없으면 NULL")
    private String momSttusCd;

    @Schema(description = "참여자 수")
    private Integer ptcptCnt;

    @Schema(description = "삭제여부 (Y/N)")
    private String delYn;
}