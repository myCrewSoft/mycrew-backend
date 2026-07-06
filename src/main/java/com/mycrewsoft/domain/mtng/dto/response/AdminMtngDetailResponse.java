package com.mycrewsoft.domain.mtng.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class AdminMtngDetailResponse {

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

    @Schema(description = "회의실예약ID, 온라인이면 NULL")
    private Long confRmRsrvId;

    @Schema(description = "회의실명, 온라인이면 NULL")
    private String confRmNm;

    @Schema(description = "화상회의ID, 오프라인이면 NULL")
    private Long vconfId;

    @Schema(description = "LiveKit room명, 오프라인이면 NULL")
    private String roomNm;

    @Schema(description = "화상회의 상태 코드, 오프라인이면 NULL")
    private String vconfSttus;

    @Schema(description = "회의록ID, 없으면 NULL")
    private Long momId;

    @Schema(description = "회의록 상태 코드, 없으면 NULL")
    private String momSttusCd;

    @Schema(description = "녹취록 첨부파일ID, 없으면 NULL")
    private Long rcrdgAtchFileId;

    @Schema(description = "녹취록 생성일시, 없으면 NULL")
    private LocalDateTime rcrdgCreatDt;

    @Schema(description = "삭제여부 (Y/N)")
    private String delYn;

    @Schema(description = "참여자 목록")
    private List<AdminMtngPtcptResponse> ptcpts;
}