package com.mycrewsoft.domain.mtng.dto.mom.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MtngMomResponse {

    @Schema(description = "회의록ID")
    private Long momId;

    @Schema(description = "회의ID")
    private Long mtngId;

    @Schema(description = "회의록 내용")
    private String momCn;

    @Schema(description = "회의록 상태 (01:AI초안, 02:편집중, 03:결재요청, 04:확정)")
    private String momSttusCd;

    @Schema(description = "담당자ID")
    private Long edtrId;

    @Schema(description = "생성일시")
    private LocalDateTime creatDt;

    @Schema(description = "검토요청일시, 결재요청 전 null", nullable = true)
    private LocalDateTime revwReqDt;

    @Schema(description = "확정일시, 확정 전 null", nullable = true)
    private LocalDateTime cnfrmDt;

    @Schema(description = "전자결재 기안문서ID, 결재요청 전 null", nullable = true)
    private Long drftDocSn;

    @Schema(description = "수정 이력 목록")
    private List<MtngMomHistResponse> histList;
}