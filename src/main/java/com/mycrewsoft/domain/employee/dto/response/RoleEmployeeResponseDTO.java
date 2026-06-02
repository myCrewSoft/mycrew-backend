package com.mycrewsoft.domain.employee.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "역할에 할당된 사원 정보 응답 DTO")
public class RoleEmployeeResponseDTO {
	@Schema(description = "역할 할당 ID", example = "1")
    private Long roleAssignmentId;
	@Schema(description = "사원 ID", example = "1")
    private Long empId;
	@Schema(description = "사원 이름", example = "홍길동")
    private String employeeName;
	@Schema(description = "부서 코드", example = "D001")
    private String deptCd;
	@Schema(description = "부서 이름", example = "인사팀")
    private String deptName;
	@Schema(description = "권한 범위 유형 코드", example = "GLOBAL")
    private String scopeTypeCd;
	@Schema(description = "권한 범위 유형 이름", example = "전체 접근")
    private String scopeId;
	@Schema(description = "사용여부", example = "Y")
    private String enabled;
}
