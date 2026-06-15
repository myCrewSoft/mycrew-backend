package com.mycrewsoft.domain.approval.dto.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "결재자 처리 상태 응답")
public class ApprovalStepStatusResponse {

    @Schema(description = "결재 단계 일련번호", example = "2001")
    private Long aprvlStepSn;

    @Schema(description = "결재 순서", example = "1")
    private Long aprvlOrd;

    @Schema(description = "결재 단계 상태 코드", example = "02")
    private String stepPrgrsCd;

    @Schema(description = "결재자 사원 ID", example = "1111")
    private Long aprvrEmpId;

    @Schema(description = "결재자명", example = "홍길동")
    private String aprvrEmpNm;

    @Schema(description = "결재자 부서명", example = "개발팀")
    private String aprvrDeptNm;

    @Schema(description = "결재자 직위명", example = "팀장")
    private String aprvrJobPstnNm;

    @Schema(description = "결재자 프로필 이미지 파일 ID", example = "1001")
    private Long aprvrPrflImgFileId;

    @Schema(description = "결재자 전자서명 파일 ID (승인 시점 스냅샷)", example = "9")
    private Long aprvrStampFileId;

    @Schema(description = "결재 처리 상태 코드", example = "01")
    private String aprvlPrgrsCd;

    @Schema(description = "승인 또는 반려 처리 일시")
    private LocalDateTime aprvlDt;

    @Schema(description = "승인 사유")
    private String aprvlRsn;

    @Schema(description = "반려 사유")
    private String rtrnRsn;
}
