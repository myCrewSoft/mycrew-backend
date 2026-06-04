package com.mycrewsoft.domain.department.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "부서 사원 배정/이동 처리 결과")
public class DepartmentMemberMutationResponseDTO {

    @Schema(description = "이동 원본 부서 코드. 단순 배정이면 null입니다.", example = "DEPT_001")
    private String sourceDeptCd;

    @Schema(description = "배정 또는 이동 대상 부서 코드", example = "DEPT_002")
    private String targetDeptCd;

    @Schema(description = "처리된 사원 수", example = "2")
    private Integer affectedEmployeeCount;

    public DepartmentMemberMutationResponseDTO(String sourceDeptCd, String targetDeptCd, Integer affectedEmployeeCount) {
        this.sourceDeptCd = sourceDeptCd;
        this.targetDeptCd = targetDeptCd;
        this.affectedEmployeeCount = affectedEmployeeCount;
    }
}
