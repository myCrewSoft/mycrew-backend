package com.mycrewsoft.domain.employee.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "역할 목록 응답 DTO")
public class RoleListResponseDTO {
	@Schema(description = "역할 ID", example = "1")
    private Long roleId;
	@Schema(description = "역할 코드", example = "ADMIN")
    private String roleCode;
	@Schema(description = "역할 이름", example = "관리자")
    private String roleName;
	@Schema(description = "역할 설명", example = "시스템 관리자 역할")
    private String description;
	@Schema(description = "역할에 연동된 권한 수", example = "50")
    private Integer permissionCount;
	@Schema(description = "역할에 할당된 사원 수", example = "5")
    private Integer assignedEmployeeCount;
}
