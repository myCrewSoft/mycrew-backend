package com.mycrewsoft.domain.department.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "부서 삭제 요청")
public class DepartmentDeleteRequestDTO {

    @Schema(description = "삭제 대상 부서에 소속 인원이 있을 때 이동시킬 대체 부서 코드", example = "DEPT_002")
    private String replacementDeptCd;
}
