package com.mycrewsoft.domain.approval.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "전자결재 AI 결재자 후보 정보")
public class ApprovalAiApproverCandidateDTO {

    @Schema(description = "사원 ID", example = "1111")
    private Long empId;

    @Schema(description = "사원명", example = "홍길동")
    private String empNm;

    @Schema(description = "부서 코드", example = "DEPT_023")
    private String deptCd;

    @Schema(description = "부서명", example = "관리부")
    private String deptNm;

    @Schema(description = "상위 부서 코드", example = "DEPT_001")
    private String parentDeptCd;

    @Schema(description = "직급 코드", example = "JOB01")
    private String jobGrdCd;

    @Schema(description = "직급명", example = "과장")
    private String jobGrdNm;

    @Schema(description = "직급 정렬 순서", example = "20")
    private Integer jobGrdSortOrder;

    @Schema(description = "직위 코드", example = "PSTN02")
    private String jobPstnCd;

    @Schema(description = "직위명", example = "팀장")
    private String jobPstnNm;

    @Schema(description = "직무 내용", example = "관리부 결재 검토")
    private String jobDutyCn;

    @Schema(description = "임원 여부", example = "N")
    private String execYn;

    @Schema(description = "전자서명 등록 여부", example = "Y")
    private String hasSignatureYn;

    @Schema(description = "부서 책임자 여부", example = "Y")
    private String deptLeaderYn;

    @Schema(description = "프로필 이미지 파일 ID", example = "9001")
    private Long prflImgFileId;
}
