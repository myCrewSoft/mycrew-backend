package com.mycrewsoft.domain.mtng.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MtngListResponse {

    @Schema(description = "회의ID")
    private Long mtngId;

    @Schema(description = "회의명")
    private String mtngNm;

    @Schema(description = "회의 진행방식 코드 (MT01:온라인, MT02:오프라인, MT03:혼합)")
    private String mtngTypeCd;

    @Schema(description = "시작일시")
    private LocalDateTime beginDt;

    @Schema(description = "종료일시")
    private LocalDateTime endDt;

    @Schema(description = "작성자명")
    private String crtrNm;

    @Schema(description = "화상회의ID, 오프라인이면 null", nullable = true)
    private Long vconfId;

    @Schema(description = "LiveKit room명, 오프라인이면 null", nullable = true)
    private String roomNm;

    @Schema(description = "회의실명, 미사용 시 null", nullable = true)
    private String confRmNm;

    @Schema(description = "회의록 상태코드, 없으면 null", nullable = true)
    private String momSttusCd;

    @Schema(description = "참여자 수")
    private Integer ptcptCnt;

    @Schema(description = "회의 상태 (scheduled/live/ended), beginDt-endDt 기준 계산값")
    private String mtngSttus;

    @Schema(description = "작성자ID (수정/삭제 권한 판단용)")
    private Long crtrId;

    @Schema(description = "사용 중인 회의실ID, 미사용 시 null", nullable = true)
    private Long confRmId;

    @Schema(description = "수정 가능 여부 (시작 전 + 본인인 경우 true)")
    private Boolean canEdit;

    @Schema(description = "삭제 가능 여부 (시작 전 + 본인인 경우 true)")
    private Boolean canDelete;

    @Schema(description = "종료 가능 여부 (진행중 + 본인인 경우 true)")
    private Boolean canEnd;
}