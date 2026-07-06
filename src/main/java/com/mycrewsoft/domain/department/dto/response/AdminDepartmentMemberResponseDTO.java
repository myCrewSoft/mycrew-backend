package com.mycrewsoft.domain.department.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "부서 소속 사원 응답")
public class AdminDepartmentMemberResponseDTO {

    @Schema(description = "사원 ID", example = "1001")
    private Long empId;

    @Schema(description = "사원명", example = "홍길동")
    private String empNm;

    @Schema(description = "프로필 이미지 파일 ID", example = "12")
    private Long prflImgFileId;

    @Schema(description = "부서 코드", example = "DEPT_001")
    private String deptCd;

    @Schema(description = "부서명", example = "개발팀")
    private String deptNm;

    @Schema(description = "직급 코드", example = "JOB_GRD_01")
    private String jobGrdCd;

    @Schema(description = "직급명", example = "대리")
    private String jobGrdNm;

    @Schema(description = "직위 코드", example = "JOB_PSTN_01")
    private String jobPstnCd;

    @Schema(description = "직위명", example = "팀원")
    private String jobPstnNm;

    @Schema(description = "사원 상태 코드", example = "EMP_ACTIVE")
    private String empStatCd;

    @Schema(description = "사원 상태명", example = "재직")
    private String empStatNm;
}
