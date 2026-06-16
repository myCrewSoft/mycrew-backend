package com.mycrewsoft.domain.approval.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "결재 템플릿 응답")
public class ApprovalTemplateResponse {

    @Schema(description = "템플릿 코드", example = "VACATION")
    private String tmplatCd;

    @Schema(description = "템플릿명", example = "휴가 신청서")
    private String tmplatNm;

    @Schema(description = "템플릿 내용")
    private String tmplatCn;

    @Schema(description = "사용 여부", example = "Y")
    private String useYn;

    @Schema(description = "첨부파일 ID", example = "1001")
    private Long atchFileId;

    @Schema(description = "현재 사원의 즐겨찾기 여부", example = "Y")
    private String favoriteYn;
}
