package com.mycrewsoft.domain.employee.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "권한 응답 DTO")
public class PermissionResponseDTO {
	@Schema(description = "권한 ID", example = "1")
    private Long permissionId;
	@Schema(description = "권한 코드", example = "ADMIN_READ_EMPLOYEE")
    private String permissionCode;
	@Schema(description = "권한 이름", example = "사원 정보 조회")
    private String permissionName;
	@Schema(description = "권한 설명", example = "사원 정보를 조회할 수 있는 권한")
    private String description;
	@Schema(description = "권한 활성화 여부", example = "Y")
    private String enabled;
}
