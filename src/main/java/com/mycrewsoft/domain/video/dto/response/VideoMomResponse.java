package com.mycrewsoft.domain.video.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "회의록 응답 DTO")
@Getter
@Setter
public class VideoMomResponse {

    @Schema(description = "회의록 ID")
    private Long momId;

    @Schema(description = "화상회의 ID")
    private Long vconfId;

    @Schema(description = "회의록 내용")
    private String momCn;

    @Schema(description = "회의록 상태 코드 (01: AI초안 / 02: 편집중 / 03: 검토중 / 04: 확정)")
    private String momSttusCd;

    @Schema(description = "담당자 ID")
    private Long edtrId;

    @Schema(description = "검토 요청 발송 일시")
    private LocalDateTime revwReqDt;

    @Schema(description = "정식 등록 일시")
    private LocalDateTime cnfrmDt;

    @Schema(description = "결재 회차")
    private Integer aprvlRoundNo;

    @Schema(description = "결재 목록")
    private List<VideoMomAprvlResponse> aprvlList;
    
    @Schema(description = "수정 이력 목록")
    private List<VideoMomHistResponse> histList;
}