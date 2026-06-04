package com.mycrewsoft.domain.department.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "관리자 부서 응답")
public class AdminDepartmentResponseDTO {

    @Schema(description = "부서 코드", example = "DEPT_001")
    private String deptCd;

    @Schema(description = "부서명", example = "개발팀")
    private String deptNm;

    @Schema(description = "상위 부서 코드", example = "DEPT_001")
    private String parentDeptCd;

    @Schema(description = "상위 부서명", example = "본부")
    private String parentDeptNm;

    @Schema(description = "사용 여부", example = "Y")
    private String useYn;

    @Schema(description = "소속 인원 수", example = "12")
    private Integer memberCount;
}
