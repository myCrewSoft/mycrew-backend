package com.mycrewsoft.domain.approval.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "결재 문서 상세 응답")
public class ApprovalDocumentDetailResponse {

    @Schema(description = "기안문 일련번호", example = "1001")
    private Long drftDocSn;

    @Schema(description = "문서 제목", example = "휴가 신청서")
    private String docTtl;

    @Schema(description = "템플릿 코드", example = "VACATION")
    private String tmplatCd;

    @Schema(description = "기안자 사원 ID", example = "1111")
    private Long empId;

    @Schema(description = "기안자명", example = "홍길동")
    private String drafterEmpNm;

    @Schema(description = "기안자 부서명", example = "개발팀")
    private String drafterDeptNm;

    @Schema(description = "기안자 직급명", example = "대리")
    private String drafterJobGrdNm;

    @Schema(description = "기안자 직위명", example = "팀원")
    private String drafterJobPstnNm;

    @Schema(description = "프로필 이미지 파일 ID", example = "1001")
    private Long prflImgFileId;

    @Schema(description = "결재 전문 내용")
    private String aprvlFullCn;

    @Schema(description = "결재 요청 일시")
    private LocalDateTime drftReqstDt;

    @Schema(description = "결재 희망 일시")
    private LocalDateTime aprvlHopeDt;

    @Schema(description = "결재 완료 일시")
    private LocalDateTime aprvlCmptnDt;

    @Schema(description = "반려 일시")
    private LocalDateTime rtrnDt;

    @Schema(description = "결재 문서 상태 코드", example = "01")
    private String aprvlDocSttsCd;

    @Schema(description = "첨부파일 ID", example = "1001")
    private Long atchFileId;

    @Schema(description = "결재 상태 표시 메시지")
    private List<String> statusMessages;

    @Schema(description = "결재 단계 및 결재자 상태")
    private List<ApprovalStepStatusResponse> approvalSteps;
}
