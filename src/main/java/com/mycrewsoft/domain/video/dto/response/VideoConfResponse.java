package com.mycrewsoft.domain.video.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "화상회의 응답 DTO")
@Getter
@Setter
public class VideoConfResponse {

    @Schema(description = "화상회의 ID")
    private Long vconfId;

    @Schema(description = "회의 이름")
    private String vconfNm;

    @Schema(description = "LiveKit 방 이름")
    private String roomNm;

    @Schema(description = "회의 상태 코드 (01: 대기 / 02: 진행 중 / 03: 종료)")
    private String confSttusCd;

    @Schema(description = "생성자 ID")
    private Long crtrId;

    @Schema(description = "생성자 이름")
    private String crtrNm;

    @Schema(description = "회의록 상태 코드 (01: AI초안 / 02: 편집중 / 03: 검토중 / 04: 확정)")
    private String momSttusCd;     
    
    @Schema(description = "시작 일시")
    private LocalDateTime beginDt;

    @Schema(description = "종료 일시")
    private LocalDateTime endDt;

    @Schema(description = "생성 일시")
    private LocalDateTime creatDt;

    @Schema(description = "참여자 목록")
    private List<VideoPtcptResponse> ptcptList;
}